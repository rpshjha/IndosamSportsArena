package com.indosam.sportsarena.models

data class Team(
    val name: String,
    var captain: Player? = null,
    var viceCaptain: Player? = null,
    val players: MutableList<Player> = mutableListOf(),
    var pointsLeft: Int = 1000
)