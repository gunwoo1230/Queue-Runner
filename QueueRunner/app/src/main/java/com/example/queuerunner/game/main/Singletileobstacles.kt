package com.example.queuerunner.game.main

import android.graphics.Color

// 1칸 짜리 장애물 3종.
//
// README 기준 "O+X 1칸 이동만으로는 통과 불가" 인 장애물이다.
// O+X 점프(거리 1칸)로 시도하면 정확히 장애물 X 구간 안에 떨어져 SLOWDOWN 이 발동된다.
// O+O(2칸) 이상의 점프로만 안전하게 넘길 수 있다.
//
// 세 클래스 모두 tileCount = 1, effect = SLOWDOWN, layer = OBSTACLE 로 동일하고
// 시각적 구분을 위해 height 와 debugColor 만 다르다.
// 7~8주차에 sprite 가 들어오면 debugColor 자리에 비트맵이 들어가도록 교체된다.

// 종량제 쓰레기봉투. 둥글둥글한 인상이라 살짝 낮고 노란 톤.
class TrashBag : MapObject() {
    override val tileCount = 1
    override val height = 140f
    override val effect = HitEffect.SLOWDOWN
    override val layer = MainScene.Layer.OBSTACLE
    override val debugColor: Int = Color.rgb(210, 190, 60)   // 노랑
}

// 음식물 쓰레기통. 통 모양이라 좀 더 높고 갈색 톤.
class FoodWaste : MapObject() {
    override val tileCount = 1
    override val height = 180f
    override val effect = HitEffect.SLOWDOWN
    override val layer = MainScene.Layer.OBSTACLE
    override val debugColor: Int = Color.rgb(140, 90, 40)    // 갈색
}

// 고양이. 가장 낮고 회색 톤. 살아있는 장애물이라는 점은 sprite 단계에서 살린다.
class Cat : MapObject() {
    override val tileCount = 1
    override val height = 110f
    override val effect = HitEffect.SLOWDOWN
    override val layer = MainScene.Layer.OBSTACLE
    override val debugColor: Int = Color.rgb(120, 120, 130)  // 회색
}