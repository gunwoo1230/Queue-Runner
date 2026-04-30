package com.example.queuerunner.game.main

import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.collections.remove

abstract class MapObject(
    gctx: GameContext,
    resId: Int,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
) : Sprite(gctx, resId), IRecyclable, IBoxCollidable {
    abstract val layer: MainScene.Layer

    init {
        dstRect.set(left, top, left + width, top + height)

        this.width = width
        this.height = height
    }

    override val collisionRect: RectF
        get() = dstRect

    fun setLeftTop(left: Float, top: Float) {
        dstRect.offsetTo(left, top)
    }

    override fun update(gctx: GameContext) {
        val dx = SPEED * gctx.frameTime
        dstRect.offset(dx, 0f)
        if (dstRect.right < 0f) {
            val scene = gctx.scene as MainScene
            scene.world.remove(this, layer)
        }
    }

    override fun onRecycle() {
    }
    companion object {
        const val SPEED = -300f
    }
}
