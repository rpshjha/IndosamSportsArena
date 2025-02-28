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
    application: Application, savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val minBid: Int = ResourceUtils.getMinBid(application.applicationContext)
    private val maxBid: Int = ResourceUtils.getMaxBid(application.applicationContext)

    private val _auctionState = MutableStateFlow(
        savedStateHandle.get<AuctionState>("auctionState") ?: AuctionState(
            teams = JsonUtils.loadTeamsFromJson(application.applicationContext), currentRound = 1
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
        val allPreviousTeamsSkipped = _skippedTeams.value.containsAll(currentState.teams.takeWhile { it != currentBidder })

        // Calculate the bid amount
        val bid = when {
            isFirstBidderInRound -> minBid
            else -> currentState.currentBid + 50
        }

        val finalBid = maxOf(minBid, bid).coerceAtMost(maxBid)

        if (finalBid > maxBid) {
            showError("Bid cannot exceed $maxBid points.")
            return
        }

        if (!canPlaceBid(currentBidder, finalBid)) return

        _teamBids.update { it + (currentBidder to finalBid) }
        _auctionState.update { it.copy(currentBid = finalBid) }
        previousBidder = currentBidder
        _biddingTeams.update { it + currentBidder }
        _teamDecisions.update { it + (currentBidder to "BID") }

        logActivity("$currentBidder placed a bid of $finalBid for ${currentState.remainingPlayers.firstOrNull()?.name}")

        if (finalBid == maxBid) {
            // Enter max bid phase: cycle through remaining teams
            val teams = currentState.teams
            val nextIndex = (teams.indexOf(currentBidder) + 1) % teams.size
            var currentIndex = nextIndex
            var allHandled = false

            while (!allHandled) {
                val team = teams[currentIndex]
                if (_skippedTeams.value.contains(team) || _teamBids.value[team] == maxBid) {
                    currentIndex = (currentIndex + 1) % teams.size
                    if (currentIndex == nextIndex) {
                        allHandled = true
                    }
                } else {
                    _auctionState.update { it.copy(currentBidder = team) }
                    break
                }
            }

            if (allHandled) {
                val maxBidders = _teamBids.value.filter { it.value == maxBid }.keys.toList()
                if (maxBidders.size > 1) {
                    performToss(maxBidders)
                    _showTossDialog.value = true
                } else {
                    assignCurrentPlayer()
                }
            }
        } else {
            // Check if all other teams have handled (skipped or bid)
            val otherTeams = currentState.teams.filter { it != currentBidder }
            val allOthersHandled = otherTeams.all { team ->
                _skippedTeams.value.contains(team) || _biddingTeams.value.contains(team)
            }

            if (allOthersHandled) {
                assignCurrentPlayer()
                _teamBids.value = emptyMap()
                _skippedTeams.value = emptySet()
                _biddingTeams.value = emptySet()
                _teamDecisions.value = emptyMap()
            } else {
                moveToNextBidder()
            }
        }
    }

    private fun allTeamsPlacedMaxBid(): Boolean {
        val currentState = _auctionState.value
        val teams = currentState.teams
        val teamBids = _teamBids.value

        // Filter teams that can afford the maximum bid
        val eligibleTeams = teams.filter { team ->
            val teamBudget = currentState.teamBudgets[team] ?: 0
            teamBudget >= maxBid && !_skippedTeams.value.contains(team)
        }

        // Check if all eligible teams have placed the maximum bid
        return eligibleTeams.all { team ->
            teamBids[team] == maxBid
        }
    }

    private fun performToss(eligibleTeams: List<String>) {
        if (eligibleTeams.isEmpty()) {
            showError("No eligible teams for toss.")
            return
        }
        val winningTeam = eligibleTeams.random()
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

            // Check if all teams have skipped their turns
            if (_skippedTeams.value.size == teams.size) {
                val currentPlayer = currentState.remainingPlayers.firstOrNull()
                if (currentPlayer != null) {
                    _auctionState.update { state ->
                        state.copy(
                            remainingPlayers = state.remainingPlayers.drop(1),
                            unsoldPlayers = state.unsoldPlayers + currentPlayer,
                            currentBid = 0,
                            currentBidder = teams[(state.currentRound) % teams.size],
                            currentRound = state.currentRound + 1
                        )
                    }
                    logActivity("${currentPlayer.name} moved to unsold list.")
                    _toastMessage.value = "${currentPlayer.name} moved to Unsold Player List"

                    SoundUtils.playError()

                    _skippedTeams.value = emptySet()
                    _biddingTeams.value = emptySet()
                    _teamDecisions.value = emptyMap()
                }
            } else {
                val nextBidderIndex = (currentBidderIndex + 1) % teams.size
                _auctionState.update { state ->
                    state.copy(currentBidder = teams[nextBidderIndex])
                }

                viewModelScope.launch {
                    val newCurrentBidder = _auctionState.value.currentBidder
                    val otherTeams = teams.filter { it != newCurrentBidder }
                    val allOthersSkipped = otherTeams.all { it in _skippedTeams.value }
                    val hasBid = newCurrentBidder in _biddingTeams.value

                    if (allOthersSkipped && hasBid) {
                        assignCurrentPlayer()
                    }
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

            SoundUtils.playSuccess()

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
        val currentState = _auctionState.value
        val currentPlayer = currentState.remainingPlayers.firstOrNull() ?: return

        val maxBidValue = _teamBids.value.values.maxOrNull() ?: 0
        val highestBidders = _teamBids.value.filter { it.value == maxBidValue }.keys.toList()

        when {
            highestBidders.isEmpty() -> {
                // No bids, move to unsold
                _auctionState.update { state ->
                    state.copy(
                        remainingPlayers = state.remainingPlayers.drop(1),
                        unsoldPlayers = state.unsoldPlayers + currentPlayer,
                        currentBid = 0,
                        currentBidder = state.teams.first(),
                        currentRound = state.currentRound + 1
                    )
                }
                logActivity("${currentPlayer.name} moved to unsold list.")
            }
            highestBidders.size == 1 -> {
                val winningTeam = highestBidders.first()
                val updatedTeamPlayers = currentState.teamPlayers.toMutableMap().apply {
                    this[winningTeam] = getOrDefault(winningTeam, mutableListOf()).also { it += currentPlayer }
                }
                val updatedBudgets = currentState.teamBudgets.toMutableMap().apply {
                    this[winningTeam] = (this[winningTeam] ?: 0) - maxBidValue
                }
                logActivity("${currentPlayer.name} assigned to $winningTeam for $maxBidValue points.")
                _toastMessage.value = "${currentPlayer.name} assigned to $winningTeam"
                SoundUtils.playSuccess()
                _auctionState.update { state ->
                    state.copy(
                        remainingPlayers = state.remainingPlayers.drop(1),
                        teamPlayers = updatedTeamPlayers,
                        teamBudgets = updatedBudgets,
                        currentBid = state.remainingPlayers.getOrNull(1)?.basePoint ?: 0,
                        currentRound = state.currentRound + 1,
                        currentBidder = state.teams[(state.currentRound) % state.teams.size]
                    )
                }
            }
            else -> {
                performToss(highestBidders)
                _showTossDialog.value = true
            }
        }

        _teamBids.value = emptyMap()
        _skippedTeams.value = emptySet()
        _biddingTeams.value = emptySet()
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
                remainingPlayers = emptyList(), teamPlayers = updatedTeamPlayers
            )
        }
    }

    fun assignUnsoldPlayers() {
        _auctionState.update { state ->
            val updatedTeamPlayers = state.teamPlayers.toMutableMap()
            val unsoldPlayers = state.unsoldPlayers.toMutableList()

            val teamCapacities =
                state.teams.associateWith { 6 - (updatedTeamPlayers[it]?.size ?: 0) }.toMutableMap()

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
                teamPlayers = updatedTeamPlayers, unsoldPlayers = unsoldPlayers
            )
        }
        _biddingTeams.value = emptySet()
    }

    fun canPlaceBid(team: String, bid: Int): Boolean {
        val teamBudget = _auctionState.value.teamBudgets[team] ?: 0
        val cappedBid = if (bid > 350) 350 else bid

        // Check if the team can afford the bid
        if (teamBudget < cappedBid) {
            showError("$team cannot place bid due to budget constraints.")
            return false
        }

        // Calculate the remaining budget after placing the bid
        val remainingBudget = teamBudget - cappedBid

        // Calculate the number of remaining players to be selected
        val playersSelected = _auctionState.value.teamPlayers[team]?.size ?: 0
        if (playersSelected >= 6) {
            showError("$team has already selected 6 players.")
            return false
        }

        val remainingPlayers = 6 - playersSelected - 1

        // Check if the team can afford the remaining players
        if (!canAffordRemainingPlayers(remainingBudget, remainingPlayers)) {
            showError("$team cannot place bid as they won't be able to afford the remaining players.")
            return false
        }

        return true
    }

    private fun canAffordRemainingPlayers(remainingBudget: Int, remainingPlayers: Int): Boolean {
        val minimumRequiredBudget = remainingPlayers * minBid
        return remainingBudget >= minimumRequiredBudget
    }

    fun calculateMaxBid(team: String): Int {
        val teamBudget = _auctionState.value.teamBudgets[team] ?: 0
        val playersSelected = _auctionState.value.teamPlayers[team]?.size ?: 0

        // Handle edge cases
        if (playersSelected >= 6) {
            return 0 // Team has already selected 6 players
        }

        val playersRemaining = 6 - playersSelected - 1
        val minRequiredBudget = playersRemaining * minBid

        val maxBid = teamBudget - minRequiredBudget
        return minOf(maxBid, 350)
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