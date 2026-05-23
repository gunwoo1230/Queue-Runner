package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// 매 프레임 박스와 OBSTACLE 레이어 안의 모든 MapObject 를 AABB 로 비교한다.
//
// 책임 분리:
//   - 무엇이 닿았는지 판정       : 여기서.
//   - 어떤 효과가 발동되는지     : MapObject.effect (HitEffect).
//   - 박스가 어떻게 반응할지      : Player.applyHit().
//
// [핵심 동작 규칙]
// 1) player.acceptsInput 이 false 면 검사 자체를 건너뛴다.
//    - 슬로우 1칸 이동 중 / 게임 오버 중 같은 "이미 처리 중" 상황을 의미한다.
//    - 같은 장애물에 매 프레임 또 충돌 판정되는 것을 막는다.
// 2) 점프 중에는 박스 collisionRect 가 공중에 떠 있고 장애물 collisionRect 는 지면 띠라서
//    자연스럽게 무충돌이 된다. 별도 처리가 필요 없다.
// 3) 첫 충돌에서 applyHit() 가 호출되면 즉시 acceptsInput=false 가 되므로,
//    같은 프레임의 후속 장애물에 대한 applyHit 호출은 Player 안의 가드에서 무시된다.
//    (성능을 더 짜내려면 break 흉내를 낼 수도 있지만, 한 화면에 장애물이 많지 않아 이대로 충분.)
//
// [layer 순서]
// MainScene.Layer 에서 PLAYER 다음에 CONTROLLER 를 두므로
// 이 update() 는 player.update() 가 박스 위치/충돌 박스를 갱신한 후에 호출된다.

class CollisionChecker(
    private val world: World<MainScene.Layer>,
    private val player: Player,
) : IGameObject {

    override fun update(gctx: GameContext) {
        // 박스가 이미 충돌 처리 중이면 검사 skip.
        if (!player.acceptsInput) return

        val playerRect = player.collisionRect

        // OBSTACLE 레이어 안의 모든 MapObject 와 AABB 검사.
        // reverse 순회: applyHit 결과로 무언가 layer 에서 제거될 가능성에 대비.
        world.forEachReversedAt(MainScene.Layer.OBSTACLE) { obj ->
            if (obj !is MapObject) return@forEachReversedAt
            if (RectF.intersects(playerRect, obj.collisionRect)) {
                // 첫 호출에서 acceptsInput=false 가 되므로
                // 이후 장애물에 대한 applyHit 호출은 Player 안의 가드에서 무시된다.
                player.applyHit(obj.effect)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        // 시각 표현 없음.
        // 디버그 빌드에서 충돌 박스는 World 가 IBoxCollidable 을 보고 자동으로 그려준다.
    }
}