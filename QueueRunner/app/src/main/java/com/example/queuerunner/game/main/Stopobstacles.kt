package com.example.queuerunner.game.main

import android.graphics.Color

// 못 지나가는(통과 시 무조건 GAME_OVER) 장애물 모음.
//
// 지금은 Car 한 종만 들어가 있지만, 추후 Truck / Bus / 컨테이너 같은
// "박스가 점프로 절대 못 넘는 큰 사물" 들이 추가되면 같은 파일에 묶는다.
//
// 공통 특징:
// - tileCount 가 3 이상이라 X+X(3칸 점프) 로도 안전하게 못 넘는다.
//   (정확히 3칸 폭일 때만 끝 경계에 착지하면 통과 가능하지만, 실전 정밀도로는 사실상 불가)
// - effect = GAME_OVER : 부딪히면 즉시 GameOverScene 으로 간다.

// 횡단보도 자동차. 박스가 점프 거리상 절대 못 넘는 폭.
// 시각적으로도 차체 검정으로 위협감을 준다.
class Car : MapObject() {
    override val tileCount = 3
    override val height = 360f
    override val effect = HitEffect.GAME_OVER
    override val layer = MainScene.Layer.OBSTACLE
    override val debugColor: Int = Color.rgb(30, 30, 30)   // 검정 (자동차 차체)
}