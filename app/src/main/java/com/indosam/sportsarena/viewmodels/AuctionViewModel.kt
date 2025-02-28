package com.indosam.sportsarena.viewmodels


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.indosam.sportsarena.models.AuctionLog
import com.indosam.sportsarena.models.AuctionState
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.utils.JsonUtils
import com.indosam.sportsarena.utils.ResourceUtils
import com.indosam.sportsarena.utils.SoundUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AuctionViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val maxBid: Int = ResourceUtils.getMaxBid(application.applicationContext)

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
    val selectedPlayers: StateFlow<Set<String>> = _selectedPlayers.asStateFlow()

    private val _selectedTeamInfo = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())
    val selectedTeamInfo: StateFlow<Map<String, Pair<String, String>>> =
        _selectedTeamInfo.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> get() = _toastMessage

    private val _auctionLogs = MutableStateFlow<List<AuctionLog>>(emptyList())
    val auctionLogs: StateFlow<List<AuctionLog>> = _auctionLogs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _skippedTeams = MutableStateFlow<Set<String>>(emptySet())
    private val _biddingTeams = MutableStateFlow<Set<String>>(emptySet())
    val biddingTeams: StateFlow<Set<String>> = _biddingTeams.asStateFlow()

    private val _teamDecisions = MutableStateFlow<Map<String, String>>(emptyMap())
    val teamDecisions: StateFlow<Map<String, String>> = _teamDecisions.asStateFlow()

    private val _teamBids = MutableStateFlow<Map<String, Int>>(emptyMap())
    val teamBids: StateFlow<Map<String, Int>> = _teamBids.asStateFlow()

    private val _showTossDialog = MutableStateFlow(false)
    val showTossDialog: StateFlow<Boolean> = _showTossDialog.asStateFlow()

    private val _tossWinner = MutableStateFlow<String?>(null)
    val tossWinner: StateFlow<String?> = _tossWinner.asStateFlow()

    private var previousBidder: String? = null

    init {
        SoundUtils.initialize(getApplication<Application>().applicationContext)
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
        _selectedPlayers.update { it + player }
    }


    fun updateSelectedTeamInfo(teamName: String, captain: String, viceCaptain: String) {
        _selectedTeamInfo.update { it + (teamName to (captain to viceCaptain)) }
    }

    fun updateRemainingPlayers(players: List<Player>) {
        _auctionState.update { it.copy(remainingPlayers = players) }
    }

    fun handleBid() {
        val currentState = _auctionState.value
        val currentBidder = currentState.currentBidder
        val minBid = currentState.remainingPlayers.firstOrNull()?.basePoint ?: 0
        val isFirstBidderInRound = _biddingTeams.value.isEmpty()

        Log.d("AuctionViewModel", "Current Bidder: $currentBidder")
        Log.d("AuctionViewModel", "Is First Bidder in Round: $isFirstBidderInRound")
        Log.d("AuctionViewModel", "Current Bid: ${currentState.currentBid}")
        Log.d("AuctionViewModel", "Min Bid (Base Point): $minBid")


        // Calculate the bid amount
        val bid = if (isFirstBidderInRound) {
            minBid // First bidder places a bid equal to the base point
        } else {
            currentState.currentBid + 50 // Subsequent bidders increment the bid
        }

        var finalBid = maxOf(minBid, bid)

        Log.d("AuctionViewModel", "Calculated Bid: $finalBid")

        if (finalBid > maxBid) {
            finalBid = minOf(finalBid, maxBid)
            showError("Bid cannot exceed $maxBid points.")
        }

        if (!canPlaceBid(currentBidder, finalBid)) return

        _teamBids.update { it + (currentBidder to finalBid) }
        _auctionState.update { it.copy(currentBid = finalBid) }
        previousBidder = currentBidder
        _biddingTeams.update { it + currentBidder }
        _teamDecisions.update { it + (currentBidder to "BID") }

        Log.d("AuctionViewModel", "Bid placed: $finalBid by $currentBidder")

        logActivity("$currentBidder placed a bid of $finalBid for ${currentState.remainingPlayers.firstOrNull()?.name}")

        if (finalBid == maxBid && allTeamsPlacedMaxBid()) {
            Log.d("AuctionViewModel", "All teams placed max bid. Triggering toss.")
            performToss()
            _showTossDialog.value = true
        } else {
            moveToNextBidder()
        }
    }

    private fun allTeamsPlacedMaxBid(): Boolean {
        val currentState = _auctionState.value
        val teams = currentState.teams
        val teamBids = _teamBids.value

        return teams.all { team ->
            teamBids[team] == maxBid
        }
    }

    private fun performToss() {
        val currentState = _auctionState.value
        val teams = currentState.teams
        val winningTeam = teams.random()

        _tossWinner.value = winningTeam
        logActivity("Toss won by $winningTeam")
    }

    fun assignPlayerAfterToss() {
        val winningTeam = _tossWinner.value ?: return
        assignPlayerToTeam(winningTeam)
        _teamBids.value = emptyMap()
        _showTossDialog.value = false
    }

    private fun moveToNextBidder() {
        _auctionState.update { state ->
            val nextIndex = (state.teams.indexOf(state.currentBidder) + 1) % state.teams.size
            state.copy(currentBidder = state.teams[nextIndex])
        }
    }

    fun skipTurn() {
        val currentState = _auctionState.value
        val teams = currentState.teams
        val currentBidder = currentState.currentBidder

        if (currentBidder in _biddingTeams.value) {
            showError("You cannot skip after placing a bid.")
            return
        }

        val currentBidderIndex = teams.indexOf(currentBidder)
        if (currentBidderIndex != -1) {
            _skippedTeams.update { it + currentBidder }
            _teamDecisions.update { it + (currentBidder to "SKIP") }
            logActivity("$currentBidder skipped the turn for ${currentState.remainingPlayers.firstOrNull()?.name}")

            if (_skippedTeams.value.size == teams.size) {
                val currentPlayer = currentState.remainingPlayers.firstOrNull()
                if (currentPlayer != null) {
                    _auctionState.update { state ->
                        state.copy(
                            remainingPlayers = state.remainingPlayers.drop(1),
                            unsoldPlayers = state.unsoldPlayers + currentPlayer,
                            currentBid = 0,
                            currentBidder = teams.first(),
                            currentRound = state.currentRound + 1
                        )
                    }
                    logActivity("${currentPlayer.name} moved to unsold list.")
                    _skippedTeams.value = emptySet()
                    _biddingTeams.value = emptySet()
                    _teamDecisions.value = emptyMap()
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

    fun nextTeam() {
        _auctionState.update { state ->
            val nextIndex = (state.teams.indexOf(state.currentBidder) + 1) % state.teams.size
            state.copy(currentBidder = state.teams[nextIndex])
        }
    }

    private fun assignPlayerToTeam(team: String) {
        _auctionState.update { state ->
            val currentPlayer = state.remainingPlayers.firstOrNull() ?: return@update state

            val updatedTeamPlayers = state.teamPlayers.toMutableMap().apply {
                this[team] = getOrDefault(team, mutableListOf()).also { it += currentPlayer }
            }

            val updatedBudgets = state.teamBudgets.toMutableMap().apply {
                this[team] = (this[team] ?: 0) - maxBid
            }

            logActivity("${currentPlayer.name} assigned to $team via toss for $maxBid points.")
            _toastMessage.update { "${currentPlayer.name} assigned to $team via toss" }

            SoundUtils.playSound()

            state.copy(
                remainingPlayers = state.remainingPlayers.drop(1),
                teamPlayers = updatedTeamPlayers,
                teamBudgets = updatedBudgets,
                currentBid = 0,
                currentBidder = state.teams.first(),
                currentRound = state.currentRound + 1
            )
        }
        _biddingTeams.value = emptySet()
        _skippedTeams.value = emptySet()
        _teamDecisions.value = emptyMap()
    }

    fun assignCurrentPlayer() {
        _auctionState.update { state ->
            val currentPlayer = state.remainingPlayers.firstOrNull() ?: return@update state

            if (state.currentBidder in _skippedTeams.value) {
                showError("${state.currentBidder} has skipped this player and cannot be assigned.")
                return@update state
            }

            val bidderBudget = state.teamBudgets[state.currentBidder] ?: 0
            if (bidderBudget < state.currentBid) {
                showError("${state.currentBidder} does not have enough budget to buy ${currentPlayer.name}.")
                return@update state
            }

            val updatedTeamPlayers = state.teamPlayers.toMutableMap().apply {
                this[state.currentBidder] = getOrDefault(state.currentBidder, mutableListOf()).also { it += currentPlayer }
            }

            val updatedBudgets = state.teamBudgets.toMutableMap().apply {
                this[state.currentBidder] = bidderBudget - state.currentBid
            }

            logActivity("${currentPlayer.name} assigned to ${state.currentBidder} for ${state.currentBid} points.")
            _toastMessage.update { "${currentPlayer.name} assigned to ${state.currentBidder}" }

            SoundUtils.playSound()

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
        _teamDecisions.value = emptyMap()
    }

    fun assignRemainingPlayers() {
        _auctionState.update { state ->
            val remainingPlayers = state.remainingPlayers.toMutableList()
            val updatedTeamPlayers = state.teamPlayers.toMutableMap()


            val numTeams = state.teams.size
            val playersPerTeam = remainingPlayers.size / numTeams
            val extraPlayers = remainingPlayers.size % numTeams


            remainingPlayers.shuffle()


            var playerIndex = 0
            for (i in state.teams.indices) {
                val team = state.teams[i]
                val playersToAssign = playersPerTeam + if (i < extraPlayers) 1 else 0

                for (j in 0 until playersToAssign) {
                    if (playerIndex < remainingPlayers.size) {
                        val player = remainingPlayers[playerIndex]
                        updatedTeamPlayers.getOrPut(team) { mutableListOf() }.add(player)
                        logActivity("${player.name} assigned to $team from remaining players.")
                        playerIndex++
                    }
                }
            }

            state.copy(
                remainingPlayers = emptyList(),
                teamPlayers = updatedTeamPlayers
            )
        }
    }

    fun assignUnsoldPlayers() {
        _auctionState.update { state ->
            val updatedTeamPlayers = state.teamPlayers.toMutableMap()
            val unsoldPlayers = state.unsoldPlayers.toMutableList()

            val teamCapacities = state.teams.associateWith { 6 - (updatedTeamPlayers[it]?.size ?: 0) }.toMutableMap()

            val playersToAssign = unsoldPlayers.toList()

            for (player in playersToAssign) {
                val availableTeams = state.teams.filter { teamCapacities.getOrDefault(it, 0) > 0 }
                if (availableTeams.isNotEmpty()) {
                    val selectedTeam = availableTeams.shuffled().first()
                    updatedTeamPlayers.getOrPut(selectedTeam) { mutableListOf() }.add(player)
                    teamCapacities[selectedTeam] = teamCapacities.getOrDefault(selectedTeam, 0) - 1
                    unsoldPlayers.remove(player)
                    logActivity("${player.name} assigned to $selectedTeam from unsold list.")
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

    fun canPlaceBid(team: String, bid: Int): Boolean {
        val teamBudget = _auctionState.value.teamBudgets[team] ?: 0
        val cappedBid = if (bid > 350) 350 else bid

        return if (teamBudget >= cappedBid) {
            true
        } else {
            showError("$team cannot place bid due to budget constraints.")
            false
        }
    }

    private fun logActivity(message: String) {
        _auctionLogs.update { it + AuctionLog(message) }
    }

    private fun showError(message: String) {
        _errorMessage.value = message
    }

    fun clearToastMessage() {
        _toastMessage.update { null }
    }

    fun clearErrorMessage() {
        _errorMessage.update { null }
    }

    fun dismissTossDialog() {
        _showTossDialog.value = false
    }

    override fun onCleared() {
        super.onCleared()
        SoundUtils.release()
    }
}