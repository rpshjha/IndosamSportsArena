package com.indosam.sportsarena.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.models.Team
import com.indosam.sportsarena.utils.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AuctionViewModel(application: Application) : AndroidViewModel(application) {

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams

    fun setTeams(selectedTeams: List<Team>) {
        _teams.value = selectedTeams
    }

    fun loadPlayers() {
        _players.value = JsonUtils.loadPlayersFromJson(getApplication<Application>().applicationContext)
    }

    // Method to set Captain
    fun setCaptain(team: Team, player: Player) {
        _teams.update { currentTeams ->
            currentTeams.map { t ->
                if (t.name == team.name) t.copy(captain = player) else t
            }
        }
    }

    // Method to set Vice-Captain
    fun setViceCaptain(team: Team, player: Player) {
        _teams.update { currentTeams ->
            currentTeams.map { t ->
                if (t.name == team.name) t.copy(viceCaptain = player) else t
            }
        }
    }

    // Method to start the auction
    fun startAuction() {
        // Implement logic to start the auction, such as updating the UI state
        println("Auction Started!")
    }
}
