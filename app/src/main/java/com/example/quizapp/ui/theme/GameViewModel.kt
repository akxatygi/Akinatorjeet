package com.example.quizapp.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.quizapp.data.Celebrity
import com.example.quizapp.data.GameRepository
import com.example.quizapp.data.Question
import com.example.quizapp.game.Answer
import com.example.quizapp.game.GameEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface GameUiState {
    data object Start : GameUiState
    data class Asking(val question: Question, val number: Int) : GameUiState
    data class Guessing(val celebrity: Celebrity) : GameUiState
    data object Won : GameUiState
    data object Lost : GameUiState
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = GameEngine(GameRepository.load(application))
    private var wrongGuesses = 0

    private val _state = MutableStateFlow<GameUiState>(GameUiState.Start)
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    fun startGame() {
        engine.reset()
        wrongGuesses = 0
        advance()
    }

    fun answer(answer: Answer) {
        val current = _state.value as? GameUiState.Asking ?: return
        engine.answer(current.question, answer)
        advance()
    }

    fun confirmGuess(correct: Boolean) {
        val current = _state.value as? GameUiState.Guessing ?: return
        if (correct) {
            _state.value = GameUiState.Won
        } else {
            engine.rejectGuess(current.celebrity)
            wrongGuesses++
            if (wrongGuesses >= MAX_WRONG_GUESSES) _state.value = GameUiState.Lost else advance()
        }
    }

    /** Decide what the player sees next: a guess, a question, or game over. */
    private fun advance() {
        if (engine.shouldGuess()) {
            val guess = engine.bestGuess()
            _state.value = if (guess != null) GameUiState.Guessing(guess) else GameUiState.Lost
        } else {
            val question = engine.nextQuestion()
            _state.value = if (question != null) {
                GameUiState.Asking(question, engine.questionsAsked + 1)
            } else {
                GameUiState.Lost
            }
        }
    }

    companion object {
        const val MAX_WRONG_GUESSES = 3
    }
}