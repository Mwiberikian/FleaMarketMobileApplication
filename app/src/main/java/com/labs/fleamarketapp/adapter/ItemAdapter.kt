package com.labs.fleamarketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.Item
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(
    private val items: List<Item>,
    private val onItemClick: (Item) -> Unit,
    private val onEditClick: ((Item) -> Unit)? = null,
    private val onDeleteClick: ((Item) -> Unit)? = null
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing_card, parent, false)
        return ItemViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.itemTitle)
        private val priceText: TextView = itemView.findViewById(R.id.itemPrice)
        private val editButton: View? = itemView.findViewById(R.id.editButton)
        private val deleteButton: View? = itemView.findViewById(R.id.deleteButton)
        private val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        
        fun bind(item: Item) {
            titleText.text = item.title
            priceText.text = formatPrice(item.price)

            // Show action buttons when callbacks provided (My Listings)
            if (onEditClick != null && onDeleteClick != null) {
                editButton?.visibility = View.VISIBLE
                deleteButton?.visibility = View.VISIBLE
                editButton?.setOnClickListener { onEditClick.invoke(item) }
                deleteButton?.setOnClickListener { onDeleteClick.invoke(item) }
            } else {
                editButton?.visibility = View.GONE
                deleteButton?.visibility = View.GONE
            }
            
            if (item.imageUrl != null && item.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
            
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
        
        private fun formatPrice(price: Double): String {
            val format = NumberFormat.getNumberInstance(Locale("en","KE"))
            return "Kshs ${format.format(price)}"
        }
    }
}

