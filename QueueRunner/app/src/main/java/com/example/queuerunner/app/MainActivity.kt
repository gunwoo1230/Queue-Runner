package com.example.queuerunner.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.queuerunner.BuildConfig
import com.example.queuerunner.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 디버그 빌드일 때에는 1초 후 게임 화면으로 바로 넘어가게 한다
        if (BuildConfig.DEBUG) {
            Handler(Looper.getMainLooper()).postDelayed({
                startGameActivity()
            }, 1000)
        }
    }

    private fun startGameActivity() {
        val intent = Intent(this, QueueRunnerActivity::class.java)
        startActivity(intent)
        finish() // 뒤로 가기 시 메인 화면을 거치지 않고 앱 종료
    }
}