package com.indosam.sportsarena.models

data class Auction(
    val players: List<Player>,
    var currentBid: Int = 50,
    var currentBidder: String? = null
)
