package com.example.queuerunner.game.main

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.util.LabelUtil
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// GameOverScene 은 MainScene 위에 얇게 올려 두는 overlay scene 이다.
//
// isTransparent = true 이므로 GameView 가 이 scene 아래의 MainScene 도 함께 그려준다.
// 그래서 박스, 추격자, 배경은 잡혔을 때의 마지막 모습 그대로 화면에 남고,
// 그 위에 어두운 막과 GAME OVER 글자, Restart / Exit 버튼이 얹힌다.
//
// SceneStack 은 top scene 에만 update 와 touch 를 보내므로,
// 이 scene 이 push 되는 순간 MainScene 의 update 는 자연스럽게 멈춘다.
// 별도 freeze 플래그 없이도 박스, 추격자, 배경이 모두 정지한 것처럼 보이는 이유이다.
//
// Restart 와 Exit 의 차이는 finishesActivity 인자로 갈린다.
// Restart 는 popAll(false) 로 stack 만 비우고 새 MainScene 을 push,
// Exit 는 popAll(true) 로 onEmptyStack callback 까지 이어져 Activity.finish() 가 호출된다.
class GameOverScene(gctx: GameContext) : Scene(gctx) {
    override val isTransparent = true

    // 화면 전체를 덮는 어두운 막.
    // alpha 값이 180/255 정도라 아래 MainScene 의 마지막 프레임이 어슴푸레 비친다.
    private val dimPaint = Paint().apply { color = Color.argb(180, 0, 0, 0) }

    // 버튼 채우기 색과 테두리 색.
    private val buttonFillPaint = Paint().apply { color = Color.rgb(60, 60, 80) }
    private val buttonStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
    }

    // 글자들. CommandController 가 LabelUtil 을 쓰는 패턴을 따라 같은 도구를 사용한다.
    private val titleLabel = LabelUtil(160f, Color.WHITE, Paint.Align.CENTER, Typeface.DEFAULT_BOLD)
    private val subtitleLabel = LabelUtil(60f, Color.LTGRAY, Paint.Align.CENTER)
    private val buttonLabel = LabelUtil(70f, Color.WHITE, Paint.Align.CENTER)

    // 1600 x 900 가상 좌표계 기준 버튼 위치.
    // 버튼 두 개를 가로로 나란히 두고, 그 위에 GAME OVER 타이틀을 둔다.
    // 손가락 탭 영역으로 너무 작지 않도록 폭 400, 높이 150 정도의 큰 박스로 잡았다.
    private val restartRect = RectF(380f, 550f, 780f, 700f)
    private val exitRect = RectF(820f, 550f, 1220f, 700f)

    // 일반 Scene 은 world 가 있어 super.draw() 가 그쪽으로 위임하지만,
    // GameOverScene 은 별도의 world 없이 직접 캔버스에 그리므로 draw() 를 통째로 override 한다.
    override fun draw(canvas: Canvas) {
        // 1) 어두운 막을 화면 전체에 깐다.
        canvas.drawRect(gctx.metrics.borderRect, dimPaint)

        // 2) 타이틀과 서브타이틀.
        //    LabelUtil 의 draw 는 Canvas.drawText 와 같은 baseline 기준이라
        //    y 값을 글자 크기의 약 70~80% 만큼 아래로 내려야 시각적으로 가운데처럼 보인다.
        titleLabel.draw(canvas, "GAME OVER", 800f, 360f)
        subtitleLabel.draw(canvas, "박스가 잡혔습니다", 800f, 440f)

        // 3) Restart / Exit 버튼 배경과 텍스트.
        drawButton(canvas, restartRect, "Restart")
        drawButton(canvas, exitRect, "Exit")
    }

    private fun drawButton(canvas: Canvas, rect: RectF, text: String) {
        canvas.drawRoundRect(rect, 30f, 30f, buttonFillPaint)
        canvas.drawRoundRect(rect, 30f, 30f, buttonStrokePaint)
        // text baseline 보정용 +25f.
        // 버튼 높이 150 / 글자 크기 70 정도 기준에서 가운데로 보이게 맞춘 값이다.
        buttonLabel.draw(canvas, text, rect.centerX(), rect.centerY() + 25f)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // ACTION_DOWN 한 번에 바로 반응하는 단순 버튼 처리.
        // 손가락이 버튼 밖으로 나가도 ACTION_UP 에서 취소되는 식의 정교한 처리는 일단 생략.
        if (event.actionMasked != MotionEvent.ACTION_DOWN) return false

        val pt = gctx.metrics.fromScreen(event.x, event.y)
        if (restartRect.contains(pt.x, pt.y)) {
            restart()
            return true
        }
        if (exitRect.contains(pt.x, pt.y)) {
            exit()
            return true
        }
        return false
    }

    // 게임 오버 상태에서 뒤로 가기 버튼을 누르면 게임을 다시 시작하기보다
    // 종료에 더 가까운 의미이므로 Exit 와 같은 처리로 둔다.
    override fun onBackPressed(): Boolean {
        exit()
        return true
    }

    private fun restart() {
        // 현재 stack: [MainScene_old, GameOverScene]
        // 단순히 GameOverScene 만 pop 하면 caught 상태인 MainScene_old 가 그대로 살아 있어
        // 다시 시작하려면 별도의 reset 로직을 MainScene 에 만들어야 한다.
        // 그 대신 stack 을 통째로 비우고 새 MainScene 을 push 하면, 모든 상태가 깔끔하게 초기화된다.
        // popAll(false) 는 onEmptyStack callback 을 호출하지 않으므로 Activity 가 종료되지 않고,
        // 바로 이어서 push 한 새 MainScene 이 stack 의 유일한 scene 이 된다.
        gctx.sceneStack.popAll(finishesActivity = false)
        gctx.sceneStack.push(MainScene(gctx))
    }

    private fun exit() {
        // popAll(true) 는 onEmptyStack callback 을 호출해 Activity.finish() 까지 이어진다.
        // GameView.init 블록에서 onEmptyStack = { activity?.finish() } 가 등록되어 있어
        // 별도 Activity 참조 없이도 앱 종료 흐름이 만들어진다.
        gctx.sceneStack.popAll(finishesActivity = true)
    }
}