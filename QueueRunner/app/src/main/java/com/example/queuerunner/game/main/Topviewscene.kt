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
    val topCleaner = TopCleaner(gctx, maze, topPlayer)
    private val topController = TopCommandController(gctx, topPlayer)

    // 점수판 (이미지 숫자). Top-View 는 위쪽 가운데.
    private val scoreDisplay = ScoreDisplay(
        gctx,
        align = ScoreDisplay.Align.CENTER,
        anchorX = 800f,
        top = 40f,
        charWidth = 50f,
    )

    // Top-View 전용 layer. Side-View 의 Layer 와 분리.
    // BG → MAZE(벽/바닥) → JANITOR(환경미화원) → PLAYER → UI
    enum class Layer {
        BG, MAZE, JANITOR, PLAYER, UI
    }

    override val world = World(Layer.entries.toTypedArray()).apply {
        add(maze, Layer.MAZE)
        add(topCleaner, Layer.JANITOR)
        add(topPlayer, Layer.PLAYER)
        add(topController, Layer.UI)
        add(scoreDisplay, Layer.UI)
    }

    // isExitPausing 구간이 향후 출구 애니메이션이 들어갈 슬롯.
    private var isExitPausing = false
    // 잡힘 → GameOver 를 한 번만 push 하기 위한 플래그.
    private var gameOverHandled = false
    private var exitPauseTimer = 0f
    private var transitionTriggered = false

    override val clipsRect = true

    // 임시 배경 색
    private val bgPaint = Paint().apply { color = Color.rgb(40, 60, 80) }

    override fun update(gctx: GameContext) {
        super.update(gctx)  // world.update() → maze, topPlayer 등 진행

        // 이미 전환/게임오버가 확정됐으면 이 Scene 은 곧 사라지거나 멈춤. 추가 작업 X.
        if (transitionTriggered || gameOverHandled) return

        // 1) 출구 도달을 먼저 체크. 일시정지에 들어가면 = 탈출 확정.
        if (!isExitPausing && topPlayer.hasReachedExit) {
            isExitPausing = true
            exitPauseTimer = 0f
        }

        // 2) 일시정지(탈출 확정) 중이 아닐 때만 잡힘 판정.
        //    → 출구에 닿는 순간 미화원이 바짝 붙어 있어도 탈출이 이긴다.
        if (!isExitPausing && topCleaner.isCaught) {
            gameOverHandled = true
            GameOverScene(gctx).push()
            return
        }

        // 일시정지 중 타이머 진행. EXIT_PAUSE_DURATION 만큼 멈췄다가 Side-View 로 복귀.
        if (isExitPausing) {
            exitPauseTimer += gctx.frameTime

            // TODO: 출구 도달 애니메이션 슬롯.
            //   - 진행도 = exitPauseTimer / EXIT_PAUSE_DURATION
            //   - 박스 프레임 갱신, 페이드, 파티클 등을 여기에서 갱신.

            if (exitPauseTimer >= EXIT_PAUSE_DURATION) {
                transitionTriggered = true
                MainScene(gctx).change()
            }
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, gctx.metrics.width, gctx.metrics.height, bgPaint)
        super.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (topController.onTouchEvent(event)) return true
        }
        return super.onTouchEvent(event)
    }

    companion object {
        // 미로 칸 크기 — Side-View 의 BLOCK_SIZE(200) 와 의도적으로 다름.
        const val TILE_SIZE = 100f

        // 미로 5칸이 화면 가로 중앙에 오도록.
        // (화면폭 1600 - 미로폭 900) / 2 = 350
        const val MAZE_LEFT = 350f

        // 플레이어 화면 고정 위치. 화면 아래쪽 1/4 지점이라 위로 더 많이 보임.
        const val PLAYER_SCREEN_X = 800f
        const val PLAYER_SCREEN_Y = 700f

        // 미로 격자 크기.
        const val MAZE_COLS = 9
        const val MAZE_ROWS = 27

        const val EXIT_PAUSE_DURATION = 0.8f
    }
}