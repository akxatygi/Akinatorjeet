package com.example.akijeet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.akijeet.game.GameManager

class InstructionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        val instructionsTitle: TextView = findViewById(R.id.instructionsTitle)
        val instructionsText: TextView = findViewById(R.id.instructionsText)
        val btnContinueToGame: Button = findViewById(R.id.btnContinueToGame)

        val isTargetMode = intent.getBooleanExtra(EXTRA_IS_TARGET_MODE, false)

        if (isTargetMode) {
            instructionsTitle.text = "TARGET CHALLENGE"
            instructionsText.text = "1. Select a target Bollywood celebrity from the list on the next screen.\n\n2. Answer the questions for that celebrity truthfully.\n\n3. Try to get a high score (up to 100 points) based on how quickly AKIJEET identifies your target star!"
            btnContinueToGame.text = "SELECT CELEBRITY"

            btnContinueToGame.setOnClickListener {
                val intent = Intent(this, SelectCelebActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        } else {
            instructionsTitle.text = "CLASSIC MODE"
            instructionsText.text = "1. Think of any famous Bollywood celebrity in your head.\n\n2. Answer each question truthfully using Yes, No, Probably, or Don't Know.\n\n3. AKIJEET will try to read your mind and guess who you are thinking of in 15 questions or fewer!"
            btnContinueToGame.text = "START GAME"

            btnContinueToGame.setOnClickListener {
                GameManager.initGame(this, targetMode = false)
                val intent = Intent(this, QuestionActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_IS_TARGET_MODE = "EXTRA_IS_TARGET_MODE"
    }
}
