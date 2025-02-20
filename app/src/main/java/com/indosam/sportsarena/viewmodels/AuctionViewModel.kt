package com.indosam.sportsarena.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.indosam.sportsarena.models.AuctionState
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.utils.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuctionViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // State for auction flow
//    private val _auctionState = MutableStateFlow(AuctionState(currentRound = 1))
    private val _auctionState = MutableStateFlow(
        savedStateHandle.get<AuctionState>("auctionState") ?: AuctionState(currentRound = 1)
    )
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

    private var previousBidder: String? = null

    init {
        // Save auction state whenever it changes
        viewModelScope.launch {
            _auctionState.collect { state ->
                savedStateHandle["auctionState"] = state
            }
        }
    }

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
        val currentBidder = currentState.currentBidder

        // Ensure the bid is valid
        val minBid = if (currentState.currentBid == 0) {
            currentState.remainingPlayers.firstOrNull()?.basePoint ?: 0
        } else {
            currentState.currentBid + 50
        }

        if (bid in minBid..350 && canPlaceBid(currentBidder, bid)) {
            _auctionState.update { state ->
                state.copy(currentBid = bid)
            }
            previousBidder = currentBidder // Update the previous highest bidder
            nextBidder() // Move to the next team
        } else {
            _toastMessage.value =
                "Bid must be at least $minBid points and cannot exceed 350 points."
        }
    }

    private fun nextBidder() {
        _auctionState.update { state ->
            val currentIndex = state.teams.indexOf(state.currentBidder)
            val nextIndex = (currentIndex + 1) % state.teams.size
            state.copy(currentBidder = state.teams[nextIndex])
        }
    }

    private fun nextTeam() {
        val currentState = _auctionState.value
        val teams = currentState.teams
        val currentBidder = currentState.currentBidder
        val currentIndex = teams.indexOf(currentBidder)

        // Calculate the starting index for the current round
        val startIndex = (currentState.currentRound - 1) % teams.size

        // Calculate the next bidder index in a round-robin fashion
        val nextIndex = (currentIndex + 1) % teams.size

        // If the next bidder is the starting bidder for this round, assign the player
        if (nextIndex == startIndex) {
            assignPlayer()
        } else {
            // Otherwise, move to the next bidder
            _auctionState.update { state ->
                state.copy(currentBidder = teams[nextIndex])
            }
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
                    currentBid = state.remainingPlayers.getOrNull(1)?.basePoint ?: 0,
                    currentRound = state.currentRound + 1
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
            val unsoldPlayers = _unsoldPlayers.value.toMutableList()

            // Calculate remaining capacity for each team
            val teamCapacities = state.teams.associateWith { team ->
                6 - (updatedTeamPlayers[team]?.size ?: 0)
            }.toMutableMap()

            // Assign unsold players to teams with available capacity
            val playersToAssign = unsoldPlayers.toMutableList()
            for (player in playersToAssign) {
                val availableTeams = state.teams.filter { teamCapacities.getOrDefault(it, 0) > 0 }

                if (availableTeams.isNotEmpty()) {
                    val selectedTeam = availableTeams.shuffled().first()
                    updatedTeamPlayers[selectedTeam]?.add(player)
                    teamCapacities[selectedTeam] = teamCapacities.getOrDefault(selectedTeam, 0) - 1
                    unsoldPlayers.remove(player)
                } else {
                    break // No more teams have capacity
                }
            }

            _unsoldPlayers.value = unsoldPlayers

            state.copy(
                teamPlayers = updatedTeamPlayers
            )
        }
    }

    fun canPlaceBid(team: String, bid: Int): Boolean {
        val teamBudget = _auctionState.value.teamBudgets[team] ?: 0
        return teamBudget >= bid && bid <= 350
    }

    fun skipTurn() {
        val currentState = _auctionState.value
        val teams = currentState.teams
        val currentBidderIndex = teams.indexOf(currentState.currentBidder)

        if (currentBidderIndex != -1) {
            // Add the current bidder to the skipped teams set
            _skippedTeams.value = _skippedTeams.value.toMutableSet().apply {
                add(currentState.currentBidder)
            }

            // Check if all teams have skipped the current player
            if (_skippedTeams.value.size == teams.size) {
                // Move the current player to the unsold list
                val currentPlayer = currentState.remainingPlayers.firstOrNull()
                if (currentPlayer != null) {
                    _unsoldPlayers.value = _unsoldPlayers.value + currentPlayer
                    _auctionState.update { state ->
                        state.copy(
                            remainingPlayers = state.remainingPlayers.drop(1),
                            currentBid = 0,
                            currentBidder = teams.first(), // Reset to the first team for the next player
                            currentRound = state.currentRound + 1 // Increment the round
                        )
                    }
                    // Clear the skipped teams set for the next player
                    _skippedTeams.value = emptySet()
                }
            } else {
                // Move to the next bidding team
                val nextBidderIndex = (currentBidderIndex + 1) % teams.size
                val nextBidder = teams[nextBidderIndex]
                _auctionState.update { state ->
                    state.copy(currentBidder = nextBidder)
                }
            }
        }
    }
}