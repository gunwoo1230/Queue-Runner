package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// Top-View 전용 커맨드 컨트롤러.
//
// 버튼 아이콘을 박스 이미지 대신 좌/우 화살표로 표시한다.
//   왼쪽 버튼(O) = ←   /   오른쪽 버튼(X) = →
// 이동 방향도 버튼과 일치한다 (TopPlayer.startMove 참고):
//   OO(왼쪽 두 번) → 왼쪽,  XX(오른쪽 두 번) → 오른쪽,  OX/XO → 위.
class TopCommandController(
    private val gctx: GameContext,
    private val player: TopPlayer,
) : IGameObject {

    enum class Command { O, X }

    private val queue = mutableListOf<Command>()

    private val btnORect = RectF(200f, 700f, 700f, 850f)
    private val btnXRect = RectF(900f, 700f, 1400f, 850f)

    // 반투명 버튼. 미로/박스가 비쳐 보이도록 alpha 낮춤.
    private val btnPaint = Paint().apply { color = Color.argb(100, 30, 30, 30) }

    // 버튼 위 화살표. 어두운 반투명 버튼 위라 흰색이 가장 잘 보인다.
    // (빨간 화살표로 바꾸고 싶으면 Color.rgb(220, 40, 40) 로만 교체하면 된다.)
    private val arrowPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(btnORect, 30f, 30f, btnPaint)
        canvas.drawRoundRect(btnXRect, 30f, 30f, btnPaint)

        // 왼쪽 버튼 = ← , 오른쪽 버튼 = →
        ArrowDrawer.draw(
            canvas, btnORect.centerX(), btnORect.centerY(),
            ARROW_W, ARROW_H, ArrowDrawer.Dir.LEFT, arrowPaint,
        )
        ArrowDrawer.draw(
            canvas, btnXRect.centerX(), btnXRect.centerY(),
            ARROW_W, ARROW_H, ArrowDrawer.Dir.RIGHT, arrowPaint,
        )
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val pt = gctx.metrics.fromScreen(event.x, event.y)
            if (btnORect.contains(pt.x, pt.y)) {
                pushCommand(Command.X)
                return true
            } else if (btnXRect.contains(pt.x, pt.y)) {
                pushCommand(Command.O)
                return true
            }
        }
        return false
    }

    private fun pushCommand(cmd: Command) {
        if (!player.canAcceptCommand()) return
        if (queue.size >= 2) queue.removeAt(0)
        queue.add(cmd)
        if (queue.size == 2) {
            fireCommand(queue[0], queue[1])
        }
    }

    private fun fireCommand(c1: Command, c2: Command) {
        player.enqueueCombo("${c1.name}${c2.name}")
    }

    companion object {
        // 좌/우 화살표라 가로로 길쭉하게.
        private const val ARROW_W = 120f
        private const val ARROW_H = 70f
    }
}