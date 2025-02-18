package com.indosam.sportsarena.utils

import android.content.Context
import com.indosam.sportsarena.models.Player
import org.json.JSONArray
import java.io.IOException

object DataManager {
    private val playerPool = mutableListOf<Player>()

    fun loadPlayers(context: Context) {
        try {
            val json = context.assets.open("players.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(json)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val player = Player(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    dob = obj.getString("dob"),
                    battingStyle = obj.getString("battingStyle"),
                    bowlingStyle = obj.getString("bowlingStyle"),
                    isCaptain = obj.getBoolean("isCaptain"),
                    isViceCaptain = obj.getBoolean("isViceCaptain"),
                    basePoint = obj.getInt("basePoint")
                )

                playerPool.add(player)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
