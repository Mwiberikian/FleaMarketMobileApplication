package com.labs.fleamarketapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.data.HomeCategory

class CategoryAdapter(
    private val categories: List<HomeCategory>,
    private val onCategoryClick: (HomeCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_card, parent, false)
        return CategoryViewHolder(view as MaterialCardView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = categories.size

    private fun setSelected(position: Int) {
        val old = selectedPosition
        selectedPosition = position
        notifyItemChanged(old)
        notifyItemChanged(selectedPosition)
    }

    inner class CategoryViewHolder(private val cardView: MaterialCardView) :
        RecyclerView.ViewHolder(cardView) {
        private val title: TextView = cardView.findViewById(R.id.categoryTitle)
        private val subtitle: TextView = cardView.findViewById(R.id.categorySubtitle)
        private val image: ImageView = cardView.findViewById(R.id.categoryImage)

        fun bind(category: HomeCategory, isSelected: Boolean) {
            cardView.isCheckable = true
            title.text = category.title
            subtitle.text = category.subtitle
            cardView.isChecked = isSelected
            cardView.strokeWidth = if (isSelected) 3 else 1
            cardView.strokeColor = cardView.context.getColor(
                if (isSelected) R.color.primary else R.color.divider
            )

            // Load image from assets based on category title
            val fileName = when (category.title.lowercase()) {
                "all" -> "all.png"
                "books" -> "books.png"
                "electronics" -> "electronics.png"
                "furniture" -> "furniture.png"
                "jewellery" -> "jewellery.png"
                "services" -> "services.png"
                "food", "snacks" -> "snacks.png"
                "fashion", "clothing", "clothings" -> "clothings.png"
                else -> null
            }
            if (fileName != null) {
                Glide.with(cardView.context)
                    .load("file:///android_asset/$fileName")
                    .error(R.drawable.ic_launcher_foreground)
                    .into(image)
            } else {
                image.setImageResource(android.R.color.transparent)
            }

            cardView.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                setSelected(position)
                onCategoryClick(category)
            }
        }
    }
}
