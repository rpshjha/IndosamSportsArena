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

    fun loadTeamsFromJson(context: Context): List<String> {
        return try {
            val json = context.assets.open("teams.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, listType) ?: emptyList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun parseSelectedTeamInfo(json: String): Map<String, Pair<String, String>> {
        val gson = Gson()
        return gson.fromJson(json, object : TypeToken<Map<String, Pair<String, String>>>() {}.type)
    }
}