package com.example.queuerunner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.queuerunner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
    }

    fun onStartGameClicked(view: View) {
        startGameActivity()
    }

    private fun startGameActivity() {
        Log.d(javaClass.simpleName, "Start Game")
        //val intent = Intent(this, DragonFlightActivity::class.java)
        //startActivity(intent)
    }
}