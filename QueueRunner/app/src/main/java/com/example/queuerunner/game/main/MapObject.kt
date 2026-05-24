package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// 모든 Side-View 장애물의 공통 부모.
//
// [좌표계]
// Janitor 와 동일하게 자기 자신의 virtualX 를 들고, 화면 X 는
//   PLAYER_SCREEN_X + (virtualX - player.virtualX)
// 로 매 프레임 계산한다.
// 박스가 멈춰 있으면 장애물도 같이 멈춰 보이게 만들기 위해 가상 좌표계 동기화 방식을 쓴다.
//
// [충돌 방식 - 얇은 띠]
// 모든 장애물은 IBoxCollidable 을 구현하고 collisionRect 를 가진다.
// 다만 collisionRect 는 두 가지 조정으로 점프 통과를 자연스럽게 허용한다:
//   1) Y 축: 지면에 거의 붙은 얇은 띠. groundY - COLLISION_BAND_HEIGHT ~ groundY.
//      박스가 점프해서 공중에 떠 있을 때는 박스 충돌 박스가 띠보다 위에 있어 안 닿는다.
//      착지하는 순간에만 박스가 띠와 겹친다.
//   2) X 축: 그리기 폭의 80% 만 사용하고 좌우로 10% 씩 비운다.
//      점프 거리가 살짝 부족해서 장애물 끝에 발끝만 살짝 걸치는 경우는
//      충돌로 보지 않게 해 게임 감각을 부드럽게 만든다.
//
// 이 단순한 AABB 방식 덕분에 6주차의 모든 장애물이 동일한 검사 흐름을 탄다.
// 미래에 새 같은 공중 장애물이 추가되어도 collisionRect 의 Y 범위를 점프 높이대에 두면
// 같은 CollisionChecker 로 처리된다.
//
// [재활용]
// IRecyclable 을 구현하고 World.obtain() ?: new 패턴으로 만든다.
// init(player, virtualX) 가 "재활용 직후 재초기화" 와 "신규 생성 직후 초기화" 를 모두 책임진다.
//
// [렌더링]
// 7~8주차 sprite 가 들어오기 전까지는 debugColor 단색 사각형으로 그린다.

abstract class MapObject : IGameObject, IRecyclable, IBoxCollidable {

    // 박스 참조. recomputeRects() 가 매 프레임 player.virtualX 를 읽어야 하므로 보관한다.
    // init(player, virtualX) 이전에는 사용해선 안 된다.
    protected lateinit var player: Player
        private set

    // 가상 좌표계상의 왼쪽 끝 X. width 와 같이 보면 장애물이 차지하는 가상 좌표 구간이 된다.
    var virtualX: Float = 0f
        private set

    // ===================== 자식이 결정해야 하는 값들 =====================

    // 1 / 2 / 3 칸. 이 값으로 width 가 자동 계산된다.
    abstract val tileCount: Int

    // 시각적 표현용 높이. 충돌 판정엔 영향 없음(충돌은 항상 얇은 띠만 본다).
    // sprite 단계에서 종횡비에 맞게 조정될 예정.
    abstract val height: Float

    // 충돌 시 효과. SLOWDOWN 이면 박스가 슬로우 1칸 이동, GAME_OVER 면 게임 종료.
    abstract val effect: HitEffect

    // World 의 어느 layer 에 들어갈지. 거의 다 OBSTACLE.
    abstract val layer: MainScene.Layer

    abstract val mipmapId: Int

    // ===================== 부모가 계산하는 값들 =====================

    // 가상 좌표계 / 화면 모두에서 사용할 폭. tileCount * BLOCK_SIZE.
    val width: Float
        get() = tileCount * BLOCK_SIZE

    // 그리기용 사각형(전체 폭 / 전체 높이). update() 안에서 매 프레임 갱신.
    protected val drawRect = RectF()

    // 충돌용 사각형(폭의 80% / 지면 띠). update() 안에서 매 프레임 갱신.
    private val _collisionRect = RectF()
    override val collisionRect: RectF get() = _collisionRect

    // ===================== 재활용 / 초기화 =====================

    // init() 에서 한 번만 로드하고 재활용. 같은 타입을 풀에서 다시 꺼낼 때도 같은 비트맵을 공유.
    private var bitmap: android.graphics.Bitmap? = null

    // ObjectPool 에서 꺼낼 때나 새로 만들 때 모두 동일하게 호출한다.
    // virtualX 위치만 다시 채우면 부모 입장에선 충분히 초기화된다.
    open fun init(gctx: GameContext, player: Player, virtualX: Float) {
        this.player = player
        this.virtualX = virtualX
        if (bitmap == null) {
            bitmap = gctx.res.getBitmap(mipmapId)
        }
        recomputeRects()
    }

    private fun recomputeRects() {
        val screenX = PLAYER_SCREEN_X + (virtualX - player.virtualX)

        // 그리기용 사각형: 전체 폭, 전체 높이
        drawRect.set(
            screenX,
            GROUND_Y - height,
            screenX + width,
            GROUND_Y,
        )

        // 충돌용 사각형: X 는 그리기 폭의 80% 만큼 중앙 정렬, Y 는 지면 얇은 띠
        val collisionWidth = width * COLLISION_WIDTH_RATIO
        val collisionLeftPad = (width - collisionWidth) * 0.5f
        _collisionRect.set(
            screenX + collisionLeftPad,
            GROUND_Y - COLLISION_BAND_HEIGHT,
            screenX + collisionLeftPad + collisionWidth,
            GROUND_Y,
        )
    }

    // ===================== IGameObject =====================

    override fun update(gctx: GameContext) {
        recomputeRects()

        // 화면 왼쪽 밖으로 완전히 나갔으면 World 에서 제거.
        // World.remove() 가 IRecyclable 을 감지해 자동으로 recycle bin 에 넣어준다.
        if (drawRect.right < 0f) {
            val scene = gctx.scene as MainScene
            scene.world.remove(this, layer)
        }
    }

    override fun draw(canvas: Canvas) {
        bitmap?.let { canvas.drawBitmap(it, null, drawRect, null) }
    }

    // ===================== IRecyclable =====================

    override fun onRecycle() {
        // 다음 init(player, virtualX) 에서 모든 상태가 다시 채워지므로 별도 비울 것이 없다.
    }

    companion object {
        // Player 의 screenX 와 정확히 일치해야 같은 좌표계가 된다.
        const val PLAYER_SCREEN_X = 400f
        // 배경 zoom 적용 후의 새 지면 위치. Player.groundY 와 같아야 한다.
        const val GROUND_Y = 700f

        // 게임 전체에서 "1칸" 의 길이. Player.blockSize 와 같아야 한다.
        const val BLOCK_SIZE = 200f

        // 충돌 띠 두께. O+X 점프(최소 높이 150f) 정점에서도 박스 발이 띠 위에 있도록
        // 30f 정도로 얇게 둔다. 부동소수점 오차로 못 잡는 경우를 피할 만큼은 충분히 두껍다.
        private const val COLLISION_BAND_HEIGHT = 30f

        // 충돌 박스 X 폭 비율 (그리기 폭 대비).
        // 박스가 장애물 끝에 발끝만 살짝 걸치는 경우를 충돌로 보지 않게 해 감각을 부드럽게.
        private const val COLLISION_WIDTH_RATIO = 0.8f


    }
}