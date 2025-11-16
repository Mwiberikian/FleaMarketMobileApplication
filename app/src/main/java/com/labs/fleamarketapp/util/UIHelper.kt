package com.labs.fleamarketapp.util

import android.text.format.DateUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.labs.fleamarketapp.R
import java.text.NumberFormat
import java.util.Locale

object UIHelper {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "KE"))

    fun formatPrice(value: Double?): String {
        return if (value != null) currencyFormat.format(value) else "Price on request"
    }

    fun relativeTime(timestamp: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    fun loadImage(view: ImageView, url: String?, placeholder: Int = R.drawable.ic_launcher_foreground) {
        if (url.isNullOrBlank()) {
            view.setImageResource(placeholder)
            return
        }
        Glide.with(view.context)
            .load(url)
            .placeholder(placeholder)
            .error(placeholder)
            .centerCrop()
            .into(view)
    }
}

