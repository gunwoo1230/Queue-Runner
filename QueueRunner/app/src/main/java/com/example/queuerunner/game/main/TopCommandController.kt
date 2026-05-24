package com.example.queuerunner.game.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import com.example.queuerunner.R
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// Top-View 전용 커맨드 컨트롤러.
//
// Side-View 의 CommandController 와 거의 동일한 구조이지만:
// - TopPlayer 를 참조한다 (Side 의 Player 가 아님).
// - 미로 위에 겹쳐 그리기 때문에 버튼 배경을 반투명으로 둔다 (옵션 c).
// - 머리 위 최근 커맨드 아이콘은 일단 생략 (필요하면 step 4 에서 추가).
class TopCommandController(
    private val gctx: GameContext,
    private val player: TopPlayer,
) : IGameObject {

    enum class Command { O, X }

    private val queue = mutableListOf<Command>()

    private val btnORect = RectF(200f, 700f, 700f, 850f)
    private val btnXRect = RectF(900f, 700f, 1400f, 850f)

    // 반투명 버튼 (옵션 c). 미로/박스가 비쳐 보이도록 alpha 낮춤.
    private val btnPaint = Paint().apply { color = Color.argb(100, 30, 30, 30) }

    private val upArrowBitmap = gctx.res.getBitmap(R.mipmap.gurubox_up)
    private val downArrowBitmap = gctx.res.getBitmap(R.mipmap.gurubox_down)
    private val btnIconRect = RectF()

    // 아이콘도 약간 투명하게 (그래야 아래 미로/박스랑 자연스럽게 섞임).
    private val iconPaint = Paint().apply { alpha = 180 }

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(btnORect, 30f, 30f, btnPaint)
        canvas.drawRoundRect(btnXRect, 30f, 30f, btnPaint)
        drawIcon(canvas, upArrowBitmap, btnORect, 0.7f)
        drawIcon(canvas, downArrowBitmap, btnXRect, 0.7f)
    }

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

    private fun drawIcon(canvas: Canvas, bitmap: Bitmap, container: RectF, scale: Float) {
        val size = container.height() * scale
        val cx = container.centerX()
        val cy = container.centerY()
        btnIconRect.set(cx - size / 2f, cy - size / 2f, cx + size / 2f, cy + size / 2f)
        canvas.drawBitmap(bitmap, null, btnIconRect, iconPaint)
    }
}