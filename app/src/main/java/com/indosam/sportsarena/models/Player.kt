package com.indosam.sportsarena.models

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable

@Immutable
data class Player(
    val id: Int,
    val name: String,
    val aboutMe: String,
    val address: String,
    val dob: String,
    val battingStyle: String,
    val bowlingStyle: String,
    var isCaptain: Boolean = false,
    var isViceCaptain: Boolean = false,
    var basePoint: Int,
    val icon: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        name = parcel.readString() ?: "",
        aboutMe = parcel.readString() ?: "",
        address = parcel.readString() ?: "",
        dob = parcel.readString() ?: "",
        battingStyle = parcel.readString() ?: "",
        bowlingStyle = parcel.readString() ?: "",
        isCaptain = parcel.readByte() != 0.toByte(),
        isViceCaptain = parcel.readByte() != 0.toByte(),
        basePoint = parcel.readInt(),
        icon = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(aboutMe)
        parcel.writeString(address)
        parcel.writeString(dob)
        parcel.writeString(battingStyle)
        parcel.writeString(bowlingStyle)
        parcel.writeByte(if (isCaptain) 1 else 0)
        parcel.writeByte(if (isViceCaptain) 1 else 0)
        parcel.writeInt(basePoint)
        parcel.writeString(icon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }
    }
}