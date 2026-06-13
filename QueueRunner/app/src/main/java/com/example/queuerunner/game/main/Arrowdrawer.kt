package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

// 커맨드 UI 에서 쓰는 단색 화살표를 Canvas Path 로 직접 그린다.
// 비트맵 리소스 없이 색/방향/크기를 코드로 제어할 수 있어
//   - (사이드뷰) 머리 위 말풍선 안의 빨간 화살표
//   - (탑뷰) 좌/우 버튼 화살표
// 양쪽에서 공용으로 쓴다.
//
// 렌더 스레드에서만 호출되므로 Path 를 1개만 재사용해 매 프레임 할당을 피한다.
object ArrowDrawer {
    enum class Dir { UP, DOWN, LEFT, RIGHT }

    private val path = Path()

    // (cx, cy) 를 중심으로 w×h 크기의 화살표를 dir 방향으로 채워 그린다.
    fun draw(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        dir: Dir,
        paint: Paint,
    ) {
        val halfW = w / 2f
        val halfH = h / 2f
        val left = cx - halfW
        val right = cx + halfW
        val top = cy - halfH
        val bottom = cy + halfH

        path.reset()
        when (dir) {
            Dir.UP -> {
                val headH = h * 0.45f       // 삼각 머리 높이
                val shaftHalf = w * 0.18f   // 몸통(기둥) 폭의 절반
                path.moveTo(cx, top)                       // 꼭짓점
                path.lineTo(left, top + headH)             // 머리 왼쪽
                path.lineTo(cx - shaftHalf, top + headH)
                path.lineTo(cx - shaftHalf, bottom)        // 몸통 왼쪽 아래
                path.lineTo(cx + shaftHalf, bottom)        // 몸통 오른쪽 아래
                path.lineTo(cx + shaftHalf, top + headH)
                path.lineTo(right, top + headH)            // 머리 오른쪽
            }
            Dir.DOWN -> {
                val headH = h * 0.45f
                val shaftHalf = w * 0.18f
                path.moveTo(cx, bottom)
                path.lineTo(left, bottom - headH)
                path.lineTo(cx - shaftHalf, bottom - headH)
                path.lineTo(cx - shaftHalf, top)
                path.lineTo(cx + shaftHalf, top)
                path.lineTo(cx + shaftHalf, bottom - headH)
                path.lineTo(right, bottom - headH)
            }
            Dir.LEFT -> {
                val headW = w * 0.45f
                val shaftHalf = h * 0.18f
                path.moveTo(left, cy)
                path.lineTo(left + headW, top)
                path.lineTo(left + headW, cy - shaftHalf)
                path.lineTo(right, cy - shaftHalf)
                path.lineTo(right, cy + shaftHalf)
                path.lineTo(left + headW, cy + shaftHalf)
                path.lineTo(left + headW, bottom)
            }
            Dir.RIGHT -> {
                val headW = w * 0.45f
                val shaftHalf = h * 0.18f
                path.moveTo(right, cy)
                path.lineTo(right - headW, top)
                path.lineTo(right - headW, cy - shaftHalf)
                path.lineTo(left, cy - shaftHalf)
                path.lineTo(left, cy + shaftHalf)
                path.lineTo(right - headW, cy + shaftHalf)
                path.lineTo(right - headW, bottom)
            }
        }
        path.close()
        canvas.drawPath(path, paint)
    }
}