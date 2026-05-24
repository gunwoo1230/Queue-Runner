package com.example.queuerunner.game.main

import android.graphics.Canvas
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.random.Random

// 박스(Player) 가 가상 좌표계상 일정 거리 전진할 때마다 화면 오른쪽 바깥에
// 새 장애물 한 개씩을 무작위로 생성한다.
//
// [동작 흐름]
// 1. nextSpawnVirtualX : "다음 장애물을 둘 가상 좌표계상의 X" 를 보관.
// 2. 매 프레임 player.virtualX 를 보고, "박스의 virtualX + SPAWN_TRIGGER_OFFSET" 까지
//    spawn 위치가 들어왔는지 검사한다.
// 3. 들어왔다면 ALL_TILES 중 하나를 무작위로 골라 MapObjectRegistry.create() 로 만들고
//    MainScene.world 의 OBSTACLE 레이어에 추가한다.
//    (MapObjectCatalog 에 등록된 creator 가 ObjectPool 에서 재사용을 시도한다.)
// 4. 방금 만든 장애물의 tileCount 에 따라 다음 간격(빈칸 수) 을 결정해 nextSpawnVirtualX 를 민다.
//
// [최소 간격 규칙 - A안]
//   1칸 장애물 → 다음 빈칸 최소 2칸
//   2칸 장애물 → 다음 빈칸 최소 1칸
//   3칸 장애물 → 다음 빈칸 최소 3칸 (잠깐 쉬는 타이밍)
// 위 최소 위에 0..2 칸을 랜덤으로 추가해 리듬에 변화를 준다.
//
// [한계]
// A안 규칙은 일부 조합(예: "1칸 → 2빈 → 3칸 자동차") 에서 사실상 클리어가 어렵다.
// 6주차에서는 단순함을 우선하고, 7주차 플레이테스트 후 필요하면 추가 제약을 도입한다.

class ObstacleSpawner(
    private val player: Player,
) : IGameObject {

    private var nextSpawnVirtualX = INITIAL_SPAWN_VIRTUAL_X

    override fun update(gctx: GameContext) {
        // 한 프레임에 여러 장애물이 한 번에 들어와야 할 경우(예: 큰 dt 점프) 도 대응할 수 있도록 while.
        while (player.virtualX + SPAWN_TRIGGER_OFFSET >= nextSpawnVirtualX) {
            val spawned = spawnOne(gctx, nextSpawnVirtualX)

            if (spawned != null) {
                val minGapTiles = when (spawned.tileCount) {
                    1 -> 3
                    2 -> 3
                    3 -> 3
                    else -> 4   // 안전 디폴트
                }
                val extraGapTiles = Random.nextInt(EXTRA_GAP_RANGE_EXCL)  // 0..(N-1)
                val totalGapTiles = minGapTiles + extraGapTiles

                val obstacleEnd = spawned.virtualX + spawned.width
                nextSpawnVirtualX = obstacleEnd + totalGapTiles * MapObject.BLOCK_SIZE
            } else {
                // catalog 에 등록되지 않은 타일이 ALL_TILES 에 섞이면 spawn 이 실패할 수 있다.
                // 무한 루프 방지를 위해 충분한 거리만큼 다음 spawn 위치를 뒤로 민다.
                nextSpawnVirtualX += 1000f
            }
        }
    }

    private fun spawnOne(gctx: GameContext, virtualX: Float): MapObject? {
        val tile = MapObjectCatalog.ALL_TILES.random()
        // MapObjectRegistry 시그니처는 (gctx, tile, left, top). 우리 게임은
        // left → virtualX, top → 사용 안 함 (모두 지면 기준) 으로 해석한다.
        val obstacle = MapObjectRegistry.create(gctx, tile, virtualX, 0f) ?: return null

        val scene = gctx.scene as MainScene
        scene.world.add(obstacle, obstacle.layer)
        return obstacle
    }

    override fun draw(canvas: Canvas) {
        // 시각 표현 없음.
    }

    companion object {
        // 첫 장애물 위치. 게임 시작 직후 화면 오른쪽 바깥에서 등장.
        // 화면 폭 1600, 박스 screenX = 400 이므로 박스 기준 오른쪽 끝까지 1200f.
        // 1600f 로 두면 초반 박스가 어느 정도 전진한 후에야 트리거된다.
        private const val INITIAL_SPAWN_VIRTUAL_X = 1600f

        // "박스의 virtualX + 이 값" 까지의 spawn 위치는 미리 만들어둔다.
        // 화면 오른쪽 끝(박스 기준 1200f) 보다 200f 더 멀리(1400f) 로 두면
        // 장애물이 화면 밖에서 자연스럽게 들어오기 시작한다.
        private const val SPAWN_TRIGGER_OFFSET = 1400f

        // 최소 간격 위에 0..(EXTRA_GAP_RANGE_EXCL - 1) 칸을 랜덤으로 추가.
        // 3 -> 0, 1, 2 중 하나가 더해지므로 최대 +2 칸까지 변동.
        private const val EXTRA_GAP_RANGE_EXCL = 3
    }
}