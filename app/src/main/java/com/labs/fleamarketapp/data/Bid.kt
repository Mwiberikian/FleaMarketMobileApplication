package com.labs.fleamarketapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bid(
    val id: String,
    val itemId: String,
    val bidderId: String,
    val bidderName: String,
    val amount: Double,
    val timestamp: Long
) : Parcelable

