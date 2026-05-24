package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.RectF
import com.example.queuerunner.R
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import java.util.LinkedList
import java.util.Queue

class Player(private val gctx: GameContext) : IGameObject, IBoxCollidable {

    // --- 화면 렌더링용 고정/가변 좌표 ---
    // 캐릭터는 화면상 X 위치가 고정된 채, 배경이 밀려나는 방식으로 이동 표현.
    // groundY 는 배경 zoom 적용 후의 새 지면 위치에 맞춰 700 으로 설정.
    private val screenX = 400f
    private val groundY = 700f
    private var screenY = groundY

    // --- 배경 스크롤용 가상 X 좌표 ---
    // 이 값을 HorzScrollBackground 의 speed 로 반영해 배경이 캐릭터 이동만큼 뒤로 밀림
    var virtualX = 100f
        private set

    // --- 정상 점프 물리 변수 (startJump 에서 역산하여 채움) ---
    // isJumping 은 CollisionChecker 가 "점프 중에는 검사 skip" 판단에 사용하기 위해
    // 외부에서 읽을 수 있게 public getter 로 노출한다. 쓰기는 Player 내부에서만.
    var isJumping = false
        private set
    private var speedX = 0f
    private var velocityY = 00f
    private var gravity = 0f
    private var targetVirtualX = 0f

    // --- 슬로우 이동 상태 ---
    // SLOWDOWN 효과로 발동되는 1칸 이동. y 고정인 채 x 만 SLOW_MOVE_DURATION 에 걸쳐 보간.
    // 슬로우 도중에는 acceptsInput = false 라 새 입력도 못 받고
    // CollisionChecker 도 검사를 건너뛴다(같은 장애물에 매 프레임 또 부딪히는 것을 막기 위함).
    // 슬로우가 끝나는 순간 acceptsInput = true 가 되며 같은 프레임의 CollisionChecker 가
    // 곧장 다음 검사를 수행한다 -> 또 충돌이면 또 슬로우, 없으면 입력 재개.
    private var isSlowMoving = false
    private var slowMoveStartX = 0f
    private var slowMoveTargetX = 0f
    private var slowMoveProgress = 0f   // 0..1

    // --- 게임 오버 상태 ---
    // GAME_OVER 효과를 받으면 true 가 되고 박스의 모든 움직임이 정지한다.
    // MainScene 이 매 프레임 이 값을 보고 Janitor.isCaught 와 동일한 경로로
    // GameOverScene 으로 전환한다.
    var isGameOver = false
        private set

    // --- 입력 수용 여부 (single source of truth) ---
    // false 일 때:
    //   - CommandController 가 enqueueCombo() 를 호출해도 큐에 적재되지 않는다.
    //   - CollisionChecker 가 충돌 검사를 건너뛴다(이미 충돌 처리 중이므로).
    //   - 향후 UI 가 "잠금" 상태를 표시할 때도 이 플래그 하나만 본다.
    // 정상 점프 중에는 true 를 유지하므로 큐 1 칸 선입력은 그대로 허용된다.
    var acceptsInput: Boolean = true
        private set

    // --- 1칸 픽셀 크기 ---
    private val blockSize = 200f

    // --- 커맨드 큐 ---
    private val commandQueue: Queue<String> = LinkedList()

    // --- 그리기 / 충돌 ---
    private val bitmap = gctx.res.getBitmap(R.mipmap.gurubox_box)
    // draw() 와 IBoxCollidable.collisionRect 가 공용으로 쓰는 사각형.
    // update() 끝에 갱신되어 같은 프레임의 CollisionChecker 가 최신 위치를 본다.
    private val rect = RectF()
    override val collisionRect: RectF get() = rect

    // -------------------------------------------------------------------------
    // 외부 인터페이스
    // -------------------------------------------------------------------------

    // CommandController 가 완성된 커맨드("OO", "OX", "XO", "XX") 를 넘겨주는 함수.
    fun enqueueCombo(combo: String) {
        if (!acceptsInput) return
        commandQueue.add(combo)
    }

    // 정상 점프 중이면 큐에 1 개까지만 선입력 허용, 슬로우/게임오버면 일체 차단.
    fun canAcceptCommand(): Boolean = acceptsInput && (!isJumping || commandQueue.isEmpty())

    // CollisionChecker 가 박스 충돌 박스와 장애물 띠가 겹쳤을 때 호출한다.
    //   SLOWDOWN  -> 슬로우 1칸 이동으로 진입
    //   GAME_OVER -> 게임 종료 상태로
    // acceptsInput 이 이미 false 면 처리 중이므로 무시한다(이중 안전망).
    fun applyHit(effect: HitEffect) {
        if (!acceptsInput) return
        when (effect) {
            HitEffect.SLOWDOWN  -> startSlowMove()
            HitEffect.GAME_OVER -> enterGameOver()
        }
    }

    // -------------------------------------------------------------------------
    // 상태 전이 헬퍼
    // -------------------------------------------------------------------------

    // 내가 지정한 수치(거리·높이·체공 시간)로 속도·중력을 계산.
    private fun startJump(combo: String) {
        val distance: Float
        val jumpHeight: Float
        val jumpDuration: Float

        when (combo) {
            "OO"       -> { distance = blockSize * 2; jumpHeight = 250f; jumpDuration = 0.5f }
            "OX", "XO" -> { distance = blockSize * 1; jumpHeight = 150f; jumpDuration = 0.4f }
            "XX"       -> { distance = blockSize * 3; jumpHeight = 350f; jumpDuration = 0.6f }
            else       -> return
        }

        targetVirtualX = virtualX + distance
        speedX   = distance / jumpDuration
        velocityY = -(4f * jumpHeight) / jumpDuration
        gravity  = (8f * jumpHeight) / (jumpDuration * jumpDuration)
        isJumping = true
    }

    // SLOWDOWN 효과로 진입. 1칸을 SLOW_MOVE_DURATION 초에 걸쳐 미끄러진다.
    // CollisionChecker 가 isJumping == false 일 때만 호출하므로
    // 이 시점 virtualX 는 이미 targetVirtualX 로 Snap 되어 정확한 칸 경계에 있다.
    private fun startSlowMove() {
        isJumping = false
        screenY = groundY

        isSlowMoving = true
        acceptsInput = false
        slowMoveStartX = virtualX
        slowMoveTargetX = virtualX + blockSize
        slowMoveProgress = 0f

        // 정상 점프 중 큐에 미리 들어와 있던 선입력은 의미가 사라졌으므로 비운다.
        // 슬로우가 끝난 다음 새로 받는 입력만 받도록.
        commandQueue.clear()
    }

    private fun enterGameOver() {
        isGameOver = true
        acceptsInput = false
        isJumping = false
        isSlowMoving = false
        screenY = groundY
        commandQueue.clear()
    }

    // -------------------------------------------------------------------------
    // IGameObject
    // -------------------------------------------------------------------------

    override fun update(gctx: GameContext) {
        val dt = gctx.frameTime

        // 게임 오버: 모든 움직임 정지. rect 만 그릴 위치 유지하도록 갱신.
        if (isGameOver) {
            rect.set(screenX - 80f, screenY - 160f, screenX + 80f, screenY)
            return
        }

        // 슬로우 이동 중: 가상 X 만 보간으로 천천히 증가, y 는 지면 고정.
        if (isSlowMoving) {
            slowMoveProgress = (slowMoveProgress + dt / SLOW_MOVE_DURATION).coerceAtMost(1f)
            virtualX = slowMoveStartX + (slowMoveTargetX - slowMoveStartX) * slowMoveProgress

            if (slowMoveProgress >= 1f) {
                virtualX = slowMoveTargetX
                isSlowMoving = false
                // 다음 프레임 CollisionChecker 가 즉시 다시 검사 -> 또 충돌이면 또 슬로우.
                acceptsInput = true
            }

            rect.set(screenX - 80f, screenY - 160f, screenX + 80f, screenY)
            return
        }

        // 대기 중이고 큐에 명령이 있으면 다음 점프 시작.
        if (!isJumping && commandQueue.isNotEmpty()) {
            startJump(commandQueue.poll()!!)
        }

        // 정상 점프 중 물리 연산.
        if (isJumping) {
            virtualX += speedX * dt

            velocityY += gravity * dt
            screenY += velocityY * dt

            // 착지 판정: 바닥에 닿으면 위치 Snap 보정 후 대기 상태로.
            if (screenY >= groundY) {
                screenY = groundY
                virtualX = targetVirtualX
                isJumping = false
            }
        }

        // 충돌 박스도 매 프레임 갱신해 같은 프레임의 CollisionChecker 가 최신 위치로 판정한다.
        // (World 의 layer 순서가 PLAYER -> CONTROLLER 이므로 player.update() 가 먼저 돈다.)
        rect.set(screenX - 80f, screenY - 160f, screenX + 80f, screenY)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, rect, null)
    }

    companion object {
        // 슬로우 이동 1칸 소요 시간. 정상 O+X 점프(0.4 초) 의 2 배로 두어
        // 페널티가 확실히 느껴지면서 흐름이 너무 끊기지 않게 한다.
        // 0.8 초 동안 Janitor(400f/s) 가 약 320f(=1.6 칸) 다가오므로
        // 박스의 전진 1 칸과 합쳐 순 0.6 칸의 거리 손실이 발생한다.
        private const val SLOW_MOVE_DURATION = 0.8f
    }
}