package com.example.queuerunner.game.main

import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class MainScene(gctx: GameContext) : Scene(gctx) {
    // 게임 내 객체들이 그려질 순서를 Enum으로 정의 (뒤쪽이 맨 위에 그려짐)
    enum class Layer {
        BG, FLOOR, ITEM, OBSTACLE, PLAYER, CONTROLLER, UI
    }

    override val world = World(Layer.values())
    override val clipsRect = true

    init {
        // 1~3주차 목표 완료! 현재는 빈 씬입니다.
        // 바로 다음 단계(4주차)에서 이 곳에 CommandController(버튼 UI)와 Player를 추가할 예정입니다.
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 추후 UI 버튼(O, X) 터치 이벤트를 처리할 예정입니다.
        return super.onTouchEvent(event)
    }
}