package com.indosam.sportsarena.utils

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class DateUtils {
    companion object {
        fun calculateAge(dob: String): Int {
            val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
            val birthDate = LocalDate.parse(dob, formatter)
            val currentDate = LocalDate.now()
            val period = Period.between(birthDate, currentDate)
            return period.years
        }
    }
}
