package com.example.akijeet.game

enum class Answer(val weight: Double) {
    YES(1.0),
    PROBABLY(0.5),
    DONT_KNOW(0.0),
    PROBABLY_NOT(-0.5),
    NO(-1.0)
}