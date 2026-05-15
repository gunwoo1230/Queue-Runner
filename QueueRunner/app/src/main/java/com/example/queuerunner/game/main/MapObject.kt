package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// 모든 Side-View 장애물의 공통 부모.
//
// [좌표계]
// Janitor 와 동일하게 자기 자신의 virtualX 를 들고, 화면 X 는
//   PLAYER_SCREEN_X + (virtualX - player.virtualX)
// 로 매 프레임 계산한다.
// 박스가 멈춰 있으면 장애물도 같이 멈춰 보여야 자연스러우므로
// "고정 속도로 흐르는" 방식이 아닌 가상 좌표계 동기화 방식을 쓴다.
//
// [충돌 검사]
// 부모는 isHitByLanding(landingVirtualX) 한 가지 메서드만 노출한다.
// 검사는 점프/슬로우 이동이 시작되기 직전에 한 번 호출되고,
// 실제 효과는 Player 가 착지한 다음 발동된다.
//
// 6 종 지면 장애물이 모두 동일한 "내 X 구간 안에 들어오는가" 검사를 쓰므로,
// 부모에 그 로직을 open 으로 기본 구현해 둔다.
// 미래의 새/박쥐 같은 공중 장애물이 등장하면 이 함수를 override 해서
// false 를 돌려주고, 별도 검사(예: AABB) 를 같은 부모에 추가하는 방향으로 확장한다.
//
// [재활용]
// IRecyclable 을 구현하고 World.obtain() ?: new 패턴으로 만든다.
// init(player, virtualX) 가 "재활용 직후 재초기화" 와 "신규 생성 직후 초기화" 를 모두 책임진다.
//
// [렌더링]
// 7~8주차 sprite 가 들어오기 전까지는 debugColor 단색 사각형으로 그린다.

abstract class MapObject : IGameObject, IRecyclable {

    // 박스 참조. recomputeRect() 가 매 프레임 player.virtualX 를 읽어야 하므로 보관한다.
    // init(player, virtualX) 이전에는 사용해선 안 된다.
    protected lateinit var player: Player
        private set

    // 가상 좌표계상의 왼쪽 끝 X. 자식 검사 함수는 [virtualX, virtualX + width) 를 본다.
    var virtualX: Float = 0f
        private set

    // ===================== 자식이 결정해야 하는 값들 =====================

    // 1 / 2 / 3 칸. 이 값으로 width 가 자동 계산된다.
    abstract val tileCount: Int

    // 시각적 표현용 높이. tileCount 와 다르게 충돌 판정엔 영향이 없고 보기에만 작용한다.
    // sprite 단계에서 종횡비에 맞게 조정될 예정.
    abstract val height: Float

    // 충돌 시 효과. SLOWDOWN 이면 박스가 슬로우 1칸 이동, GAME_OVER 면 게임 종료.
    abstract val effect: HitEffect

    // World 의 어느 layer 에 들어갈지. 거의 다 OBSTACLE 이지만,
    // 미래의 행인이나 새는 다른 layer 를 쓸 수 있도록 열어둔다.
    abstract val layer: MainScene.Layer

    // 임시 디버그 색상. sprite 가 들어오면 사용 안 함.
    protected abstract val debugColor: Int

    // ===================== 부모가 계산하는 값들 =====================

    // 가상 좌표계 / 화면 모두에서 사용할 폭. tileCount * BLOCK_SIZE.
    val width: Float
        get() = tileCount * BLOCK_SIZE

    // 매 프레임 update() 안에서 다시 계산되는 화면 좌표 사각형. draw() 가 그대로 사용.
    protected val rect = RectF()

    // ===================== 재활용 / 초기화 =====================

    // ObjectPool 에서 꺼낼 때나 새로 만들 때 모두 동일하게 호출한다.
    // virtualX 위치만 다시 채우면 부모 입장에선 충분히 초기화된다.
    open fun init(player: Player, virtualX: Float) {
        this.player = player
        this.virtualX = virtualX
        recomputeRect()
    }

    private fun recomputeRect() {
        val screenX = PLAYER_SCREEN_X + (virtualX - player.virtualX)
        rect.set(
            screenX,
            GROUND_Y - height,
            screenX + width,
            GROUND_Y,
        )
    }

    // ===================== 충돌 판정 =====================

    // Player 가 점프 / 슬로우 이동을 시작하기 직전에 호출.
    // landingVirtualX 는 이번 이동이 끝났을 때 박스 발 밑이 어디 있을지 예측한 가상 좌표이다.
    //
    // 기본 구현: 박스 발(점) 이 내 X 구간 [virtualX, virtualX + width) 안에 들어오는가.
    // 새/박쥐 같은 공중 장애물은 이 함수를 override 해서 false 를 돌려주고,
    // 별도의 "비행 중 박스와의 충돌" 검사 함수를 추가할 예정이다.
    open fun isHitByLanding(landingVirtualX: Float): Boolean {
        return landingVirtualX >= virtualX && landingVirtualX < virtualX + width
    }

    // ===================== IGameObject =====================

    override fun update(gctx: GameContext) {
        recomputeRect()

        // 화면 왼쪽 밖으로 완전히 나갔으면 World 에서 제거.
        // World.remove() 가 IRecyclable 을 감지해 자동으로 recycle bin 에 넣어준다.
        if (rect.right < 0f) {
            val scene = gctx.scene as MainScene
            scene.world.remove(this, layer)
        }
    }

    override fun draw(canvas: Canvas) {
        debugPaint.color = debugColor
        canvas.drawRect(rect, debugPaint)
    }

    // ===================== IRecyclable =====================

    override fun onRecycle() {
        // 다음 init(player, virtualX) 에서 모든 상태가 다시 채워지므로 별도 비울 것이 없다.
        // sprite 애니메이션 상태가 생기면 여기서 초기화한다.
    }

    companion object {
        // Player 의 screenX 와 정확히 일치해야 같은 좌표계가 된다.
        const val PLAYER_SCREEN_X = 400f
        const val GROUND_Y = 800f

        // 게임 전체에서 "1칸" 의 길이. Player.blockSize 와 같아야 한다.
        const val BLOCK_SIZE = 200f

        private val debugPaint = Paint().apply {
            style = Paint.Style.FILL
        }
    }
}