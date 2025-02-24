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

    private val _auctionState = MutableStateFlow(
        savedStateHandle.get<AuctionState>("auctionState") ?: AuctionState(
            teams = JsonUtils.loadTeamsFromJson(application.applicationContext),
            currentRound = 1
        )
    )
    val auctionState: StateFlow<AuctionState> = _auctionState.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _selectedPlayers = MutableStateFlow<Set<String>>(emptySet())
    val selectedPlayers: StateFlow<Set<String>> = _selectedPlayers

    private val _selectedTeamInfo = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())
    val selectedTeamInfo: StateFlow<Map<String, Pair<String, String>>> = _selectedTeamInfo

    private val _unsoldPlayers = MutableStateFlow<List<Player>>(emptyList())
    val unsoldPlayers: StateFlow<List<Player>> = _unsoldPlayers.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> get() = _toastMessage

    private val _skippedTeams = MutableStateFlow<Set<String>>(emptySet())
    private val _biddingTeams = MutableStateFlow<Set<String>>(emptySet())

    private var previousBidder: String? = null

    init {
        viewModelScope.launch {
            _auctionState.collect { state ->
                savedStateHandle["auctionState"] = state
            }
        }
    }

    fun loadPlayers(excludeNames: List<String> = emptyList()) {
        viewModelScope.launch {
            val allPlayers = JsonUtils.loadPlayersFromJson(getApplication<Application>().applicationContext)
            _players.value = allPlayers.filter { it.name !in excludeNames }
            updateRemainingPlayers(_players.value)
        }
    }

    fun addSelectedPlayer(player: String) {
        _selectedPlayers.update { it + player }
    }

    fun updateSelectedTeamInfo(teamName: String, captain: String, viceCaptain: String) {
        _selectedTeamInfo.update { it + (teamName to (captain to viceCaptain)) }
    }

    fun updateRemainingPlayers(players: List<Player>) {
        _auctionState.update { it.copy(remainingPlayers = players) }
    }

    fun handleBid(bid: Int) {
        val currentState = _auctionState.value
        val currentBidder = currentState.currentBidder

        val minBid = currentState.remainingPlayers.firstOrNull()?.basePoint ?: 0
        val nextBid = if (currentState.currentBid == 0) minBid else currentState.currentBid + 50

        if (bid in nextBid..350 && canPlaceBid(currentBidder, bid)) {
            _auctionState.update { it.copy(currentBid = bid) }
            previousBidder = currentBidder
            _biddingTeams.update { it + currentBidder }
            nextBidder()
        } else {
            _toastMessage.value = "Bid must be at least $nextBid points and cannot exceed 350 points."
        }
    }

    private fun nextBidder() {
        _auctionState.update { state ->
            val nextIndex = (state.teams.indexOf(state.currentBidder) + 1) % state.teams.size
            state.copy(currentBidder = state.teams[nextIndex])
        }
    }

    fun nextTeam() {
        _auctionState.update { state ->
            val nextIndex = (state.teams.indexOf(state.currentBidder) + 1) % state.teams.size
            state.copy(currentBidder = state.teams[nextIndex])
        }
    }

    fun assignPlayer() {
        _auctionState.update { state ->
            val currentPlayer = state.remainingPlayers.firstOrNull() ?: return@update state

            if (state.currentBidder in _skippedTeams.value) {
                _toastMessage.value = "${state.currentBidder} has skipped this player and cannot be assigned."
                return@update state
            }

            val updatedTeamPlayers = state.teamPlayers.toMutableMap().apply {
                get(state.currentBidder)?.add(currentPlayer)
            }
            val updatedBudgets = state.teamBudgets.toMutableMap().apply {
                this[state.currentBidder] = this[state.currentBidder]!! - state.currentBid
            }

            _toastMessage.value = "${currentPlayer.name} assigned to ${state.currentBidder}"

            state.copy(
                remainingPlayers = state.remainingPlayers.drop(1),
                teamPlayers = updatedTeamPlayers,
                teamBudgets = updatedBudgets,
                currentBid = state.remainingPlayers.getOrNull(1)?.basePoint ?: 0,
                currentRound = state.currentRound + 1
            )
        }
        _biddingTeams.value = emptySet()
        _skippedTeams.value = emptySet()
    }

    fun assignUnsoldPlayers() {
        _auctionState.update { state ->
            val updatedTeamPlayers = state.teamPlayers.toMutableMap()
            val unsoldPlayers = _unsoldPlayers.value.toMutableList()

            val teamCapacities = state.teams.associateWith { 6 - (updatedTeamPlayers[it]?.size ?: 0) }.toMutableMap()

            val playersToAssign = unsoldPlayers.toMutableList()

            for (player in playersToAssign) {
                val availableTeams = state.teams.filter { teamCapacities.getOrDefault(it, 0) > 0 }
                if (availableTeams.isNotEmpty()) {
                    val selectedTeam = availableTeams.shuffled().first()
                    updatedTeamPlayers[selectedTeam]?.add(player)
                    teamCapacities[selectedTeam] = teamCapacities.getOrDefault(selectedTeam, 0) - 1
                    unsoldPlayers.remove(player)
                } else {
                    break
                }
            }

            state.copy(
                teamPlayers = updatedTeamPlayers,
                unsoldPlayers = unsoldPlayers
            )
        }
        _biddingTeams.value = emptySet()
    }

    private fun canPlaceBid(team: String, bid: Int): Boolean {
        val teamBudget = _auctionState.value.teamBudgets[team] ?: 0
        return teamBudget >= bid && bid <= 350
    }

    fun skipTurn() {
        val currentState = _auctionState.value
        val teams = currentState.teams
        val currentBidder = currentState.currentBidder

        if (currentBidder in _biddingTeams.value) {
            _toastMessage.value = "You cannot skip after placing a bid."
            return
        }

        val currentBidderIndex = teams.indexOf(currentBidder)
        if (currentBidderIndex != -1) {
            _skippedTeams.update { it + currentBidder }

            if (_skippedTeams.value.size == teams.size) {
                val currentPlayer = currentState.remainingPlayers.firstOrNull()
                if (currentPlayer != null) {
                    _unsoldPlayers.update { it + currentPlayer }
                    _auctionState.update { state ->
                        state.copy(
                            remainingPlayers = state.remainingPlayers.drop(1),
                            unsoldPlayers = state.unsoldPlayers + currentPlayer,
                            currentBid = 0,
                            currentBidder = teams.first(),
                            currentRound = state.currentRound + 1
                        )
                    }
                    _skippedTeams.value = emptySet()
                    _biddingTeams.value = emptySet()
                }
            } else {
                val nextBidderIndex = (currentBidderIndex + 1) % teams.size
                _auctionState.update { state ->
                    state.copy(currentBidder = teams[nextBidderIndex])
                }
            }
            previousBidder = null
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}