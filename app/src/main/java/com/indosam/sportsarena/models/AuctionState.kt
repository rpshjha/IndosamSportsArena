package com.indosam.sportsarena.models

import android.os.Parcel
import android.os.Parcelable

data class AuctionState(
    val teams: List<String> = listOf("Indosam Titans", "Indosam Warriors", "Indosam Strikers"),
    val currentRound: Int,
    val currentBidder: String = teams.random(),
    val startingBidder: String = currentBidder,
    val currentBid: Int = 0,
    val remainingPlayers: List<Player> = emptyList(),
    val teamPlayers: Map<String, MutableList<Player>> = teams.associateWith { mutableListOf() },
    val teamBudgets: Map<String, Int> = teams.associateWith { 1000 }
) : Parcelable {
    constructor(parcel: Parcel) : this(
        teams = parcel.createStringArrayList() ?: emptyList(),
        currentRound = parcel.readInt(),
        currentBidder = parcel.readString() ?: "",
        startingBidder = parcel.readString() ?: "",
        currentBid = parcel.readInt(),
        remainingPlayers = parcel.createTypedArrayList(Player.CREATOR) ?: emptyList(),
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
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(teams)
        parcel.writeInt(currentRound)
        parcel.writeString(currentBidder)
        parcel.writeString(startingBidder)
        parcel.writeInt(currentBid)
        parcel.writeTypedList(remainingPlayers)
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