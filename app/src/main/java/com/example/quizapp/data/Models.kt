package com.example.quizapp.data

data class Question(val id: String, val text: String)

data class Celebrity(val name: String, val answers: Map<String, Int>)

data class GameData(
    val questions: List<Question>,
    val celebrities: List<Celebrity>
)
