package com.example.queuerunner.game.main

import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.HorzScrollBackground
import com.example.queuerunner.BuildConfig
import com.example.queuerunner.R

// 디버그 전환 확인용
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class MainScene(gctx: GameContext) : Scene(gctx) {
    // 6주차에서 CONTROLLER 레이어가 추가됨.
    // CONTROLLER 는 시각 요소가 없고 update() 로 게임 로직만 돌리는 객체용 레이어이다.
    // 순서 결정:
    //   BG → FLOOR → ITEM → OBSTACLE → PLAYER → CONTROLLER → UI
    // - OBSTACLE 이 PLAYER 보다 먼저: 장애물 위치(virtualX 기반 화면좌표) 가 갱신된 뒤
    //   PLAYER 가 자기 점프/슬로우/충돌박스를 갱신한다.
    // - CONTROLLER 가 PLAYER 보다 뒤: 박스가 이번 프레임 위치를 확정한 후
    //   CollisionChecker 가 AABB 검사를 수행한다.
    // - UI 는 항상 마지막에 그려진다(CommandController, ProgressGauge, ScoreDisplay 등).
    enum class Layer {
        BG, FLOOR, ITEM, OBSTACLE, PLAYER, CONTROLLER, UI
    }
    override val clipsRect = true

    init {
        // 6주차부터 6종 장애물(T/F/C/P/M/A) 을 MapObjectRegistry 에 등록한다.
        // 두 번 호출돼도 내부 플래그로 한 번만 동작한다.
        MapObjectCatalog.registerAll()
    }

    val player = Player(gctx)

    // 관리사무소 아저씨. player 의 virtualX 를 읽어 거리를 계산하므로 player 다음에 만든다.
    val janitor = Janitor(gctx, player)
    val cleaner = Cleaner(gctx, player)
    private val controller = CommandController(gctx, player)

    // 상단 진행률 게이지 (Side-View 전용). player/janitor 의 virtualX 를 읽으므로 둘 다음에 만든다.
    private val progressGauge = ProgressGauge(player, janitor)

    // 점수판 (이미지 숫자). Side-View 는 오른쪽 위.
    private val scoreDisplay = ScoreDisplay(
        gctx,
        align = ScoreDisplay.Align.RIGHT,
        anchorX = 1560f,
        top = 28f,
        charWidth = 40f,
    )

    // 배경 (시각 차원에서 일단 가지고 있는 임시 리소스).
    // scaleFactor = 2f 를 주면 비트맵이 2 배 크기로 그려지며 아래 절반만 화면에 표시된다.
    // -> 카메라가 zoom in 된 듯한 시각 효과 + Player/MapObject 의 groundY 도 700 으로 맞춤.
    private val backgrounds = listOf(
        HorzScrollBackground(gctx, R.mipmap.gurubox_bg, 0f, scaleFactor = 2f) to 0.3f,
        HorzScrollBackground(gctx, R.mipmap.gurubox_bg_middle, 0f, scaleFactor = 2f) to 1.0f,
    )

    // player.virtualX 의 이전 프레임 값. 매 프레임 delta 를 구해 배경 스크롤 / 점수에 반영.
    // 시작값을 player 의 시작 virtualX 로 맞춰, 첫 프레임에 시작 오프셋(100)이 통째로
    // delta 로 잡혀 점수/배경이 튀는 것을 막는다.
    private var prevVirtualX = player.virtualX

    // 1점 미만 누적분. 정수 점수만 GameSession 에 더하고 나머지는 여기 보관한다.
    private var scoreCarry = 0f

    // isCaught / isGameOver 를 한 번만 처리하고 GameOverScene 을 push 하기 위한 플래그.
    private var gameOverHandled = false

    // 디버그 확인용 (배포 빌드에서는 그리지도, 반응하지도 않음 — 아래 BuildConfig.DEBUG 게이트)
    private val debugTopViewRect = RectF(1400f, 0f, 1600f, 100f)
    private val debugPaint = Paint().apply { color = Color.MAGENTA }

    override val world = World(Layer.entries.toTypedArray()).apply {
        backgrounds.forEach { (bg, _) -> add(bg, Layer.BG) }

        add(cleaner, Layer.PLAYER)
        add(player, Layer.PLAYER)
        add(janitor, Layer.PLAYER)
        add(controller, Layer.UI)
        add(progressGauge, Layer.UI)
        add(scoreDisplay, Layer.UI)
    }

    init {
        // ObstacleSpawner / CollisionChecker 는 world 가 생성된 뒤에 등록한다.
        // (생성자 안에서 world 를 참조하므로 위 apply 블록 안에 두면 forward reference 문제가 생긴다.)
        // 같은 CONTROLLER 레이어 안에서는 add 순서대로 update 가 호출되므로
        // ObstacleSpawner → CollisionChecker 순서.
        // 갓 spawn 된 장애물은 화면 오른쪽 바깥에 있어 박스와 닿지 않으므로
        // 같은 프레임에 즉시 충돌 처리되는 일은 없다.
        world.add(ObstacleSpawner(player), Layer.CONTROLLER)
        world.add(CollisionChecker(world, player), Layer.CONTROLLER)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 디버그 빌드에서만: 우상단을 눌러 즉시 Top-View 로 점프 (개발 편의용).
        // 배포 빌드에서는 player.virtualX >= Cleaner.TRANSITION_X 자동 트리거로만 전환된다.
        if (BuildConfig.DEBUG && event.action == MotionEvent.ACTION_DOWN) {
            val pt = gctx.metrics.fromScreen(event.x, event.y)
            if (debugTopViewRect.contains(pt.x, pt.y)) {
                TopViewScene(gctx).change()
                return true
            }
        }

        if (controller.onTouchEvent(event)) return true
        return super.onTouchEvent(event)
    }

    override fun update(gctx: GameContext) {
        // 1. world 전체(player 포함) 를 update 해서 모든 위치/상태를 최신화.
        super.update(gctx)

        // 2. 이번 프레임 박스가 전진한 가상 거리.
        val delta = player.virtualX - prevVirtualX
        prevVirtualX = player.virtualX

        // 3. 점수: Side-View 는 전진 거리에 1:1 로 누적. (1점 미만은 carry 로 보관해 손실 최소화)
        if (delta > 0f) {
            scoreCarry += delta
            val gained = scoreCarry.toInt()
            if (gained > 0) {
                GameSession.score += gained
                scoreCarry -= gained
            }
        }

        // 4. 게임 오버 트리거 확인.
        //    - 추격자에게 잡힘 (janitor.isCaught)
        //    - 장애물 GAME_OVER (player.isGameOver) -- 2칸 맨홀 / 3칸 자동차 충돌
        if (!gameOverHandled && (janitor.isCaught || player.isGameOver)) {
            gameOverHandled = true
            GameOverScene(gctx).push()
        }

        // 5. Side → Top 전환.
        if (!gameOverHandled && player.virtualX >= Cleaner.TRANSITION_X) {
            TopViewScene(gctx).change()
            return
        }

        // 6. 배경마다 시차 비율만큼 스크롤 (같은 delta 재사용).
        //    HorzScrollBackground 는 x 가 커질수록 오른쪽으로 밀리므로 -delta 를 더한다.
        backgrounds.forEach { (bg, parallax) ->
            bg.x -= delta * parallax
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        // 디버그 빌드에서만 마젠타 점프 버튼을 표시.
        if (BuildConfig.DEBUG) {
            canvas.drawRect(debugTopViewRect, debugPaint)
        }
    }
}