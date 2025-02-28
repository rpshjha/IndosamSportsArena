package com.indosam.sportsarena.utils

import android.content.Context
import com.indosam.sportsarena.R

object ResourceUtils {
    fun getMaxBid(context: Context): Int {
        return context.resources.getInteger(R.integer.max_bid)
    }

    fun getMaxBudget(context: Context): Int {
        return context.resources.getInteger(R.integer.max_budget)
    }
}