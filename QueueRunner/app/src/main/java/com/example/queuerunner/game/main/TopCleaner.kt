package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.hypot

// Top-View 환경미화원. Side-View Janitor 처럼 격자 무시 + 매 프레임 픽셀 단위 연속 이동.
//
// [추격 방식]
// - 위치는 미로 로컬 픽셀 (posX, posY) = 미화원 중심점.
// - 매 프레임:
//     1) 미화원 현재 칸 / 박스 칸 계산
//     2) Maze.nextStepToward() 로 최단 경로의 다음 칸(waypoint) 획득
//     3) waypoint 중심으로 SPEED 만큼 픽셀 이동
//   → 벽을 존중하므로 플레이어가 벽 뒤로 돌면 미화원이 우회 → 거리 벌림 가능.
// - 같은 칸에 들어오면 박스 중심을 직접 추적(미세 접근)해 잡는다.
//
// [속도] 박스 유효 속도(600px/s)의 80% = 480px/s → 잘 움직이면 따돌릴 수 있다.
class TopCleaner(
    private val gctx: GameContext,
    private val maze: Maze,
    private val player: TopPlayer,
) : IGameObject {

    private var posX: Float
    private var posY: Float

    // 박스 셀에 충분히 가까워지면 true. step 7 에서 TopViewScene 이 폴링해 GameOver 연결 예정.
    var isCaught = false
        private set

    private val rect = RectF()
    private val paint = Paint().apply { color = Color.rgb(70, 90, 130) }

    init {
        val tile = TopViewScene.TILE_SIZE
        // 미로 입구 하단 한쪽 끝(col 0, 최하단 row)에서 시작. 박스(col 4)와 가로로 떨어져 출발.
        posX = (START_COL + 0.5f) * tile
        posY = (START_ROW + 0.5f) * tile
    }

    override fun update(gctx: GameContext) {
        if (isCaught) return

        val tile = TopViewScene.TILE_SIZE
        val dt = gctx.frameTime

        val curCol = (posX / tile).toInt()
        val curRow = (posY / tile).toInt()
        val tgtCol = player.chaseTargetCol
        val tgtRow = player.chaseTargetRow

        // 목표 지점(미로 로컬) 결정.
        val aimX: Float
        val aimY: Float
        if (curCol == tgtCol && curRow == tgtRow) {
            // 같은 칸: 박스 중심 직접 추적.
            aimX = (player.visualCol + 0.5f) * tile
            aimY = (player.visualRow + 0.5f) * tile
        } else {
            val next = maze.nextStepToward(curCol, curRow, tgtCol, tgtRow)
            if (next != null) {
                aimX = (next.first + 0.5f) * tile
                aimY = (next.second + 0.5f) * tile
            } else {
                aimX = posX
                aimY = posY
            }
        }

        // 픽셀 이동.
        val dx = aimX - posX
        val dy = aimY - posY
        val dist = hypot(dx, dy)
        val step = SPEED * dt
        if (dist <= step || dist == 0f) {
            posX = aimX
            posY = aimY
        } else {
            posX += dx / dist * step
            posY += dy / dist * step
        }

        // 잡힘 판정: 두 중심 거리 < tile * 0.6.
        val boxCenterX = (player.visualCol + 0.5f) * tile
        val boxCenterY = (player.visualRow + 0.5f) * tile
        if (hypot(posX - boxCenterX, posY - boxCenterY) < tile * CATCH_RATIO) {
            isCaught = true
        }
    }

    override fun draw(canvas: Canvas) {
        val tile = TopViewScene.TILE_SIZE
        // 미로 로컬 → 화면 좌표. Maze.draw 와 동일한 cameraRow 기준 변환.
        val screenX = TopViewScene.MAZE_LEFT + posX
        val screenY = TopViewScene.PLAYER_SCREEN_Y + (posY - maze.cameraRow * tile)

        rect.set(
            screenX - SIZE / 2f,
            screenY - SIZE / 2f,
            screenX + SIZE / 2f,
            screenY + SIZE / 2f,
        )
        canvas.drawRect(rect, paint)
    }

    companion object {
        private const val SPEED = 200f          // 600 * 0.8
        private const val SIZE = 90f            // 박스(80)보다 약간 큼
        private const val CATCH_RATIO = 0.6f

        private const val START_COL = 0
        private val START_ROW = TopViewScene.MAZE_ROWS - 1  // 최하단 row
    }
}