package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// Side-View 의 정적 NPC. 박스의 도주 경로 끝에 서서
// "더 이상 직진 못 함" 의 시각적 단서가 되어, TopView(미로) 로 우회할 명분을 만든다.
//
// [동작]
// - virtualX = APPEAR_X (= TRANSITION_X + 400) 에 고정.
// - 플레이어가 다가올수록 화면 우측에서부터 점차 들어옴.
//   APPEAR_X(3400) - 1200(화면 우측에 닿는 거리) = 2200 부터 시야에 진입.
//   TRANSITION_X(3000) 에 도달하면 화면 중앙(screenX≈800) 에 위치.
// - 플레이어 virtualX >= TRANSITION_X 이면 MainScene 이 TopViewScene 으로 전환.
//
// [임시 표현]
// - 아직 스프라이트 없음. 회색 사각형 placeholder (Side-View Janitor 의 placeholder 와 톤만 다르게).
class Cleaner(private val player: Player) : IGameObject {

    private val rect = RectF()
    private val paint = Paint().apply { color = Color.rgb(100, 110, 130) }

    override fun update(gctx: GameContext) {
        // 정적 NPC. 위치 변동 없음.
    }

    override fun draw(canvas: Canvas) {
        // 화면상 위치 계산. Player 가 PLAYER_SCREEN_X(=400) 에 고정.
        val screenX = 400f + (APPEAR_X - player.virtualX)

        // 화면 밖이면 그리지 않음 (clipsRect 가 잘라주긴 하지만 비용 절감).
        if (screenX < -200f || screenX > 1800f) return

        // groundY = 700 기준. Player 와 같은 높이대.
        rect.set(screenX - 70f, 540f, screenX + 70f, 700f)
        canvas.drawRect(rect, paint)
    }

    companion object {
        // Side→Top 전환 임계값. MainScene 이 참조.
        const val TRANSITION_X = 3000f
        // Cleaner 가 서 있는 가상 위치. 트리거 지점에서 오른쪽 400 만큼.
        const val APPEAR_X = TRANSITION_X + 400f
    }
}