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

    // State for auction flow
    private val _auctionState = MutableStateFlow(AuctionState())
    val auctionState: StateFlow<AuctionState> = _auctionState.asStateFlow()

    // State for players
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    // State for selected players
    private val _selectedPlayers = MutableStateFlow<Set<String>>(emptySet())
    val selectedPlayers: StateFlow<Set<String>> = _selectedPlayers

    // State for selected team info
    private val _selectedTeamInfo = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())
    val selectedTeamInfo: StateFlow<Map<String, Pair<String, String>>> = _selectedTeamInfo

    // State for unsold players
    private val _unsoldPlayers = MutableStateFlow<List<Player>>(emptyList())
    val unsoldPlayers: StateFlow<List<Player>> = _unsoldPlayers.asStateFlow()

    // State for toast messages
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> get() = _toastMessage

    // Track which teams have skipped for the current player
    private val _skippedTeams = MutableStateFlow<Set<String>>(emptySet())
    val skippedTeams: StateFlow<Set<String>> = _skippedTeams

    fun loadPlayers(excludeNames: List<String> = emptyList()) {
        viewModelScope.launch {
            val allPlayers =
                JsonUtils.loadPlayersFromJson(getApplication<Application>().applicationContext)
            _players.value = allPlayers.filter { it.name !in excludeNames }
            updateRemainingPlayers(_players.value)
        }
    }

    fun addSelectedPlayer(player: String) {
        _selectedPlayers.value = _selectedPlayers.value.toMutableSet().apply { add(player) }
    }

    fun updateSelectedTeamInfo(teamName: String, captain: String, viceCaptain: String) {
        _selectedTeamInfo.value = _selectedTeamInfo.value.toMutableMap().apply {
            put(teamName, Pair(captain, viceCaptain))
        }
    }

    fun updateRemainingPlayers(players: List<Player>) {
        _auctionState.update { state ->
            state.copy(remainingPlayers = players)
        }
    }

    fun handleBid(bid: Int) {
        val currentState = _auctionState.value
        val updatedBid = currentState.currentBid + bid

        if (updatedBid >= 350) {
            val winningTeam = if (currentState.teams.count {
                    currentState.teamBudgets[it]!! >= 350
                } > 1) {
                currentState.teams.filter { currentState.teamBudgets[it]!! >= 350 }.random()
            } else {
                currentState.teams.firstOrNull { currentState.teamBudgets[it]!! >= 350 }
            }

            if (winningTeam != null) {
                _auctionState.update { state ->
                    val updatedTeamPlayers = state.teamPlayers.toMutableMap()
                    updatedTeamPlayers[winningTeam]?.add(state.remainingPlayers.first())
                    val updatedBudgets = state.teamBudgets.toMutableMap()
                    updatedBudgets[winningTeam] = updatedBudgets[winningTeam]!! - 350
                    state.copy(
                        remainingPlayers = state.remainingPlayers.drop(1),
                        teamPlayers = updatedTeamPlayers,
                        teamBudgets = updatedBudgets,
                        currentBid = 0
                    )
                }
            }
        } else {
            _auctionState.update { it.copy(currentBid = updatedBid) }
        }
    }

    private fun nextTeam() {
        val currentState = _auctionState.value
        val currentIndex = currentState.teams.indexOf(currentState.currentBidder)
        val nextIndex = (currentIndex + 1) % currentState.teams.size
        val nextBidder = currentState.teams[nextIndex]

        if (nextBidder == currentState.currentBidder) {
            assignPlayer()
        } else {
            _auctionState.update { it.copy(currentBidder = nextBidder) }
        }
    }

    fun assignPlayer() {
        _auctionState.update { state ->
            val currentPlayer = state.remainingPlayers.firstOrNull()
            if (currentPlayer != null) {
                val updatedTeamPlayers = state.teamPlayers.toMutableMap()
                updatedTeamPlayers[state.currentBidder]?.add(currentPlayer)
                val updatedBudgets = state.teamBudgets.toMutableMap()
                updatedBudgets[state.currentBidder] =
                    updatedBudgets[state.currentBidder]!! - state.currentBid
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

    fun canPlaceBid(team: String, bid: Int): Boolean {
        return _auctionState.value.teamBudgets[team]!! >= bid
    }

    fun skipTurn() {
        val currentState = _auctionState.value
        val currentPlayer = currentState.remainingPlayers.firstOrNull()
        if (currentPlayer != null) {
            // Add the current team to the skipped teams set
            _skippedTeams.value = _skippedTeams.value.toMutableSet().apply {
                add(currentState.currentBidder)
            }

            // Check if all teams have skipped
            if (_skippedTeams.value.size == currentState.teams.size) {
                // Move the player to unsold players
                _unsoldPlayers.value = _unsoldPlayers.value + currentPlayer
                _auctionState.update { state ->
                    state.copy(remainingPlayers = state.remainingPlayers.drop(1))
                }
                _toastMessage.value = "${currentPlayer.name} is unsold and added to unsold players."
                // Reset skipped teams for the next player
                _skippedTeams.value = emptySet()
            } else {
                // Move to the next team
                nextTeam()
            }
        }
    }

    data class AuctionState(
        val teams: List<String> = listOf("Indosam Titans", "Indosam Warriors", "Indosam Strikers"),
        val currentBidder: String = teams.random(),
        val currentBid: Int = 0,
        val remainingPlayers: List<Player> = emptyList(),
        val teamPlayers: Map<String, MutableList<Player>> = teams.associateWith { mutableListOf() },
        val teamBudgets: Map<String, Int> = teams.associateWith { 1000 }
    )
}