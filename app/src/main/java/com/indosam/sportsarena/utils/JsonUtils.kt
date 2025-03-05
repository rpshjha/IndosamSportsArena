package com.indosam.sportsarena.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.indosam.sportsarena.models.BoxLeagueMatchDetails
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.models.Team
import java.io.IOException

object JsonUtils {
    fun loadPlayersFromJson(context: Context): List<Player> {
        return try {
            val json = context.assets.open("players.json").bufferedReader().use { it.readText() }
            Log.d("JsonUtils", "Loaded players JSON: $json")

            val listType = object : TypeToken<List<Player>>() {}.type
            val players: List<Player> = Gson().fromJson(json, listType) ?: emptyList()

            Log.d("JsonUtils", "Parsed players: $players")
            players
        } catch (e: IOException) {
            Log.e("JsonUtils", "Error loading players from JSON: ${e.message}", e)
            emptyList()
        }
    }

    fun loadTeamsFromJson(context: Context): List<Team> {
        return try {
            val json = context.assets.open("teams.json").bufferedReader().use { it.readText() }
            Log.d("JsonUtils", "Loaded teams JSON: $json")

            val listType = object : TypeToken<List<Team>>() {}.type
            val teams: List<Team> = Gson().fromJson(json, listType) ?: emptyList()

            Log.d("JsonUtils", "Parsed teams: $teams")
            teams
        } catch (e: IOException) {
            Log.e("JsonUtils", "Error loading teams from JSON: ${e.message}", e)
            emptyList()
        }
    }

    fun parseSelectedTeamInfo(json: String): Map<String, Pair<String, String>> {
        val gson = Gson()
        return gson.fromJson(json, object : TypeToken<Map<String, Pair<String, String>>>() {}.type)
    }

    fun loadMatchesFromJson(context: Context): List<BoxLeagueMatchDetails> {
        return try {
            val json = context.assets.open("matches.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<BoxLeagueMatchDetails>>() {}.type
            Gson().fromJson(json, listType) ?: emptyList()
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}