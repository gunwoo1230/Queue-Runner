package com.example.queuerunner.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.queuerunner.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // "구르기" 버튼을 누르면 게임 화면으로 이동
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            startGameActivity()
        }
    }

    private fun startGameActivity() {
        val intent = Intent(this, QueueRunnerActivity::class.java)
        startActivity(intent)
        finish() // 뒤로 가기 시 메인 화면을 거치지 않고 앱 종료
    }
}