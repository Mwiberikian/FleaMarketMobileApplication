package com.labs.fleamarketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.Bid
import com.labs.fleamarketapp.util.UIHelper

class BidHistoryAdapter : ListAdapter<Bid, BidHistoryAdapter.BidViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BidViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bid_history, parent, false)
        return BidViewHolder(view)
    }

    override fun onBindViewHolder(holder: BidViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BidViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bidderName: TextView = itemView.findViewById(R.id.bidderName)
        private val bidAmount: TextView = itemView.findViewById(R.id.bidAmount)
        private val bidTime: TextView = itemView.findViewById(R.id.bidTime)

        fun bind(bid: Bid) {
            bidderName.text = if (bid.bidderName.isNotBlank()) bid.bidderName else "Anonymous"
            bidAmount.text = UIHelper.formatPrice(bid.amount)
            bidTime.text = UIHelper.relativeTime(bid.timestamp)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Bid>() {
        override fun areItemsTheSame(oldItem: Bid, newItem: Bid): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Bid, newItem: Bid): Boolean = oldItem == newItem
    }
}

