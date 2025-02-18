package com.indosam.sportsarena.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.utils.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuctionViewModel(application: Application) : AndroidViewModel(application) {

    // State for players
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    // State for selected players
    private val _selectedPlayers = MutableStateFlow<Set<String>>(emptySet())
    val selectedPlayers: StateFlow<Set<String>> = _selectedPlayers

    fun addSelectedPlayer(player: String) {
        _selectedPlayers.value = _selectedPlayers.value.toMutableSet().apply { add(player) }
    }
    fun updateSelectedTeamInfo(teamName: String, captain: String, viceCaptain: String) {
        _selectedTeamInfo.value = _selectedTeamInfo.value.toMutableMap().apply {
            put(teamName, Pair(captain, viceCaptain))
        }
    }

    // State for selected team info
    private val _selectedTeamInfo = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())
    val selectedTeamInfo: StateFlow<Map<String, Pair<String, String>>> = _selectedTeamInfo

    // State for auction flow
    private val _auctionState = MutableStateFlow(AuctionState())
    val auctionState: StateFlow<AuctionState> = _auctionState.asStateFlow()

    fun loadPlayers(excludeNames: List<String> = emptyList()) {
        viewModelScope.launch {
            val allPlayers = JsonUtils.loadPlayersFromJson(getApplication<Application>().applicationContext)
            _players.value = allPlayers.filter { it.name !in excludeNames }
            updateRemainingPlayers(_players.value)
        }
    }

    // Update remaining players
    fun updateRemainingPlayers(players: List<Player>) {
        _auctionState.update { state ->
            state.copy(remainingPlayers = players)
        }
    }

    // Handle bid placement
    fun handleBid(bid: Int) {
        _auctionState.update { state ->
            val currentPlayer = state.remainingPlayers.firstOrNull()
            if (currentPlayer != null && bid >= currentPlayer.basePoint && bid <= 350) {
                state.copy(currentBid = bid)
            } else {
                state
            }
        }
    }

    // Move to the next team in the bidding order
    fun nextTeam() {
        _auctionState.update { state ->
            val currentIndex = state.teams.indexOf(state.currentBidder)
            val nextBidder = state.teams[(currentIndex + 1) % state.teams.size]
            state.copy(currentBidder = nextBidder)
        }
    }

    // Assign the current player to the winning team
    fun assignPlayer() {
        _auctionState.update { state ->
            val currentPlayer = state.remainingPlayers.firstOrNull()
            if (currentPlayer != null) {
                val updatedTeamPlayers = state.teamPlayers.toMutableMap()
                updatedTeamPlayers[state.currentBidder]?.add(currentPlayer)
                val updatedBudgets = state.teamBudgets.toMutableMap()
                updatedBudgets[state.currentBidder] = updatedBudgets[state.currentBidder]!! - state.currentBid
                state.copy(
                    remainingPlayers = state.remainingPlayers.drop(1),
                    teamPlayers = updatedTeamPlayers,
                    teamBudgets = updatedBudgets,
                    currentBid = state.remainingPlayers.getOrNull(1)?.basePoint ?: 0
                )
            } else {
                state
            }
        }
        nextTeam()
    }

    // Assign unsold players randomly to teams
    fun assignUnsoldPlayers() {
        _auctionState.update { state ->
            val updatedTeamPlayers = state.teamPlayers.toMutableMap()
            val remainingPlayers = state.remainingPlayers.toMutableList()
            while (remainingPlayers.isNotEmpty()) {
                val player = remainingPlayers.first()
                val team = state.teams.random()
                if ((updatedTeamPlayers[team]?.size ?: 0) < 6) {
                    updatedTeamPlayers[team]?.add(player)
                    remainingPlayers.removeAt(0)
                }
            }
            state.copy(
                remainingPlayers = emptyList(),
                teamPlayers = updatedTeamPlayers
            )
        }
    }

    // Check if a team can place a bid
    fun canPlaceBid(team: String, bid: Int): Boolean {
        return _auctionState.value.teamBudgets[team]!! >= bid
    }

    // Data class for auction state
    data class AuctionState(
        val teams: List<String> = listOf("Indosam Titans", "Indosam Warriors", "Indosam Strikers"),
        val currentBidder: String = teams.random(),
        val currentBid: Int = 0,
        val remainingPlayers: List<Player> = emptyList(),
        val teamPlayers: Map<String, MutableList<Player>> = teams.associateWith { mutableListOf() },
        val teamBudgets: Map<String, Int> = teams.associateWith { 1000 }
    )
}