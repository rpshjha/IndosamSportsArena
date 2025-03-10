package com.indosam.sportsarena.models

import android.os.Parcel
import android.os.Parcelable

data class AuctionState(
    val teams: List<Team>,
    val currentRound: Int,
    val currentBidder: String = teams.firstOrNull()?.name ?: "",
    val startingBidder: String = currentBidder,
    val currentBid: Int = 0,
    val remainingPlayers: List<Player> = emptyList(),
    val unsoldPlayers: List<Player> = emptyList(),
    val teamPlayers: Map<String, MutableList<Player>> = teams.associateBy({ it.name }, { mutableListOf() }),
    val teamBudgets: Map<String, Int> = teams.associate { it.name to it.pointsLeft },

    val biddingTeams: MutableList<String> = teams.map { it.name }.toMutableList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        teams = parcel.createTypedArrayList(Team.CREATOR) ?: emptyList(),
        currentRound = parcel.readInt(),
        currentBidder = parcel.readString() ?: "",
        startingBidder = parcel.readString() ?: "",
        currentBid = parcel.readInt(),
        remainingPlayers = parcel.createTypedArrayList(Player.CREATOR) ?: emptyList(),
        unsoldPlayers = parcel.createTypedArrayList(Player.CREATOR) ?: emptyList(),
        teamPlayers = mutableMapOf<String, MutableList<Player>>().apply {
            val size = parcel.readInt()
            for (i in 0 until size) {
                val key = parcel.readString() ?: ""
                val value = parcel.createTypedArrayList(Player.CREATOR) ?: mutableListOf()
                this[key] = value
            }
        },
        teamBudgets = mutableMapOf<String, Int>().apply {
            val size = parcel.readInt()
            for (i in 0 until size) {
                val key = parcel.readString() ?: ""
                val value = parcel.readInt()
                this[key] = value
            }
        },
        biddingTeams = parcel.createStringArrayList()?.toMutableList() ?: mutableListOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(teams)
        parcel.writeInt(currentRound)
        parcel.writeString(currentBidder)
        parcel.writeString(startingBidder)
        parcel.writeInt(currentBid)
        parcel.writeTypedList(remainingPlayers)
        parcel.writeTypedList(unsoldPlayers)
        parcel.writeInt(teamPlayers.size)
        for ((key, value) in teamPlayers) {
            parcel.writeString(key)
            parcel.writeTypedList(value)
        }
        parcel.writeInt(teamBudgets.size)
        for ((key, value) in teamBudgets) {
            parcel.writeString(key)
            parcel.writeInt(value)
        }
        parcel.writeStringList(biddingTeams)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AuctionState> {
        override fun createFromParcel(parcel: Parcel): AuctionState {
            return AuctionState(parcel)
        }

        override fun newArray(size: Int): Array<AuctionState?> {
            return arrayOfNulls(size)
        }
    }
}