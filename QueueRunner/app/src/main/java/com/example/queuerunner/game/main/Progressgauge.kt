package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.util.Gauge
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// Side-View 전용 진행률 게이지 (화면 상단 HUD).
//
// [구간]
// - 시작점 = 플레이어가 처음 서 있는 virtualX (생성 시점 값을 그대로 캡처).
// - 끝점   = TopView 로 전환되는 virtualX (Cleaner.TRANSITION_X).
//
// [표시]
// - 채워지는 진행 바 = 플레이어가 끝점까지 얼마나 왔는지 (박스 톤).
// - 네모(박스) 마커  = 플레이어 현재 위치.
// - 빨간 원 마커      = 관리사무소(경비) 아저씨(Janitor) 현재 위치.
//   둘 다 매 프레임 virtualX 를 읽어 위치를 갱신한다.
//
// TopViewScene 에는 추가하지 않으므로 미로 단계에서는 게이지가 나타나지 않는다.
class ProgressGauge(
    private val player: Player,
    private val janitor: Janitor,
) : IGameObject {

    // "플레이어가 처음 시작하는 virtualX". 게이지 생성 시점(=update 전)의 값을 캡처한다.
    private val startX = player.virtualX
    private val endX = Cleaner.TRANSITION_X

    private val gauge = Gauge(
        GAUGE_THICKNESS,
        GAUGE_FG_COLOR,   // 채워지는 진행분
        GAUGE_BG_COLOR,   // 배경 트랙
    )

    private val playerFill = Paint().apply {
        color = GAUGE_FG_COLOR
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val janitorFill = Paint().apply {
        color = JANITOR_COLOR
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val markerStroke = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val playerMarkerRect = RectF()

    override fun update(gctx: GameContext) {
        // 위치는 draw() 에서 player/janitor 의 최신 virtualX 를 직접 읽어 계산한다.
    }

    override fun draw(canvas: Canvas) {
        val playerP = progressOf(player.virtualX)
        val janitorP = progressOf(janitor.virtualX)

        // 1. 진행 바: 배경 + 플레이어 진행분.
        gauge.draw(canvas, GAUGE_X, GAUGE_Y, GAUGE_WIDTH, playerP)

        // 2. 아저씨(빨간 원) 먼저 → 겹칠 때 플레이어 마커가 위로 오도록.
        drawJanitorMarker(canvas, markerX(janitorP), GAUGE_Y)

        // 3. 플레이어(박스 네모).
        drawPlayerMarker(canvas, markerX(playerP), GAUGE_Y)
    }

    private fun progressOf(virtualX: Float): Float {
        val span = endX - startX
        if (span <= 0f) return 0f
        return ((virtualX - startX) / span).coerceIn(0f, 1f)
    }

    private fun markerX(progress: Float): Float = GAUGE_X + progress * GAUGE_WIDTH

    private fun drawPlayerMarker(canvas: Canvas, cx: Float, cy: Float) {
        val half = PLAYER_MARKER_SIZE / 2f
        playerMarkerRect.set(cx - half, cy - half, cx + half, cy + half)
        canvas.drawRoundRect(playerMarkerRect, PLAYER_MARKER_CORNER, PLAYER_MARKER_CORNER, playerFill)
        canvas.drawRoundRect(playerMarkerRect, PLAYER_MARKER_CORNER, PLAYER_MARKER_CORNER, markerStroke)
    }

    private fun drawJanitorMarker(canvas: Canvas, cx: Float, cy: Float) {
        canvas.drawCircle(cx, cy, JANITOR_MARKER_RADIUS, janitorFill)
        canvas.drawCircle(cx, cy, JANITOR_MARKER_RADIUS, markerStroke)
    }

    companion object {
        // 게이지 위치/크기 (가상 해상도 1600x900 기준, 화면 상단).
        private const val GAUGE_X = 200f
        private const val GAUGE_Y = 100f
        private const val GAUGE_WIDTH = 1200f
        // Gauge 내부 기준 길이 1.0 대비 두께. 화면 두께 ≈ THICKNESS * GAUGE_WIDTH.
        private const val GAUGE_THICKNESS = 0.02f

        private const val PLAYER_MARKER_SIZE = 36f
        private const val PLAYER_MARKER_CORNER = 8f
        private const val JANITOR_MARKER_RADIUS = 17f

        private val GAUGE_BG_COLOR = Color.argb(150, 30, 30, 30)
        private val GAUGE_FG_COLOR = Color.rgb(205, 150, 90)   // 박스(골판지) 톤
        private val JANITOR_COLOR = Color.rgb(225, 70, 60)     // 빨강
    }
}