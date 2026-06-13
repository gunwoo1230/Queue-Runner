package com.example.queuerunner.game.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.queuerunner.R
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
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

    // O/X 버튼 안에 그릴 박스 포즈 이미지 (위로 점프 / 아래로 슬라이드).
    // ※ 화살표 이미지가 아니라 박스 스프라이트다. (버튼 표시는 기존 그대로 유지)
    private val upPoseBitmap = gctx.res.getBitmap(R.mipmap.gurubox_up)
    private val downPoseBitmap = gctx.res.getBitmap(R.mipmap.gurubox_down)

    // 버튼 안에 그릴 이미지 영역을 매 프레임 재사용.
    private val btnIconRect = RectF()

    // ----- 머리 위 말풍선(흰 말풍선 + 빨간 화살표) 그리기용 도구 -----
    private val bubbleBodyRect = RectF()
    private val bubblePath = Path()
    private val tailPath = Path()
    private val bubbleFillPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val bubbleStrokePaint = Paint().apply {
        color = Color.rgb(60, 60, 60)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    private val arrowPaint = Paint().apply {
        color = Color.rgb(220, 40, 40)   // 빨강
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun update(gctx: GameContext) {
        // UI 자체의 주기적 로직 (현재는 없음)
    }

    override fun draw(canvas: Canvas) {
        // 1. 버튼 배경 (둥근 사각형 그대로 유지)
        canvas.drawRoundRect(btnORect, 30f, 30f, btnPaint)
        canvas.drawRoundRect(btnXRect, 30f, 30f, btnPaint)

        // 2. 버튼 안에 박스 포즈 이미지 그리기 (기존과 동일)
        drawIcon(canvas, upPoseBitmap, btnORect, btnIconRect, scale = 0.7f)
        drawIcon(canvas, downPoseBitmap, btnXRect, btnIconRect, scale = 0.7f)

        // 3. 플레이어 머리 위에 마지막 커맨드를 "흰 말풍선 + 빨간 화살표" 로 표시
        val lastCmd = queue.lastOrNull() ?: return
        val pointUp = (lastCmd == Command.O)   // O = 위, X = 아래
        drawCommandBubble(
            canvas,
            player.collisionRect.centerX(),
            player.collisionRect.top,
            pointUp,
        )
    }

    // 머리 끝(headTopY) 바로 위에 꼬리 달린 흰 말풍선을 그리고 그 안에 빨간 화살표를 채운다.
    private fun drawCommandBubble(
        canvas: Canvas,
        headCenterX: Float,
        headTopY: Float,
        pointUp: Boolean,
    ) {
        val tailTipY = headTopY - BUBBLE_GAP
        val bodyBottom = tailTipY - BUBBLE_TAIL_H
        val bodyTop = bodyBottom - BUBBLE_H
        val bodyLeft = headCenterX - BUBBLE_W / 2f
        val bodyRight = headCenterX + BUBBLE_W / 2f
        bubbleBodyRect.set(bodyLeft, bodyTop, bodyRight, bodyBottom)

        // 본체(둥근 사각형) ∪ 꼬리(삼각형) 를 하나의 외곽선으로 합쳐
        // 채우기 + 테두리를 깔끔하게(이음매 없이) 그린다.
        bubblePath.reset()
        bubblePath.addRoundRect(bubbleBodyRect, BUBBLE_CORNER, BUBBLE_CORNER, Path.Direction.CW)
        tailPath.reset()
        tailPath.moveTo(headCenterX - BUBBLE_TAIL_HALF, bodyBottom)
        tailPath.lineTo(headCenterX + BUBBLE_TAIL_HALF, bodyBottom)
        tailPath.lineTo(headCenterX, tailTipY)
        tailPath.close()
        bubblePath.op(tailPath, Path.Op.UNION)

        canvas.drawPath(bubblePath, bubbleFillPaint)
        canvas.drawPath(bubblePath, bubbleStrokePaint)

        // 말풍선 본체 중앙에 빨간 화살표.
        val arrowCy = (bodyTop + bodyBottom) / 2f
        ArrowDrawer.draw(
            canvas,
            headCenterX,
            arrowCy,
            ARROW_W,
            ARROW_H,
            if (pointUp) ArrowDrawer.Dir.UP else ArrowDrawer.Dir.DOWN,
            arrowPaint,
        )
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
        // 머리 위 말풍선 본체 / 꼬리 / 화살표 크기.
        private const val BUBBLE_W = 130f
        private const val BUBBLE_H = 120f
        private const val BUBBLE_CORNER = 24f
        private const val BUBBLE_TAIL_H = 26f       // 꼬리 높이
        private const val BUBBLE_TAIL_HALF = 20f    // 꼬리 밑변 절반
        private const val BUBBLE_GAP = 10f          // 꼬리 끝과 머리 사이 간격
        private const val ARROW_W = 56f
        private const val ARROW_H = 72f
    }
}