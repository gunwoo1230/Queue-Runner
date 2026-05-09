package com.example.queuerunner.game.main

import android.util.Log
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.HorzScrollBackground
import com.example.queuerunner.R


class MainScene(gctx: GameContext) : Scene(gctx) {
    enum class Layer {
        BG, FLOOR, ITEM, OBSTACLE, PLAYER, UI
    }
    override val clipsRect = true

    init {
        // 스테이지를 만들면 사용
        // MapObjectCatalog.registerAll()
    }

    val player = Player(gctx)

    // 관리사무소 아저씨. player 의 virtualX 를 읽어 거리를 계산하므로 player 다음에 만든다.
    val janitor = Janitor(gctx, player)
    private val controller = CommandController(gctx, player)

    // 쿠키런 배경 사용 -> 후에 수정
    private val backgrounds = listOf(
        HorzScrollBackground(gctx, R.mipmap.gurubox_bg, 0f) to 0.3f,
        HorzScrollBackground(gctx, R.mipmap.gurubox_bg_middle, 0f) to 1.0f,
    )

    // player.virtualX 의 이전 프레임 값 : 매 프레임 delta 를 구하기 위해 사용
    private var prevVirtualX = 0f

    private var gameOverHandled = false
    override val world = World(Layer.entries.toTypedArray()).apply {
        backgrounds.forEach { (bg, _) -> add(bg, Layer.BG) }

        // TODO: CollisionChecker, MapLoader 구현 후 주석 해제
        // add(CollisionChecker(this, player), Layer.CONTROLLER)
        // add(MapLoader(gctx, this, stage), Layer.CONTROLLER)

        add(player, Layer.PLAYER)
        add(janitor, Layer.PLAYER)
        add(controller, Layer.UI)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (controller.onTouchEvent(event)) return true
        return super.onTouchEvent(event)
    }

    override fun update(gctx: GameContext) {
        // 1. 먼저 world 전체(player 포함)를 update 해서 player.virtualX 를 최신화
        super.update(gctx)

        // 2. 추격자가 박스를 잡았는지 한 번만 확인.
        //    추후 GameOverScene 이 만들어지면 Log 대신 sceneStack.change(GameOverScene(gctx)) 등으로 바꾼다.
        if (janitor.isCaught && !gameOverHandled) {
            gameOverHandled = true
            Log.d(
                javaClass.simpleName,
                "GAME OVER: chaser caught the box at player.virtualX=${player.virtualX}"
            )
            // TODO: gctx.sceneStack.change(GameOverScene(gctx))
        }

        // 2. 이번 프레임에 player 가 이동한 가상 거리 계산
        val delta = player.virtualX - prevVirtualX
        prevVirtualX = player.virtualX

        // 3. 배경마다 시차 비율만큼 스크롤
        //    HorzScrollBackground 는 x 가 커질수록 오른쪽으로 밀리므로 -delta 를 더한다.
        backgrounds.forEach { (bg, parallax) ->
            bg.x -= delta * parallax
        }
    }
}