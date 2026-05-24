package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Bitmap
import com.example.queuerunner.R
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.util.LabelUtil
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class CommandController(
    private val gctx: GameContext,
    private val player: Player,
) : IGameObject {
    // 커맨드 종류
    enum class Command { O, X }

    // 2슬롯 밀어내기 큐
    private val queue = mutableListOf<Command>()

    // 화면 하단 버튼 영역 (가상 해상도 1600x900 기준)
    private val btnORect = RectF(200f, 700f, 700f, 850f)
    private val btnXRect = RectF(900f, 700f, 1400f, 850f)

    // UI 그리기 도구
    private val btnPaint = Paint().apply { color = Color.DKGRAY }

    // 위/아래 화살표 비트맵. O = 위, X = 아래.
    private val upArrowBitmap = gctx.res.getBitmap(R.mipmap.gurubox_up)
    private val downArrowBitmap = gctx.res.getBitmap(R.mipmap.gurubox_down)

    // 버튼 안에 그릴 화살표 영역 / 머리 위에 그릴 영역을 매 프레임 재사용.
    private val btnIconRect = RectF()
    private val headIconRect = RectF()

    override fun update(gctx: GameContext) {
        // UI 자체의 주기적 로직 (현재는 없음)
    }

    override fun draw(canvas: Canvas) {
        // 1. 버튼 배경 (둥근 사각형 그대로 유지)
        canvas.drawRoundRect(btnORect, 30f, 30f, btnPaint)
        canvas.drawRoundRect(btnXRect, 30f, 30f, btnPaint)

        // 2. 버튼 안에 화살표 비트맵 그리기 (텍스트 대신)
        drawIcon(canvas, upArrowBitmap, btnORect, btnIconRect, scale = 0.7f)
        drawIcon(canvas, downArrowBitmap, btnXRect, btnIconRect, scale = 0.7f)

        // 3. 플레이어 머리 위에 마지막 커맨드 1개만 표시
        val lastCmd = queue.lastOrNull() ?: return
        val cmdBitmap = if (lastCmd == Command.O) upArrowBitmap else downArrowBitmap
        val centerX = player.collisionRect.centerX()
        val headTopY = player.collisionRect.top
        headIconRect.set(
            centerX - HEAD_ICON_HALF,
            headTopY - HEAD_ICON_OFFSET - HEAD_ICON_SIZE,
            centerX + HEAD_ICON_HALF,
            headTopY - HEAD_ICON_OFFSET,
        )
        canvas.drawBitmap(cmdBitmap, null, headIconRect, null)
    }

    // 버튼 사각형 안에 비트맵을 정사각형으로 가운데 정렬해 그리는 헬퍼.
    private fun drawIcon(
        canvas: Canvas,
        bitmap: Bitmap,
        container: RectF,
        out: RectF,
        scale: Float,
    ) {
        val size = container.height() * scale
        val cx = container.centerX()
        val cy = container.centerY()
        out.set(cx - size / 2f, cy - size / 2f, cx + size / 2f, cy + size / 2f)
        canvas.drawBitmap(bitmap, null, out, null)
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

    // 밀어내기(Queue) 로직
    private fun pushCommand(cmd: Command) {
        if (!player.canAcceptCommand()) return
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
        val combo = "${cmd1.name}${cmd2.name}"  // "OO", "OX", "XO", "XX"
        player.enqueueCombo(combo)
    }

    companion object {
        // 머리 위 아이콘 크기 / 머리 끝과의 간격.
        private const val HEAD_ICON_SIZE = 100f
        private const val HEAD_ICON_HALF = 50f
        private const val HEAD_ICON_OFFSET = 20f  // collisionRect.top 으로부터 위로 띄울 간격
    }
}