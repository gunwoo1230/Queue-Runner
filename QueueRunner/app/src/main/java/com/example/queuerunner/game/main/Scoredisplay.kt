package com.example.queuerunner.game.main

import android.graphics.Canvas
import androidx.core.graphics.withTranslation
import com.example.queuerunner.R
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.ImageNumber
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// GameSession.score 를 이미지 숫자(number_24x32 시트)로 그리는 HUD.
//
// 숫자 렌더링은 우리 프로젝트의 공용 ImageNumber 를 그대로 쓴다.
// ImageNumber 는 "오른쪽 정렬"만 지원하므로(right 기준 좌측으로 그림),
// 가운데 정렬이 필요하면 현재 숫자 폭의 절반만큼 캔버스를 평행이동해 중앙에 오게 한다.
//
// 매 프레임 GameSession.score 를 value 로 동기화하고,
// ImageNumber 내부 애니메이션(displayValue 가 value 를 천천히 따라감)으로 숫자가 부드럽게 오른다.
//
// Side ↔ Top 전환 시 각 Scene 이 새 ScoreDisplay 를 만들지만,
// init 에서 setValueImmediately(GameSession.score) 를 호출하므로
// 누적 점수가 0 부터 다시 오르지 않고 곧바로 이어져 보인다.
class ScoreDisplay(
    gctx: GameContext,
    private val align: Align,
    private val anchorX: Float,
    top: Float,
    private val charWidth: Float,
) : IGameObject {
    enum class Align { RIGHT, CENTER }

    // 내부 숫자는 right = 0 기준으로 그려서 [-N*charWidth, 0] 영역을 차지하게 둔다.
    // 실제 화면 위치는 draw() 에서 정렬에 맞춰 평행이동으로 맞춘다.
    private val number = ImageNumber(
        gctx = gctx,
        mipmapId = R.mipmap.number_24x32,
        right = 0f,
        top = top,
        dstCharWidth = charWidth,
    )

    init {
        number.setValueImmediately(GameSession.score)
    }

    override fun update(gctx: GameContext) {
        number.value = GameSession.score
        number.update(gctx)
    }

    override fun draw(canvas: Canvas) {
        val shiftX = when (align) {
            Align.RIGHT -> anchorX                          // 오른쪽 끝 = anchorX
            Align.CENTER -> anchorX + currentWidth() / 2f   // 가운데 = anchorX
        }
        canvas.withTranslation(shiftX, 0f) {
            number.draw(this)
        }
    }

    // 현재 표시값의 자리수 × 글자폭.
    // (애니메이션 중에는 value 기준이라 실제 그려지는 displayValue 와 1프레임 정도 오차가 날 수 있으나
    //  정지하면 정확히 가운데에 온다.)
    private fun currentWidth(): Float {
        var v = number.value
        if (v < 0) v = 0
        var digits = 1
        while (v >= 10) {
            v /= 10
            digits++
        }
        return digits * charWidth
    }
}