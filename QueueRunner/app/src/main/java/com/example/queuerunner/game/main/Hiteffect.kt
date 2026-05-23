package com.example.queuerunner.game.main

// 박스가 장애물과 충돌했을 때 어떤 효과가 발동할지를 나타내는 enum.
//
// 충돌 검사 흐름:
//   1) 모든 장애물은 IBoxCollidable 의 collisionRect 를 가진다.
//      - Y 축: 지면에 거의 붙은 얇은 띠(점프 중에는 절대 닿지 않을 만큼 얇음).
//      - X 축: 그리기 폭의 80% 만 차지(끝부분에 발끝만 걸치는 경우는 무시).
//   2) CollisionChecker 가 매 프레임 박스 collisionRect 와 AABB 검사.
//   3) 박스가 공중에 떠 있는 동안은 자연스럽게 무충돌이고, 착지하는 순간에만 닿는다.
//   4) 닿은 직후 박스가 이 HitEffect 에 따라 분기 처리한다.
//
// SLOWDOWN
//   1칸 장애물(쓰레기봉투/음식물통/고양이) 과 2칸 웅덩이에 적용.
//   박스가 입력을 받지 않는 슬로우 모드로 진입해
//   y 고정인 채 x 만 1칸(BLOCK_SIZE) 천천히 미끄러진다.
//   슬로우 이동도 일종의 커맨드 발동으로 취급하므로 이동 중에는 새 충돌 검사가 일어나지 않고,
//   슬로우가 끝나는 시점에 비로소 다시 검사가 시작된다.
//   (그 다음 검사 결과 또 SLOWDOWN 이면 또 슬로우 1칸, 없으면 입력 재개)
//
// GAME_OVER
//   2칸 맨홀과 3칸 자동차에 적용.
//   박스가 빠지거나 찌그러져 더 이상 움직일 수 없는 상태가 되었음을 의미한다.
//   MainScene 이 이 효과를 감지하면 Janitor.isCaught 와 동일한 경로로 GameOverScene 을 push 한다.
//
// 추후 NONE 같은 무해 효과(행인 등) 가 필요해지면 여기에 추가한다.
enum class HitEffect {
    SLOWDOWN,
    GAME_OVER,
}