package com.example.queuerunner.game.main

// Scene 사이를 가로질러 보존되는 게임 세션 상태.
//
// MainScene → TopViewScene → MainScene 으로 change() 가 반복될 때
// 각 Scene 인스턴스는 매번 새로 만들어지지만 GameSession 은 살아남아
// 누적 점수, 총 도주 거리, 사이클 카운트 등을 보관한다.
//
// 새 게임 시작 (GameOverScene 의 Restart) 에서만 reset() 한다.
object GameSession {
    var score: Int = 0
    var totalDistance: Float = 0f
    var cycleCount: Int = 0   // 몇 번째 Side ↔ Top 왕복인지

    fun reset() {
        score = 0
        totalDistance = 0f
        cycleCount = 0
    }
}