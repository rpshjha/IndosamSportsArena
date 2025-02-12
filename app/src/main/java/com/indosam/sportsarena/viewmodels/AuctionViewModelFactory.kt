package com.indosam.sportsarena.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// ViewModel Factory to create an instance of AuctionViewModel with Application
class AuctionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuctionViewModel::class.java)) {
            return AuctionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
