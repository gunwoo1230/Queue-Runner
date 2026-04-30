package com.example.queuerunner.game.main

import android.graphics.Rect
import com.example.queuerunner.R
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.SheetSprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import java.util.*

class Player(gctx: GameContext) : SheetSprite(gctx, R.mipmap.player_sheet, 10f), IBoxCollidable {
    enum class State {
        // 지금은 RUN, JUMP, FALL, DOUBLE_JUMP 네 상태만 두고 시작한다.
        // 이후 Slide, Hit 같은 상태가 늘어나면 이 enum 에 계속 추가할 수 있다.
        STAND, RUN, JUMP, FALL, DOUBLE_JUMP,
    }

    // 1. 커맨드 결과에 따른 이동 타입 정의
    enum class MoveCommand(val distance: Float, val jumpPower: Float) {
        SHORT(100f, 600f),  // O+X, X+O (1칸)
        MEDIUM(200f, 800f), // O+O (2칸)
        LONG(300f, 1000f)   // X+X (3칸)
    }

    // 2. 명령을 저장할 큐 (선입선출)
    private val commandQueue: Queue<MoveCommand> = LinkedList()

    // 이동 물리 변수
    private var velocityY = 0f
    private val gravity = 2500f
    private var groundY = 0f
    private var targetJumpDistance = 0f
    private var currentJumpDistance = 0f

    // 외부(CommandController)에서 완성된 커맨드를 넣어주는 함수
    fun enqueueCommand(commandStr: String) {
        val move = when (commandStr) {
            "OO" -> MoveCommand.MEDIUM
            "XX" -> MoveCommand.LONG
            "OX", "XO" -> MoveCommand.SHORT
            else -> null
        }
        move?.let { commandQueue.add(it) }
    }

    override fun update(gctx: GameContext) {
        val dt = gctx.frameTime

        // 3. 상태(State) 처리: 서 있을 때만 다음 명령을 꺼냄
        if (state == State.STAND && commandQueue.isNotEmpty()) {
            val nextMove = commandQueue.poll()
            startJump(nextMove)
        }

        // 4. 점프/이동 물리 로직
        if (state == State.JUMP || state = State.DOUBLE_JUMP || state == State.FALL) {
            velocityY += gravity * dt
            y += velocityY * dt

            if (y >= groundY) { // 착지 판정
                y = groundY
                velocityY = 0f
                state = State.STAND
            }
        }

        super.update(gctx)
    }

    private fun startMove(move: MoveCommand) {
        state = State.JUMP
        velocityY = -move.jumpPower
        targetJumpDistance = move.distance
        currentJumpDistance = 0f
    }


}