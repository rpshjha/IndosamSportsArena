package com.indosam.sportsarena.models

data class AuctionLog(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)