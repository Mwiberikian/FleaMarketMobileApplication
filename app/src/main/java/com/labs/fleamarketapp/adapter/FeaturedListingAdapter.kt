package com.labs.fleamarketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.Item
import com.labs.fleamarketapp.util.UIHelper

class FeaturedListingAdapter(
    private val onItemClick: (Item) -> Unit
) : ListAdapter<Item, FeaturedListingAdapter.FeaturedViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_listing, parent, false)
        return FeaturedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeaturedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FeaturedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ShapeableImageView = itemView.findViewById(R.id.featuredImage)
        private val title: TextView = itemView.findViewById(R.id.featuredTitle)
        private val meta: TextView = itemView.findViewById(R.id.featuredMeta)
        private val price: TextView = itemView.findViewById(R.id.featuredPrice)

        fun bind(item: Item) {
            title.text = item.title
            price.text = UIHelper.formatPrice(item.price)
            meta.text = "${item.sellerName} • ${item.pickupLocation} • ${UIHelper.relativeTime(item.createdAt)}"
            UIHelper.loadImage(image, item.imageUrl)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem
    }
}

