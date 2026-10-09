package com.example.akijeet.game

import android.content.Context
import com.example.akijeet.data.Celebrity
import com.example.akijeet.data.GameData
import com.example.akijeet.data.GameRepository

object GameManager {
    private var data: GameData? = null
    private var engine: GameEngine? = null
    var wrongGuesses = 0

    fun initGame(context: Context): GameEngine {
        val gameData = data ?: GameRepository.load(context).also { data = it }
        val newEngine = GameEngine(gameData)
        newEngine.reset()
        engine = newEngine
        wrongGuesses = 0
        return newEngine
    }

    fun getEngine(context: Context): GameEngine {
        return engine ?: initGame(context)
    }

    fun findCelebrityByName(name: String): Celebrity? {
        return data?.celebrities?.firstOrNull { it.name == name }
    }
}
