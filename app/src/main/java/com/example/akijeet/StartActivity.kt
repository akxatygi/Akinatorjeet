package com.example.akijeet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startButton: Button = findViewById(R.id.startButton)
        val targetModeButton: Button = findViewById(R.id.targetModeButton)

        startButton.setOnClickListener {
            val intent = Intent(this, InstructionsActivity::class.java).apply {
                putExtra(InstructionsActivity.EXTRA_IS_TARGET_MODE, false)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        targetModeButton.setOnClickListener {
            val intent = Intent(this, InstructionsActivity::class.java).apply {
                putExtra(InstructionsActivity.EXTRA_IS_TARGET_MODE, true)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}
