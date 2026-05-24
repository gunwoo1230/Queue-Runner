package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// Top-View 미로. 데이터 + 렌더링 + 셀 조회 책임만 가진다.
//
// [좌표계 — Y 축 스크롤]
// - cameraRow 가 화면상 PLAYER_SCREEN_Y 에 그려지는 row.
// - row 가 cameraRow 보다 작으면 화면 위쪽, 크면 화면 아래쪽에 그려진다.
// - cameraRow 는 Float 라 step 3 의 칸 사이 보간 이동에서 자연스럽게 부드러운 스크롤이 된다.
//
// [데이터 규칙]
// - 각 String 이 정확히 MAZE_COLS(5) 글자여야 한다.
// - '#' 벽, '.' 통로, 'S' 시작, 'E' 출구.
// - S/E 는 통과 가능. 의미만 다르고 충돌 판정은 '.' 와 동일.
class Maze : IGameObject {

    private val data = arrayOf(
        "#.E.#",  // row 0
        "#.#.#",  // row 1
        "#...#",  // row 2
        "###.#",  // row 3
        "#...#",  // row 4
        "#.###",  // row 5
        "#...#",  // row 6
        "###.#",  // row 7
        "#...#",  // row 8
        "#.#.#",  // row 9
        "#...#",  // row 10
        "###.#",  // row 11
        "#...#",  // row 12
        "#.###",  // row 13
        "#.S.#",  // row 14
    )

    val rows: Int get() = data.size
    val cols: Int = TopViewScene.MAZE_COLS

    // 화면 PLAYER_SCREEN_Y 에 그려질 row. step 3 에서 player.cellRow 와 동기화.
    var cameraRow: Float = (data.size - 1).toFloat()

    // ===================== 외부 조회 =====================

    fun isWall(col: Int, row: Int): Boolean {
        if (row !in data.indices) return true
        val rowStr = data[row]
        if (col !in 0 until cols) return true
        return rowStr[col] == '#'
    }

    fun getStartCell(): Pair<Int, Int> = findCell('S') ?: (2 to data.size - 1)
    fun getExitCell(): Pair<Int, Int> = findCell('E') ?: (2 to 0)

    private fun findCell(target: Char): Pair<Int, Int>? {
        for (row in data.indices) {
            val col = data[row].indexOf(target)
            if (col >= 0) return col to row
        }
        return null
    }

    // ===================== IGameObject =====================

    override fun update(gctx: GameContext) {
        // step 3 에서 cameraRow = player.cellRow 동기화 추가 예정.
    }

    override fun draw(canvas: Canvas) {
        val tile = TopViewScene.TILE_SIZE
        val left0 = TopViewScene.MAZE_LEFT
        val anchorY = TopViewScene.PLAYER_SCREEN_Y

        // 화면 밖에 있는 row 는 그릴 필요 없다 (시야 컬링).
        // anchorY 위쪽으로 보일 수 있는 row 개수 + 1 만큼 위에서 시작.
        val firstRow = (cameraRow - anchorY / tile - 1).toInt().coerceAtLeast(0)
        val lastRow = (cameraRow + (900f - anchorY) / tile + 1).toInt().coerceAtMost(data.size - 1)

        for (row in firstRow..lastRow) {
            val rowStr = data[row]
            val y = anchorY + (row - cameraRow) * tile
            for (col in 0 until cols) {
                val x = left0 + col * tile
                val ch = rowStr[col]
                val paint = when (ch) {
                    '#' -> wallPaint
                    'E' -> exitPaint
                    'S' -> startPaint
                    else -> pathPaint
                }
                canvas.drawRect(x, y, x + tile, y + tile, paint)
            }
        }
    }

    companion object {
        private val wallPaint = Paint().apply { color = Color.rgb(60, 50, 40) }
        private val pathPaint = Paint().apply { color = Color.rgb(140, 130, 110) }
        private val exitPaint = Paint().apply { color = Color.rgb(80, 220, 100) }
        private val startPaint = Paint().apply { color = Color.rgb(220, 200, 80) }
    }
}