package com.labs.fleamarketapp.ui.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.databinding.ItemNotificationBinding
import com.labs.fleamarketapp.viewmodel.Notification

class NotificationAdapter(
    private val onToggleRead: (Notification, Boolean) -> Unit,
    private val onOpenItem: (Notification) -> Unit
) : ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding, onToggleRead, onOpenItem)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
        private val onToggleRead: (Notification, Boolean) -> Unit,
        private val onOpenItem: (Notification) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            val context = binding.root.context
            binding.typeLabel.text = context.getString(notification.typeLabelRes())
            binding.titleText.text = notification.title
            binding.messageText.text = notification.message
            binding.timeLabel.text = DateUtils.getRelativeTimeSpanString(
                notification.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            binding.typeIcon.setImageDrawable(
                ContextCompat.getDrawable(context, notification.typeIconRes())
            )

            binding.readIndicator.alpha = if (notification.isRead) 0f else 1f
            binding.root.alpha = if (notification.isRead) 0.65f else 1f

            binding.markReadButton.text = context.getString(
                if (notification.isRead) R.string.notification_mark_unread
                else R.string.notification_mark_read
            )
            binding.markReadButton.setOnClickListener {
                onToggleRead(notification, !notification.isRead)
            }

            val hasItemLink = !notification.itemId.isNullOrBlank()
            binding.viewButton.visibility = if (hasItemLink) View.VISIBLE else View.GONE
            binding.viewButton.setOnClickListener { onOpenItem(notification) }
            binding.root.setOnClickListener {
                if (hasItemLink) onOpenItem(notification) else onToggleRead(notification, !notification.isRead)
            }
        }

        private fun Notification.typeIconRes(): Int = when (type.uppercase()) {
            "BID", "NEW_BID" -> R.drawable.ic_notif_bid
            "OUTBID" -> R.drawable.ic_notif_outbid
            "WIN", "AUCTION_WIN" -> R.drawable.ic_notif_win
            "ORDER" -> R.drawable.ic_notif_order
            else -> R.drawable.ic_notif_message
        }

        private fun Notification.typeLabelRes(): Int = when (type.uppercase()) {
            "BID", "NEW_BID" -> R.string.notification_type_bid
            "OUTBID" -> R.string.notification_type_outbid
            "WIN", "AUCTION_WIN" -> R.string.notification_type_win
            "ORDER" -> R.string.notification_type_order
            else -> R.string.notification_type_message
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean =
            oldItem == newItem
    }
}
