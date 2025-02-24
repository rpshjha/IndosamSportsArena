package com.indosam.sportsarena.utils

object StringUtils {

    fun getFirstName(fullName: String): String {
        return fullName.split(" ").firstOrNull() ?: fullName
    }
}
