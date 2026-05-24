package kr.ac.tukorea.ge.spgp2026.a2dg.objects

import android.graphics.Canvas
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

// x 를 Sprite 중심점이 아니라 "배경이 얼마나 가로로 이동했는가"를 나타내는 스크롤 양처럼 사용하고,
// draw() 에서는 현재 스크롤 위치를 tileWidth 로 나눈 나머지를 기준으로
// 같은 bitmap 을 가로로 여러 번 반복해서 이어 그린다.
//
// [scaleFactor]
// 한 장의 배경을 화면 세로 높이의 몇 배로 확대해 그릴지 결정한다.
// 기본값은 1f 로 기존 동작과 동일하다.
// 2f 면 비트맵이 가로/세로 모두 2 배 크기로 그려지고, 화면 세로의 2 배가 되므로
// 위로 화면 높이만큼 끌어올려 "비트맵의 아래 절반이 화면에 보이도록" 배치한다.
// (시각적으로 카메라가 zoom in 되어 지면에 가까운 부분만 화면에 채워지는 효과)
open class HorzScrollBackground(
    gctx: GameContext,
    resId: Int,
    private val speed: Float,
    private val scaleFactor: Float = 1f,
) : Sprite(gctx, resId) {
    private val screenWidth = gctx.metrics.width
    private val screenHeight = gctx.metrics.height

    // 확대 후의 한 장 폭. scaleFactor = 1f 면 기존 공식 그대로.
    private val tileWidth = bitmapWidth * screenHeight / bitmapHeight.toFloat() * scaleFactor

    // 확대 후의 그리기 높이.
    private val drawHeight = screenHeight * scaleFactor

    // 비트맵의 아래쪽 부분이 화면 영역 (0 ~ screenHeight) 에 보이도록 위로 끌어올린다.
    // scaleFactor = 1f → drawTop = 0 (기존 동작)
    // scaleFactor = 2f → drawTop = -screenHeight (비트맵 윗 절반이 화면 위로 잘려나가고
    //                                              아랫 절반만 화면에 표시됨)
    private val drawTop = screenHeight - drawHeight

    init {
        // 한 장의 배경 이미지는 화면 세로높이에 맞춘 채 원본 비율을 유지한다.
        // 실제 draw() 는 이 크기의 배경 조각을 가로로 반복해서 붙인다.
        setCenterProportionalHeight(screenWidth / 2f, screenHeight / 2f, screenHeight)
    }

    override fun update(gctx: GameContext) {
        // x 값을 중심점이 아니라 누적 스크롤 양으로 사용하므로,
        // 배경 자체를 이동시키지 않고 "어디서부터 반복 배치를 시작할지"만 바꾼다고 보면 된다.
        x += speed * gctx.frameTime
    }

    override fun draw(canvas: Canvas) {
        var curr = x % tileWidth
        if (curr > 0f) curr -= tileWidth
        while (curr < screenWidth) {
            dstRect.set(curr, drawTop, curr + tileWidth, drawTop + drawHeight)
            canvas.drawBitmap(bitmap, null, dstRect, null)
            curr += tileWidth
        }
    }
}