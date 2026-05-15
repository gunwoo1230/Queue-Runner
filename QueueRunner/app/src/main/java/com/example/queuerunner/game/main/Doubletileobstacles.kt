package com.example.queuerunner.game.main

import android.graphics.Color

// 2칸 짜리 장애물 2종.
//
// README 기준 X+X(3칸 점프) 로만 안전하게 넘길 수 있는 장애물이다.
// O+O(2칸 점프) 로는 두 번째 칸에 정확히 떨어져 충돌이 발생한다.
//
// 같은 2칸 폭이지만 효과가 다르다는 점이 중요하다:
// - 웅덩이(Puddle) : SLOWDOWN. 빠지면 발이 젖어 두 칸 동안 천천히 미끄러져 빠져나온다.
//   2칸 폭이므로 첫 칸에서 슬로우 1번, 두 번째 칸에서 또 슬로우 1번 → 총 2번 슬로우.
// - 맨홀(Manhole)  : GAME_OVER. 박스가 빠져서 더 이상 움직일 수 없게 된다.
//
// 두 클래스 모두 tileCount = 2, layer = OBSTACLE 로 동일하고
// effect, height, debugColor 만 다르다.

// 빗물 웅덩이. 바닥에 깔리는 느낌이라 매우 낮고 푸른 톤.
class Puddle : MapObject() {
    override val tileCount = 2
    override val height = 40f
    override val effect = HitEffect.SLOWDOWN
    override val layer = MainScene.Layer.OBSTACLE
    override val debugColor: Int = Color.rgb(70, 120, 180)   // 파랑
}

// 하수구 맨홀. 빠지면 끝. 검은 구멍 같은 톤.
// 시각적으로도 "빠질 만한 구멍" 으로 읽혀야 하므로 height 는 낮게.
class Manhole : MapObject() {
    override val tileCount = 2
    override val height = 30f
    override val effect = HitEffect.GAME_OVER
    override val layer = MainScene.Layer.OBSTACLE
    override val debugColor: Int = Color.rgb(20, 20, 20)     // 거의 검정 (구멍)
}