package com.example.queuerunner.game.main

// 타일 문자 → 장애물 생성 규칙 카탈로그.
//
// MapObjectRegistry 는 (gctx, tile, left, top) 이라는 일반 시그니처를 그대로 둔다.
// 우리 게임은 가상 좌표계 + 지면 고정이므로 다음과 같이 해석한다:
//   - left → virtualX (가상 좌표계상 왼쪽 끝)
//   - top  → 사용 안 함 (모든 장애물이 GROUND_Y 기준이므로)
// 7주차 이후 Top-View 격자 맵이 추가되면 top 자리가 다시 의미를 갖게 된다.
//
// 각 creator 가 하는 일은 모두 동일하다:
//   1) (gctx.scene as MainScene) 에서 world / player 참조를 꺼낸다.
//   2) world.obtain(타입::class.java) 로 풀에서 같은 타입을 재활용 시도.
//   3) 없으면 new.
//   4) init(player, virtualX) 로 위치만 다시 채운다.
//
// 이 패턴 덕분에 같은 타입의 장애물이 화면 밖으로 나갔다가 다시 spawn 될 때
// 객체를 한 번도 새로 만들지 않을 수 있다.
//
// 타일 문자 매핑:
//   T - TrashBag   (1칸, SLOWDOWN)
//   F - FoodWaste  (1칸, SLOWDOWN)
//   C - Cat        (1칸, SLOWDOWN)
//   P - Puddle     (2칸, SLOWDOWN)
//   M - Manhole    (2칸, GAME_OVER)
//   A - Car        (3칸, GAME_OVER)

object MapObjectCatalog {
    private var registered = false

    fun registerAll() {
        if (registered) return
        registered = true

        // ---------- 1칸 ----------
        MapObjectRegistry.register('T') { gctx, _, virtualX, _ ->
            val scene = gctx.scene as MainScene
            (scene.world.obtain(TrashBag::class.java) ?: TrashBag())
                .also { it.init(gctx, scene.player, virtualX) }
        }
        MapObjectRegistry.register('F') { gctx, _, virtualX, _ ->
            val scene = gctx.scene as MainScene
            (scene.world.obtain(FoodWaste::class.java) ?: FoodWaste())
                .also { it.init(gctx, scene.player, virtualX) }
        }
        MapObjectRegistry.register('C') { gctx, _, virtualX, _ ->
            val scene = gctx.scene as MainScene
            (scene.world.obtain(Cat::class.java) ?: Cat())
                .also { it.init(gctx, scene.player, virtualX) }
        }

        // ---------- 2칸 ----------
        MapObjectRegistry.register('P') { gctx, _, virtualX, _ ->
            val scene = gctx.scene as MainScene
            (scene.world.obtain(Puddle::class.java) ?: Puddle())
                .also { it.init(gctx, scene.player, virtualX) }
        }
        MapObjectRegistry.register('M') { gctx, _, virtualX, _ ->
            val scene = gctx.scene as MainScene
            (scene.world.obtain(Manhole::class.java) ?: Manhole())
                .also { it.init(gctx, scene.player, virtualX) }
        }

        // ---------- 3칸 ----------
        MapObjectRegistry.register('A') { gctx, _, virtualX, _ ->
            val scene = gctx.scene as MainScene
            (scene.world.obtain(Car::class.java) ?: Car())
                .also { it.init(gctx, scene.player, virtualX) }
        }
    }

    // 7단계 ObstacleSpawner 에서 무작위 선택할 때 사용할 6종 타일 문자 목록.
    // CharArray 로 두면 random() 시 박싱(boxing) 이 일어나지 않는다.
    val ALL_TILES: CharArray = charArrayOf('T', 'F', 'C', 'P', 'M', /*'A'*/)
}