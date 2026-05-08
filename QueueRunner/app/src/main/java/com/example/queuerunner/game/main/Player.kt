package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import java.util.LinkedList
import java.util.Queue

class Player(private val gctx: GameContext) : IGameObject, IBoxCollidable {

    // --- 화면 렌더링용 고정/가변 좌표 ---
    // 캐릭터는 화면상 X 위치가 고정된 채, 배경이 밀려나는 방식으로 이동 표현
    private val screenX = 400f
    private val groundY = 800f
    private var screenY = groundY

    // --- 배경 스크롤용 가상 X 좌표 ---
    // 이 값을 HorzScrollBackground 의 speed 로 반영해 배경이 캐릭터 이동만큼 뒤로 밀림
    var virtualX = 0f
        private set

    // --- 물리 변수 (startJump 에서 역산하여 채움) ---
    private var isJumping = false
    private var speedX = 0f
    private var velocityY = 0f
    private var gravity = 0f
    private var targetVirtualX = 0f    // 착지 시 Snap 보정 목표

    // --- 1칸 픽셀 크기 ---
    private val blockSize = 200f

    // --- 커맨드 큐 ---
    private val commandQueue: Queue<String> = LinkedList()

    // --- 그리기 도구 ---
    private val paint = Paint().apply { color = Color.RED }
    // draw() 에서 매 프레임 RectF 를 새로 만들지 않도록 멤버로 선언
    private val rect = RectF()

    // IBoxCollidable: rect 를 충돌 사각형으로 재사용
    override val collisionRect: RectF get() = rect

    // -------------------------------------------------------------------------
    // 외부 인터페이스
    // -------------------------------------------------------------------------

    // CommandController 가 완성된 커맨드("OO", "OX", "XO", "XX")를 넣어주는 함수
    fun enqueueCombo(combo: String) {
        commandQueue.add(combo)
    }
    fun canAcceptCommand(): Boolean = !isJumping || commandQueue.isEmpty()

    // -------------------------------------------------------------------------
    // IGameObject 구현
    // -------------------------------------------------------------------------

    override fun update(gctx: GameContext) {
        val dt = gctx.frameTime

        // 대기 중일 때 큐에 명령이 있으면 다음 점프 시작
        if (!isJumping && commandQueue.isNotEmpty()) {
            startJump(commandQueue.poll()!!)
        }

        // 점프(이동) 중 물리 연산
        if (isJumping) {
            virtualX += speedX * dt

            velocityY += gravity * dt
            screenY += velocityY * dt

            // 착지 판정: 바닥에 닿으면 위치 Snap 보정 후 대기 상태로
            if (screenY >= groundY) {
                screenY = groundY
                virtualX = targetVirtualX   // 프레임 오차 누적 제거
                isJumping = false
            }
        }
    }

    override fun draw(canvas: Canvas) {
        // ToDo: 플레이어 예쁘게 바꾸기
        // 중심 아래 기준: 가로 80, 세로 80 크기의 사각형
        rect.set(screenX - 40f, screenY - 80f, screenX + 40f, screenY)
        canvas.drawRect(rect, paint)
    }


    // 내가 지정한 수치(거리·높이·체공 시간)로 속도·중력을 계산
    private fun startJump(combo: String) {
        val distance: Float
        val jumpHeight: Float
        val jumpDuration: Float

        when (combo) {
            "OO"       -> { distance = blockSize * 2; jumpHeight = 250f; jumpDuration = 0.5f }
            "OX", "XO" -> { distance = blockSize * 1; jumpHeight = 150f; jumpDuration = 0.4f }
            "XX"       -> { distance = blockSize * 3; jumpHeight = 350f; jumpDuration = 0.6f }
            else       -> return   // 알 수 없는 커맨드는 무시
        }

        targetVirtualX = virtualX + distance
        speedX   = distance / jumpDuration
        velocityY = -(4f * jumpHeight) / jumpDuration
        gravity  = (8f * jumpHeight) / (jumpDuration * jumpDuration)
        isJumping = true
    }
}