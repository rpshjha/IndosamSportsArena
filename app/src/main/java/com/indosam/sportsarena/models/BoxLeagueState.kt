package com.indosam.sportsarena.models

data class BoxLeagueState(
    val schedule: BoxLeagueMatchDetails,
    val isExpanded: Boolean = false
)