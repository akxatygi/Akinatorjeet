package com.example.quizapp.game

import com.example.quizapp.data.Celebrity
import com.example.quizapp.data.GameData
import com.example.quizapp.data.Question
import kotlin.math.abs

class GameEngine(private val data: GameData) {

    private val scores = mutableMapOf<String, Double>()
    private val askedIds = mutableSetOf<String>()
    private val rejectedNames = mutableSetOf<String>()

    var questionsAsked = 0
        private set

    init { reset() }

    fun reset() {
        scores.clear()
        data.celebrities.forEach { scores[it.name] = 0.0 }
        askedIds.clear()
        rejectedNames.clear()
        questionsAsked = 0
    }


    fun answer(question: Question, answer: Answer) {
        askedIds += question.id
        questionsAsked++
        for (celebrity in data.celebrities) {
            val fact = celebrity.answers[question.id] ?: 0   // 1 yes, -1 no, 0 unsure
            scores[celebrity.name] = scores.getValue(celebrity.name) + answer.weight * fact
        }
    }


    private fun ranked(): List<Celebrity> =
        data.celebrities
            .filter { it.name !in rejectedNames }
            .sortedByDescending { scores.getValue(it.name) }

    /** The question that splits the remaining candidates most evenly (null if none helps). */
    fun nextQuestion(): Question? {
        val ranked = ranked()
        if (ranked.isEmpty()) return null

        val top = scores.getValue(ranked.first().name)
        val candidates = ranked.filter { scores.getValue(it.name) >= top - MARGIN }

        var best: Question? = null
        var bestCost = Double.MAX_VALUE
        for (q in data.questions) {
            if (q.id in askedIds) continue
            val yes = candidates.count { (it.answers[q.id] ?: 0) == 1 }
            val no = candidates.count { (it.answers[q.id] ?: 0) == -1 }
            if (yes == 0 || no == 0) continue            // does not separate anyone
            val unknown = candidates.size - yes - no
            val cost = abs(yes - no) + 0.5 * unknown      // lower = better split
            if (cost < bestCost) { bestCost = cost; best = q }
        }
        return best
    }

    fun shouldGuess(): Boolean {
        val ranked = ranked()
        if (ranked.isEmpty()) return false
        if (nextQuestion() == null) return true           // nothing useful left to ask
        if (questionsAsked >= MAX_QUESTIONS) return true
        if (questionsAsked < MIN_QUESTIONS) return false
        if (ranked.size == 1) return true
        val lead = scores.getValue(ranked[0].name) - scores.getValue(ranked[1].name)
        return lead >= LEAD_TO_GUESS
    }

    fun bestGuess(): Celebrity? = ranked().firstOrNull()

    /** Call when the player says "No, that's not who I'm thinking of". */
    fun rejectGuess(celebrity: Celebrity) { rejectedNames += celebrity.name }

    companion object {
        const val MARGIN = 3.0          // how close a celebrity must be to the leader to stay a candidate
        const val MIN_QUESTIONS = 5     // never guess before this
        const val MAX_QUESTIONS = 15    // always guess by this point
        const val LEAD_TO_GUESS = 4.0   // how far ahead the leader must be
    }
}