package com.labs.fleamarketapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val rating: Float = 0f
) : Parcelable

