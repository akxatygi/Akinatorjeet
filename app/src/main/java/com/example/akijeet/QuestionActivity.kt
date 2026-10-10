package com.example.akijeet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.akijeet.data.Question
import com.example.akijeet.game.Answer
import com.example.akijeet.game.GameEngine
import com.example.akijeet.game.GameManager

class QuestionActivity : AppCompatActivity() {

    private lateinit var questionNumberText: TextView
    private lateinit var questionText: TextView
    private lateinit var btnYes: Button
    private lateinit var btnProbably: Button
    private lateinit var btnDontKnow: Button
    private lateinit var btnProbablyNot: Button
    private lateinit var btnNo: Button

    private lateinit var engine: GameEngine
    private var currentQuestion: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        engine = GameManager.getEngine(this)

        questionNumberText = findViewById(R.id.questionNumberText)
        questionText = findViewById(R.id.questionText)
        btnYes = findViewById(R.id.btnYes)
        btnProbably = findViewById(R.id.btnProbably)
        btnDontKnow = findViewById(R.id.btnDontKnow)
        btnProbablyNot = findViewById(R.id.btnProbablyNot)
        btnNo = findViewById(R.id.btnNo)

        setupClickListeners()
        displayNextStep()
    }

    override fun onRestart() {
        super.onRestart()
        displayNextStep()
    }

    private fun setupClickListeners() {
        btnYes.setOnClickListener { handleAnswer(Answer.YES) }
        btnProbably.setOnClickListener { handleAnswer(Answer.PROBABLY) }
        btnDontKnow.setOnClickListener { handleAnswer(Answer.DONT_KNOW) }
        btnProbablyNot.setOnClickListener { handleAnswer(Answer.PROBABLY_NOT) }
        btnNo.setOnClickListener { handleAnswer(Answer.NO) }
    }

    private fun handleAnswer(answer: Answer) {
        val q = currentQuestion ?: return
        engine.answer(q, answer)
        displayNextStep()
    }

    private fun displayNextStep() {
        if (engine.shouldGuess()) {
            val guess = engine.bestGuess()
            if (guess != null) {
                val intent = Intent(this, GuessActivity::class.java).apply {
                    putExtra(GuessActivity.EXTRA_CELEBRITY_NAME, guess.name)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            } else {
                openResultScreen(won = false)
            }
            return
        }

        val nextQ = engine.nextQuestion()
        if (nextQ != null) {
            currentQuestion = nextQ
            questionNumberText.text = "Question #${engine.questionsAsked + 1}"
            questionText.text = nextQ.text
        } else {
            openResultScreen(won = false)
        }
    }

    private fun openResultScreen(won: Boolean) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_WON, won)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
