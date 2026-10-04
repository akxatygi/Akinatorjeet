package com.example.quizapp

import com.example.quizapp.data.GameData
import com.example.quizapp.game.Answer
import com.example.quizapp.game.GameEngine
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GameEngineTest {

    private val data: GameData = File("src/main/assets/celebrities.json")
        .reader().use { Gson().fromJson(it, GameData::class.java) }

    @Test
    fun engineGuessesEveryCelebrityWhenAnswersAreTruthful() {
        for (celebrity in data.celebrities) {
            val engine = GameEngine(data)
            while (!engine.shouldGuess()) {
                val question = engine.nextQuestion()!!
                val answer = when (celebrity.answers[question.id])
                {
                    1 -> Answer.YES
                    -1 -> Answer.NO
                    else -> Answer.DONT_KNOW
                }
                engine.answer(question, answer)
            }
            assertEquals(celebrity.name, engine.bestGuess()?.name)
            assertTrue("Too many questions for ${celebrity.name}", engine.questionsAsked <= 15)
        }
    }
}
