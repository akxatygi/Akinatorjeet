package com.example.akijeet.game

import android.content.Context
import com.example.akijeet.data.Celebrity
import com.example.akijeet.data.GameData
import com.example.akijeet.data.GameRepository

object GameManager {
    private var data: GameData? = null
    private var engine: GameEngine? = null
    var wrongGuesses = 0

    var isTargetMode: Boolean = false
    var targetCelebrity: Celebrity? = null

    fun initGame(context: Context, targetMode: Boolean = false, target: Celebrity? = null): GameEngine {
        val gameData = data ?: GameRepository.load(context).also { data = it }
        val newEngine = GameEngine(gameData)
        newEngine.reset()
        engine = newEngine
        wrongGuesses = 0
        isTargetMode = targetMode
        targetCelebrity = target
        return newEngine
    }

    fun getEngine(context: Context): GameEngine {
        return engine ?: initGame(context)
    }

    fun getAllCelebrities(context: Context): List<Celebrity> {
        val gameData = data ?: GameRepository.load(context).also { data = it }
        return gameData.celebrities.sortedBy { it.name }
    }

    fun findCelebrityByName(name: String): Celebrity? {
        return data?.celebrities?.firstOrNull { it.name == name }
    }

    fun calculateScore(questionsCount: Int): Int {
        // Score calculation:
        // 5 questions = 100 points
        // 6-15 questions = decreasing points from 95 to 50
        return when {
            questionsCount <= 5 -> 100
            questionsCount >= 15 -> 50
            else -> 100 - ((questionsCount - 5) * 5)
        }
    }

    fun getRatingStars(score: Int): String {
        return when {
            score >= 95 -> "⭐⭐⭐⭐⭐ (Master Mind!)"
            score >= 85 -> "⭐⭐⭐⭐ (Awesome!)"
            score >= 70 -> "⭐⭐⭐ (Great Job!)"
            else -> "⭐⭐ (Good Effort!)"
        }
    }
}
