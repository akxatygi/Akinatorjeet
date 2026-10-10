package com.example.akijeet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.akijeet.game.GameManager

class GuessActivity : AppCompatActivity() {

    private lateinit var guessCelebrityName: TextView
    private lateinit var btnGuessYes: Button
    private lateinit var btnGuessNo: Button

    private var celebrityName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess)

        celebrityName = intent.getStringExtra(EXTRA_CELEBRITY_NAME)

        guessCelebrityName = findViewById(R.id.guessCelebrityName)
        btnGuessYes = findViewById(R.id.btnGuessYes)
        btnGuessNo = findViewById(R.id.btnGuessNo)

        guessCelebrityName.text = celebrityName ?: "Unknown"

        btnGuessYes.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra(ResultActivity.EXTRA_WON, true)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        btnGuessNo.setOnClickListener {
            val name = celebrityName
            if (name != null) {
                val celebrity = GameManager.findCelebrityByName(name)
                if (celebrity != null) {
                    GameManager.getEngine(this).rejectGuess(celebrity)
                }
            }
            GameManager.wrongGuesses++
            if (GameManager.wrongGuesses >= 3) {
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra(ResultActivity.EXTRA_WON, false)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            } else {
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }

    companion object {
        const val EXTRA_CELEBRITY_NAME = "EXTRA_CELEBRITY_NAME"
    }
}
