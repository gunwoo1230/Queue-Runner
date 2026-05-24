package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import java.util.LinkedList
import java.util.Queue

// Top-View 박스. Side-View 의 Player 와 완전히 별개 클래스.
//
// [좌표]
// - cellCol/cellRow : 정수 셀 좌표. 정지 상태일 때만 의미가 있다.
// - displayCol/displayRow : 보간 중 부동소수 위치. 그리기 / 카메라 동기화는 이쪽을 사용.
//
// [이동]
// - TopCommandController 가 enqueueCombo("OO" / "OX" / "XO" / "XX") 호출.
// - update() 가 큐를 하나씩 꺼내 startMove() → 보간 → 정수 셀 Snap.
// - 한 칸 이동에 MOVE_DURATION 초 걸린다.
//
// [벽 충돌]
// - 이동 시작 시점에 maze.isWall(target) 확인. 벽이면 그 콤보는 무시 (큐에서는 빠짐).
//
// [카메라]
// - 매 프레임 update() 끝에 maze.cameraRow = displayRow 동기화.
//   Maze.update() 보다 PLAYER 레이어가 뒤에 오므로 같은 프레임 안에서 깔끔히 반영됨.
class TopPlayer(
    private val gctx: GameContext,
    private val maze: Maze,
) : IGameObject {

    // 정지 시 정수 셀 좌표.
    var cellCol: Int = 0
        private set
    var cellRow: Int = 0
        private set

    // 보간 중 부동소수 좌표. 그리기 / 카메라용.
    private var displayCol: Float = 0f
    private var displayRow: Float = 0f

    // 이동 상태.
    private var isMoving = false
    private var moveStartCol = 0f
    private var moveStartRow = 0f
    private var moveTargetCol = 0
    private var moveTargetRow = 0
    private var moveProgress = 0f

    // 콤보 큐.
    private val comboQueue: Queue<String> = LinkedList()

    var acceptsInput = true
        private set

    private val rect = RectF()
    private val paint = Paint().apply { color = Color.rgb(180, 130, 70) }  // 박스 톤

    init {
        // 미로의 'S' 셀에서 시작. Maze 가 먼저 생성되어 있어야 함.
        val (startCol, startRow) = maze.getStartCell()
        cellCol = startCol
        cellRow = startRow
        displayCol = startCol.toFloat()
        displayRow = startRow.toFloat()
        // 카메라 초기 동기화. 안 맞추면 시작 직후 한 프레임 동안 카메라가 튕김.
        maze.cameraRow = startRow.toFloat()
    }

    // ===================== 외부 인터페이스 =====================

    fun enqueueCombo(combo: String) {
        if (!acceptsInput) return
        comboQueue.add(combo)
    }

    // 점프 중에도 큐에 1 개까지 선입력 허용 (Side-View 와 동일 패턴).
    fun canAcceptCommand(): Boolean = acceptsInput && (!isMoving || comboQueue.isEmpty())

    // ===================== 상태 전이 =====================

    private fun startMove(combo: String) {
        // README 의 Top-View 이동 규칙:
        //   OO  -> 오 (col+1)
        //   OX / XO -> 위 (row-1)
        //   XX  -> 왼 (col-1)
        //   ※ 아래 이동 없음
        val (dCol, dRow) = when (combo) {
            "OO" -> 1 to 0
            "OX", "XO" -> 0 to -1
            "XX" -> -1 to 0
            else -> return
        }

        val newCol = cellCol + dCol
        val newRow = cellRow + dRow

        // 벽이면 그 콤보는 그냥 소비되고 이동은 일어나지 않는다.
        if (maze.isWall(newCol, newRow)) return

        moveStartCol = cellCol.toFloat()
        moveStartRow = cellRow.toFloat()
        moveTargetCol = newCol
        moveTargetRow = newRow
        moveProgress = 0f
        isMoving = true
    }

    // ===================== IGameObject =====================

    override fun update(gctx: GameContext) {
        val dt = gctx.frameTime

        if (isMoving) {
            moveProgress = (moveProgress + dt / MOVE_DURATION).coerceAtMost(1f)
            displayCol = moveStartCol + (moveTargetCol - moveStartCol) * moveProgress
            displayRow = moveStartRow + (moveTargetRow - moveStartRow) * moveProgress

            if (moveProgress >= 1f) {
                cellCol = moveTargetCol
                cellRow = moveTargetRow
                displayCol = cellCol.toFloat()
                displayRow = cellRow.toFloat()
                isMoving = false
            }
        }

        // 대기 중이고 큐에 콤보가 있으면 다음 이동 시작.
        if (!isMoving && comboQueue.isNotEmpty()) {
            startMove(comboQueue.poll()!!)
        }

        // 카메라 동기화. 매 프레임 displayRow 를 따라가도록.
        maze.cameraRow = displayRow
    }

    override fun draw(canvas: Canvas) {
        val tile = TopViewScene.TILE_SIZE
        val cellLeft = TopViewScene.MAZE_LEFT + displayCol * tile
        // displayRow == maze.cameraRow 이므로 화면 Y 는 항상 PLAYER_SCREEN_Y 근처.
        val cellTop = TopViewScene.PLAYER_SCREEN_Y + (displayRow - maze.cameraRow) * tile

        // 셀 안에 약간 패딩 두고 그리기.
        val pad = (tile - PLAYER_SIZE) / 2f
        rect.set(
            cellLeft + pad,
            cellTop + pad,
            cellLeft + pad + PLAYER_SIZE,
            cellTop + pad + PLAYER_SIZE,
        )
        canvas.drawRect(rect, paint)
    }

    companion object {
        // 한 칸 이동에 걸리는 시간. Side-View 의 O+X 점프(0.4초) 와 비슷한 페이스.
        private const val MOVE_DURATION = 0.3f

        // 셀(180) 보다 약간 작아서 박스 사이에 시각적 간격이 보이게.
        private const val PLAYER_SIZE = 140f
    }
}