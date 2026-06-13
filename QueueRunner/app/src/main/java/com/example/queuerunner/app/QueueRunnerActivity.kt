package com.example.queuerunner.app

import kr.ac.tukorea.ge.spgp2026.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import com.example.queuerunner.BuildConfig
import com.example.queuerunner.game.main.GameSession
import com.example.queuerunner.game.main.MainScene

class QueueRunnerActivity : BaseGameActivity() {
    // 디버그 정보(FPS, 격자, 충돌 박스) 표시 여부
    override val drawsDebugGrid: Boolean = BuildConfig.DEBUG
    override val drawsDebugInfo: Boolean = BuildConfig.DEBUG
    override val drawsFpsGraph: Boolean = BuildConfig.DEBUG

    override fun createRootScene(gctx: GameContext): Scene {
        // QueueRunner는 쿠키런처럼 횡스크롤 게임이므로 1600 x 900 비율 사용
        gctx.metrics.setSize(1600f, 900f)

        // 앱이 새로 시작될 때(첫 실행 / Exit 후 프로세스가 살아있는 채 재실행 등)
        // 누적 점수를 0 으로 초기화한다. Side↔Top 전환은 change() 라 여기를 거치지 않으므로
        // 사이클 중에는 점수가 그대로 유지된다.
        GameSession.reset()

        return MainScene(gctx)
    }
}