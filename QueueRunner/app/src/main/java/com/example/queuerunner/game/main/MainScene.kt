package com.example.queuerunner.game.main

import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.HorzScrollBackground
import com.example.queuerunner.R


class MainScene(gctx: GameContext) : Scene(gctx) {
    enum class Layer {
        // OBSTACLE 은 FLOOR/ITEM 보다 앞에, PLAYER 보다 뒤에 둔다.
        // 이렇게 하면 장애물이 바닥과 아이템 위에 보이면서도,
        // 플레이어가 장애물에 가려지지 않아 충돌 상황을 확인하기 쉽다.
        BG, FLOOR, ITEM, OBSTACLE, PLAYER, UI
    }
    override val clipsRect = true

    init {
        // MapLoader 는 stage 문자를 MapObjectRegistry 를 통해 객체로 바꾼다.
        // 따라서 MapLoader 가 생성되기 전에, 이 게임에서 사용할 문자별 생성 규칙을 등록해 둔다.
        // registerAll() 은 MainScene 이 다시 만들어져도 중복 등록되지 않도록 내부에서 한 번만 실행된다.
        MapObjectCatalog.registerAll()
    }

    val player = Player(gctx)
    private val controller = CommandController(gctx)
    override val world = World(Layer.entries.toTypedArray()).apply {
        listOf(
            R.mipmap.cookie_run_bg_1 to -50f,
            R.mipmap.cookie_run_bg_2 to -100f,
            R.mipmap.cookie_run_bg_3 to -150f,
        ).forEach { (resId, speed) ->
            // 같은 코드 패턴으로 배경을 추가하므로 유지보수가 쉽다.
            // 배경 장수를 늘릴 때는 위 리스트에 항목만 추가하면 된다.
            add(HorzScrollBackground(gctx, resId, speed), com.example.queuerunner.game.main.MainScene.Layer.BG)
        }

        add(CollisionChecker(this, player), com.example.queuerunner.game.main.MainScene.Layer.CONTROLLER)
        add(MapLoader(gctx, this, stage), com.example.queuerunner.game.main.MainScene.Layer.CONTROLLER)

        // 플레이어는 배경보다 앞 레이어에 배치한다.
        add(player, com.example.queuerunner.game.main.MainScene.Layer.PLAYER)

        add(controller, Layer.UI)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (controller.onTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)

        // 캐릭터가 이동 중일 때 배경도 그만큼 더 빨리 움직이게 처리 가능
        // background.speed = 기본속도 + (player.state == JUMP 일 때 추가 속도)
    }
}