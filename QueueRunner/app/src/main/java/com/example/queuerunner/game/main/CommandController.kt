package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.util.LabelUtil
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class CommandController(private val gctx: GameContext) : IGameObject {
    // 커맨드 종류
    enum class Command { O, X }

    // 기획서 반영: 2슬롯 밀어내기 큐
    private val queue = mutableListOf<Command>()

    // 화면 하단 버튼 영역 (가상 해상도 1600x900 기준)
    private val btnORect = RectF(200f, 700f, 700f, 850f)
    private val btnXRect = RectF(900f, 700f, 1400f, 850f)

    // UI 그리기 도구
    private val btnPaint = Paint().apply { color = Color.DKGRAY }
    private val labelUtilBtn = LabelUtil(100f, Color.WHITE, Paint.Align.CENTER)
    private val labelUtilQueue = LabelUtil(120f, Color.YELLOW, Paint.Align.CENTER)

    override fun update(gctx: GameContext) {
        // UI 자체의 주기적 로직 (현재는 없음)
    }

    override fun draw(canvas: Canvas) {
        // 1. O, X 버튼 그리기
        canvas.drawRoundRect(btnORect, 30f, 30f, btnPaint)
        canvas.drawRoundRect(btnXRect, 30f, 30f, btnPaint)

        // 2. 버튼 텍스트
        labelUtilBtn.draw(canvas, "O", btnORect.centerX(), btnORect.centerY() + 35f)
        labelUtilBtn.draw(canvas, "X", btnXRect.centerX(), btnXRect.centerY() + 35f)

        // 3. 기획서 반영: 현재 Queue 상태 화면에 표시
        val slot1 = queue.getOrNull(0)?.name ?: "_"
        val slot2 = queue.getOrNull(1)?.name ?: "_"
        labelUtilQueue.draw(canvas, "CMD: [ $slot1 ] [ $slot2 ]", 800f, 200f)
    }

    // 터치 입력 처리 (버튼이 눌렸을 때만 반응)
    fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val pt = gctx.metrics.fromScreen(event.x, event.y)

            if (btnORect.contains(pt.x, pt.y)) {
                pushCommand(Command.O)
                return true
            } else if (btnXRect.contains(pt.x, pt.y)) {
                pushCommand(Command.X)
                return true
            }
        }
        return false
    }

    // 기획서 핵심: 밀어내기(Queue) 로직
    private fun pushCommand(cmd: Command) {
        // 큐가 2칸 꽉 찼으면 가장 오래된(앞쪽, index 0) 데이터를 삭제
        if (queue.size >= 2) {
            queue.removeAt(0)
        }
        queue.add(cmd)

        // 기획서: 2개가 찼을 때 완성된 커맨드 반환(발동) 로직
        if (queue.size == 2) {
            fireCommand(queue[0], queue[1])
        }
    }

    private fun fireCommand(cmd1: Command, cmd2: Command) {
        // TODO: 나중에 Player 클래스를 만들면 여기서 Player의 이동 함수를 호출합니다!
        println("행동 발동! 입력된 조합: $cmd1 + $cmd2")
    }
}