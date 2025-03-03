package com.indosam.sportsarena.models

data class BoxLeagueMatchDetails(
    val eventName: String,                // Name of the event
    val date: String,                     // Date of the event
    val venue: String,                    // Venue of the event
    val matches: List<String>,            // List of matches
    val winnerCaptain: String? = null,    // Winning captain (nullable)
    val manOfTheMatch: String? = null,    // Man of the match (nullable)
    val highlights: String? = null,       // Match highlights (nullable)
    val winnerCaptainImage: String? = null // Image resource for winning captain (nullable)
)