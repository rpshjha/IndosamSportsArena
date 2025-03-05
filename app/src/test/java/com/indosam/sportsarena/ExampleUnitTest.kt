package com.indosam.sportsarena

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.indosam.sportsarena.models.AuctionState
import com.indosam.sportsarena.models.Player
import com.indosam.sportsarena.models.Team
import com.indosam.sportsarena.utils.JsonUtils
import com.indosam.sportsarena.utils.ResourceUtils
import com.indosam.sportsarena.utils.SoundUtils
import com.indosam.sportsarena.viewmodels.AuctionViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val application: Application = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private lateinit var viewModel: AuctionViewModel

    private val testTeams = listOf(
        Team("Indosam Warriors", null, null, mutableListOf(), 1000),
        Team("Indosam Strikers", null, null, mutableListOf(), 1000),
        Team("Indosam Titans", null, null, mutableListOf(), 1000)
    )

    private val testPlayers = listOf(
        Player(1, "Player1", "About Player1", "Address1", "DOB1", "Right", "Fast",
            isCaptain = false,
            isViceCaptain = false,
            basePoint = 100,
            icon = "icon1"
        ),
        Player(2, "Player2", "About Player2", "Address2", "DOB2", "Left", "Spin",
            isCaptain = false,
            isViceCaptain = false,
            basePoint = 150,
            icon = "icon2"
        ),
        Player(3, "Player3", "About Player3", "Address3", "DOB3", "Right", "Medium",
            isCaptain = false,
            isViceCaptain = false,
            basePoint = 200,
            icon = "icon3"
        ),
        Player(4, "Player4", "About Player4", "Address4", "DOB4", "Left", "Fast",
            isCaptain = false,
            isViceCaptain = false,
            basePoint = 250,
            icon = "icon4"
        ),
        Player(5, "Player5", "About Player5", "Address5", "DOB5", "Right", "Spin",
            isCaptain = false,
            isViceCaptain = false,
            basePoint = 300,
            icon = "icon5"
        ),
        Player(6, "Player6", "About Player6", "Address6", "DOB6", "Left", "Medium", false,
            isViceCaptain = false,
            basePoint = 350,
            icon = "icon6"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(JsonUtils::class)
        mockkStatic(ResourceUtils::class)
        mockkStatic(SoundUtils::class)
        every { JsonUtils.loadTeamsFromJson(any()) } returns testTeams
        every { JsonUtils.loadPlayersFromJson(any()) } returns testPlayers
        every { ResourceUtils.getMinBid(any()) } returns 50
        every { ResourceUtils.getMaxBid(any()) } returns 350
        every { SoundUtils.initialize(any()) } returns Unit
        every { SoundUtils.release() } returns Unit
        every { SoundUtils.playSuccess() } returns Unit
        every { SoundUtils.playError() } returns Unit

        val initialState = AuctionState(teams = testTeams, currentRound = 1, currentBidder = "Indosam Warriors", remainingPlayers = testPlayers)
        every { savedStateHandle.get<AuctionState>("auctionState") } returns initialState
        viewModel = AuctionViewModel(application, savedStateHandle)
        viewModel.loadPlayers()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPlayers should load players from JSON`() = runTest {
        assertEquals(testPlayers, viewModel.players.value)
    }

    @Test
    fun `addSelectedPlayer should add player to selected players`() = runTest {
        viewModel.addSelectedPlayer("Player1")
        assertTrue(viewModel.selectedPlayers.first().contains("Player1"))
    }

    @Test
    fun `updateSelectedTeamInfo should update selected team info`() = runTest {
        viewModel.updateSelectedTeamInfo("Indosam Warriors", "Captain1", "ViceCaptain1")
        assertEquals("Captain1" to "ViceCaptain1", viewModel.selectedTeamInfo.first()["Indosam Warriors"])
    }

    @Test
    fun `handleBid should increment bid and move to next bidder`() = runTest {
        viewModel.handleBid()
        assertEquals(150, viewModel.auctionState.first().currentBid)
        assertEquals("Indosam Strikers", viewModel.auctionState.first().currentBidder)
    }

    @Test
    fun `handleBid should handle max bid and trigger toss`() = runTest {
        viewModel.auctionState.value.remainingPlayers.first().basePoint = 350
        viewModel.handleBid()
        viewModel.handleBid()
        viewModel.handleBid()
        assertTrue(viewModel.showTossDialog.first())
    }

    @Test
    fun `skipTurn should move to next bidder`() = runTest {
        viewModel.skipTurn()
        assertEquals("Indosam Strikers", viewModel.auctionState.first().currentBidder)
    }

    @Test
    fun `skipTurn should move player to unsold list if all teams skip`() = runTest {
        viewModel.skipTurn()
        viewModel.skipTurn()
        viewModel.skipTurn()
        assertEquals("Player1", viewModel.auctionState.first().unsoldPlayers.first().name)
    }

    @Test
    fun `nextTeam should move to next team`() = runTest {
        viewModel.nextTeam()
        assertEquals("Indosam Strikers", viewModel.auctionState.first().currentBidder)
    }


    @Test
    fun `assignCurrentPlayer should assign player to highest bidder`() = runTest {
        viewModel.handleBid()
        viewModel.nextTeam()
        viewModel.handleBid()
        viewModel.assignCurrentPlayer()
        assertEquals("Player1", viewModel.auctionState.first().teamPlayers["Indosam Strikers"]?.first()?.name)
    }

    @Test
    fun `assignRemainingPlayers should assign remaining players`() = runTest{
        viewModel.assignRemainingPlayers()
        assertTrue(viewModel.auctionState.first().remainingPlayers.isEmpty())
    }

    @Test
    fun `assignUnsoldPlayers should assign unsold players`() = runTest{
        viewModel.skipTurn()
        viewModel.skipTurn()
        viewModel.skipTurn()
        viewModel.assignUnsoldPlayers()
        assertTrue(viewModel.auctionState.first().unsoldPlayers.isEmpty())
    }

    @Test
    fun `canPlaceBid should return true if team can afford bid`() = runTest {
        assertTrue(viewModel.canPlaceBid("Indosam Warriors", 100))
    }

    @Test
    fun `canPlaceBid should return false if team cannot afford bid`() = runTest {
        assertFalse(viewModel.canPlaceBid("Indosam Warriors", 1001))
    }

    @Test
    fun `calculateMaxBid should calculate max bid based on budget and remaining players`() = runTest {
        assertEquals(350, viewModel.calculateMaxBid("Indosam Warriors"))
    }
}