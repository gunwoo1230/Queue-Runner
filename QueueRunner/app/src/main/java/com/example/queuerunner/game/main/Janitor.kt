package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.RectF
import com.example.queuerunner.R
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// 관리사무소 아저씨 (Janitor)
//
// Side-View 단계에서 박스를 화면 왼쪽으로부터 추격하는 캐릭터이다.
// README 의 추격 메커닉은 "박스가 장애물에 걸려 느려지면 거리가 줄어들고,
// 거리가 0 이 되면 게임 오버" 이므로, 아저씨 자체는 일정한 속도로만 전진하고
// 거리 관리는 player.virtualX 와의 차이로만 계산한다.
//
// 위치 표현 방식:
// - Player 는 항상 screenX = 400f 에 그려지고, 대신 virtualX 가 늘어난다.
// - Janitor 도 자신의 virtualX 를 들고 있고,
//   화면상 X 위치는 박스와의 가상 거리만큼 왼쪽으로 떨어뜨려 그린다.
//   즉, Janitor_screenX = playerScreenX - (player.virtualX - Janitor.virtualX)
// - 박스가 멀리 도망갈수록 아저씨가 화면 왼쪽으로 멀어지고,
//   박스가 멈춰 있으면 아저씨가 점점 박스에 가까워진다.
//
// 시작 시점에는 박스보다 INITIAL_DISTANCE 만큼 뒤에 있도록 virtualX 를 음수로 시작한다.
// SPEED 는 가상 좌표계 기준 1초당 전진 거리이다. Player 의 점프 평균 속도보다 충분히
// 작아야, 정상적으로 입력을 잘 한 플레이어가 천천히 거리를 벌릴 수 있다.

class Janitor(
    private val gctx: GameContext,
    private val player: Player,
) : IGameObject, IBoxCollidable {

    // 박스와 아저씨가 그려지는 화면상 기준 좌표.
    // Player 의 screenX, groundY 와 같은 값을 써서 두 캐릭터가 같은 지면 위에 있도록 맞춘다.
    private val playerScreenX = 400f
    private val groundY = 700f

    // 아저씨가 가상 좌표계에서 얼마나 전진했는지를 들고 있는 값이다.
    // 시작 시점에는 박스(virtualX = 0)보다 INITIAL_DISTANCE 만큼 뒤에 있어야 하므로
    // 음수 -INITIAL_DISTANCE 로 시작한다.
    var virtualX = -INITIAL_DISTANCE
        private set

    // 화면 그리기 도구. 임시로 파란 사각형을 사용하고, 추후 스프라이트로 교체한다.
    private val bitmap = gctx.res.getBitmap(R.mipmap.gurubox_janitor)
    private val rect = RectF()

    // IBoxCollidable: 충돌 검사용 사각형으로 그림용 rect 를 그대로 재사용한다.
    override val collisionRect: RectF get() = rect

    // 박스와 아저씨 사이의 가상 거리.
    // 양수이면 박스가 앞에 있고, 0 이하이면 잡힌 상태이다.
    // 매 프레임 player.virtualX 와 자신의 virtualX 만 보면 되므로 별도 저장은 하지 않는다.
    val distance: Float
        get() = player.virtualX - virtualX

    // 한 번 true 가 되면 다시 false 로 돌아오지 않게 latch 한다.
    // 그렇지 않으면 잡힌 직후에 player 가 다시 점프해서 distance 가 0 보다 커지는 순간
    // isCaught 가 false 가 되어 "게임 오버 처리"가 풀리는 이상한 상황이 생긴다.
    var isCaught = false
        private set

    override fun update(gctx: GameContext) {
        // 한 번 잡혔다면 더 이상 전진하지 않고 멈춘다.
        // 게임 오버 이후 화면 위에 그대로 남아 있어도 이상하지 않다.
        if (isCaught) return

        // 박스 입력과 무관하게 일정한 속도로 천천히 전진한다.
        virtualX += SPEED * gctx.frameTime

        // 거리가 0 이하로 떨어지면 잡힌 것으로 latch.
        if (distance <= 0f) {
            isCaught = true
        }
    }

    override fun draw(canvas: Canvas) {
        // 박스 screenX 는 playerScreenX 로 고정돼 있으므로,
        // 아저씨 screenX 는 그 위치에서 distance 만큼 왼쪽으로 떨어진 점이 된다.
        // distance 가 0 에 가까워질수록 아저씨가 박스 위에 겹쳐지듯 다가온다.
        val screenX = playerScreenX - distance
        rect.set(
            screenX - HALF_WIDTH,
            groundY - HEIGHT,
            screenX + HALF_WIDTH,
            groundY,
        )
        canvas.drawBitmap(bitmap, null, rect, null)
    }

    companion object {
        // 가상 좌표계 기준 아저씨가 1초에 전진하는 거리.
        // ToDo: 밸런스를 위해 나중에 조절
        const val SPEED = 400f

        // 시작 시점에 박스와 아저씨 사이의 가상 거리.
        // ToDo: 추후 시작 지점 만들면 (분리수거장) 변경
        const val INITIAL_DISTANCE = 700f

        // 임시 사각형 크기. 추후 sprite 로 교체될 때 다시 정한다.
        // Player 가 80x80 사각형이라 그보다 약간 더 크게 그려 위협적인 느낌만 잡아둔다.
        private const val HALF_WIDTH = 150f
        private const val HEIGHT = 360f
    }
}