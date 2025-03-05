package com.indosam.sportsarena.models

import android.os.Parcel
import android.os.Parcelable

data class Team(
    val name: String,
    var captain: Player? = null,
    var viceCaptain: Player? = null,
    val players: MutableList<Player> = mutableListOf(),
    var pointsLeft: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        name = parcel.readString() ?: "",
        captain = parcel.readParcelable(Player::class.java.classLoader),
        viceCaptain = parcel.readParcelable(Player::class.java.classLoader),
        players = parcel.createTypedArrayList(Player.CREATOR) ?: mutableListOf(),
        pointsLeft = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(captain, flags)
        parcel.writeParcelable(viceCaptain, flags)
        parcel.writeTypedList(players)
        parcel.writeInt(pointsLeft)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Team> {
        override fun createFromParcel(parcel: Parcel): Team {
            return Team(parcel)
        }

        override fun newArray(size: Int): Array<Team?> {
            return arrayOfNulls(size)
        }
    }
}
