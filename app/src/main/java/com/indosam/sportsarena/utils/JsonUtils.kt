package com.indosam.sportsarena.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.indosam.sportsarena.models.Player
import java.io.IOException

object JsonUtils {
    fun loadPlayersFromJson(context: Context): List<Player> {
        return try {
            val json = context.assets.open("players.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Player>>() {}.type
            Gson().fromJson(json, listType)
        } catch (e: IOException) {
            emptyList()
        }
    }
}