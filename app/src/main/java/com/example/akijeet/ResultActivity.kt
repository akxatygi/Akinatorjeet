package com.example.akijeet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.akijeet.game.GameManager
import com.google.android.material.card.MaterialCardView

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val gameOverTitle: TextView = findViewById(R.id.gameOverTitle)
        val gameOverSubtitle: TextView = findViewById(R.id.gameOverSubtitle)
        val scoreCard: MaterialCardView = findViewById(R.id.scoreCard)
        val scoreText: TextView = findViewById(R.id.scoreText)
        val questionsCountText: TextView = findViewById(R.id.questionsCountText)
        val ratingText: TextView = findViewById(R.id.ratingText)
        val btnPlayAgain: Button = findViewById(R.id.btnPlayAgain)

        val won = intent.getBooleanExtra(EXTRA_WON, false)
        val isTargetMode = GameManager.isTargetMode
        val questionsAsked = GameManager.getEngine(this).questionsAsked
        val targetCeleb = GameManager.targetCelebrity

        if (won) {
            gameOverTitle.text = "AKIJEET Guessed It!"
            if (isTargetMode && targetCeleb != null) {
                gameOverSubtitle.text = "Target: ${targetCeleb.name}"
                val score = GameManager.calculateScore(questionsAsked)
                val stars = GameManager.getRatingStars(score)

                scoreText.text = "Score: $score / 100"
                questionsCountText.text = "Guessed in $questionsAsked questions!"
                ratingText.text = stars
                scoreCard.visibility = View.VISIBLE
            } else {
                gameOverSubtitle.text = "Thanks for playing!"
                scoreCard.visibility = View.GONE
            }
        } else {
            gameOverTitle.text = "You Beat AKIJEET!"
            if (isTargetMode && targetCeleb != null) {
                gameOverSubtitle.text = "Couldn't guess ${targetCeleb.name}!"
            } else {
                gameOverSubtitle.text = "I couldn't guess who you were thinking of."
            }
            scoreCard.visibility = View.GONE
        }

        btnPlayAgain.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }

    companion object {
        const val EXTRA_WON = "EXTRA_WON"
    }
}
