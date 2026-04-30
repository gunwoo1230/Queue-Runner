package com.example.queuerunner.game.main

import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

fun interface MapObjectCreator {
    fun create(gctx: GameContext, tile: Char, left: Float, top: Float): MapObject?
}

object MapObjectRegistry {
    private val creators = mutableMapOf<Char, MapObjectCreator>()

    fun register(ch: Char, creator: MapObjectCreator) {
        creators[ch] = creator
    }

    fun register(chars: CharRange, creator: MapObjectCreator) {
        for (ch in chars) {
            creators[ch] = creator
        }
    }

    fun create(gctx: GameContext, tile: Char, left: Float, top: Float): MapObject? {
        return creators[tile]?.create(gctx, tile, left, top)
    }
}

