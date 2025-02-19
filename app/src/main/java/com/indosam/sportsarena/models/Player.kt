package com.indosam.sportsarena.models

import java.util.Date

data class Player(
    val id: Int,
    val name: String,
    val dob: String,
    val battingStyle: String,
    val bowlingStyle: String,
    var isCaptain: Boolean = false,
    var isViceCaptain: Boolean = false,
    var basePoint: Int,
    val icon: String?
)