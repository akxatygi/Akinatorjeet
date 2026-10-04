package com.example.quizapp.data

import android.content.Context
import com.google.gson.Gson

object GameRepository {
    fun load(context: Context): GameData {
        return context.assets.open("celebrities.json").bufferedReader().use {
            Gson().fromJson(it, GameData::class.java)
        }
    }
}
