package com.indosam.sportsarena.models

data class Player(
    val id: Int,
    val name: String,
    val role: String,
    var isCaptain: Boolean = false,
    var isViceCaptain: Boolean = false,
    var basePoint: Int = 50
)