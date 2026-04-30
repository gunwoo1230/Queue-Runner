package com.example.queuerunner.game.main

object MapObjectCatalog {
    private var registered = false

    fun registerAll() {
        if (registered) return
        registered = true
    }
}
