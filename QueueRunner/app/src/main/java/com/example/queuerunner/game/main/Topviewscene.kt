package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// Top-View 미로 도주 단계. Side-View 의 MainScene 과 자산이 0% 공유되는 별개 게임.
//
// [좌표계 — Y축 스크롤]
// - 플레이어는 화면상 (PLAYER_SCREEN_X, PLAYER_SCREEN_Y) 고정.
// - 미로 셀의 화면 위치 :
//     screenX = MAZE_LEFT + col * TILE_SIZE
//     screenY = PLAYER_SCREEN_Y + (cellRow - player.cellRow) * TILE_SIZE
// - row 0 = 미로 최상단 (출구), row 14 = 최하단 (입구).
// - 박스가 위로 올라갈수록 (cellRow 감소) 화면상 미로가 아래로 스크롤됨.
//
// [무한 반복]
// - MainScene → (Side 우측 끝) → TopViewScene.change()
// - TopViewScene → (출구 도달) → MainScene(gctx).change()
//   change() 는 인스턴스를 교체하므로 매 사이클 새 Scene 이 만들어짐.
//   누적 점수/총 거리 같은 cross-scene 상태는 GameSession 에 보관.
class TopViewScene(gctx: GameContext) : Scene(gctx) {

    val maze = Maze()
    val topPlayer = TopPlayer(gctx, maze)
    private val topController = TopCommandController(gctx, topPlayer)

    // Top-View 전용 layer. Side-View 의 Layer 와 분리.
    // BG → MAZE(벽/바닥) → JANITOR(환경미화원) → PLAYER → UI
    enum class Layer {
        BG, MAZE, JANITOR, PLAYER, UI
    }

    override val world = World(Layer.entries.toTypedArray()).apply {
        add(maze, Layer.MAZE)
        add(topPlayer, Layer.PLAYER)
        add(topController, Layer.UI)
    }

    override val clipsRect = true

    // 임시 배경 색 — step 2 에서 미로 렌더링 추가 시 어두운 골목 톤으로 교체 예정.
    private val bgPaint = Paint().apply { color = Color.rgb(40, 60, 80) }

    // 1단계 임시: 우측 상단 누르면 MainScene 으로 복귀. step 5 에서 출구 도달 자동 트리거로 교체.
    private val debugBackRect = RectF(1400f, 0f, 1600f, 100f)
    private val debugPaint = Paint().apply { color = Color.MAGENTA }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, gctx.metrics.width, gctx.metrics.height, bgPaint)
        super.draw(canvas)

        canvas.drawRect(debugBackRect, debugPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val pt = gctx.metrics.fromScreen(event.x, event.y)
            if (debugBackRect.contains(pt.x, pt.y)) {
                MainScene(gctx).change()
                return true
            }
            if (topController.onTouchEvent(event)) return true
        }
        return super.onTouchEvent(event)
    }

    companion object {
        // 미로 칸 크기 — Side-View 의 BLOCK_SIZE(200) 와 의도적으로 다름.
        const val TILE_SIZE = 180f

        // 미로 5칸이 화면 가로 중앙에 오도록.
        // (화면폭 1600 - 미로폭 900) / 2 = 350
        const val MAZE_LEFT = 350f

        // 플레이어 화면 고정 위치. 화면 아래쪽 1/4 지점이라 위로 더 많이 보임.
        const val PLAYER_SCREEN_X = 800f
        const val PLAYER_SCREEN_Y = 700f

        // 미로 격자 크기.
        const val MAZE_COLS = 5
        const val MAZE_ROWS = 15
    }
}