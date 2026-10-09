package com.example.akijeet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val gameOverTitle: TextView = findViewById(R.id.gameOverTitle)
        val gameOverSubtitle: TextView = findViewById(R.id.gameOverSubtitle)
        val btnPlayAgain: Button = findViewById(R.id.btnPlayAgain)

        val won = intent.getBooleanExtra(EXTRA_WON, false)

        if (won) {
            gameOverTitle.text = "I guessed it!"
            gameOverSubtitle.text = "Thanks for playing!"
        } else {
            gameOverTitle.text = "You beat me!"
            gameOverSubtitle.text = "I couldn't guess who you were thinking of."
        }

        btnPlayAgain.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val EXTRA_WON = "EXTRA_WON"
    }
}
