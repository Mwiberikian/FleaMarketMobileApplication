package com.labs.fleamarketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.labs.fleamarketapp.R

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    
    private var selectedPosition = 0
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false)
        return CategoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedPosition)
    }
    
    override fun getItemCount() = categories.size
    
    fun setSelected(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }
    
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        
        fun bind(category: String, isSelected: Boolean) {
            categoryText.text = category
            categoryText.isSelected = isSelected
            
            itemView.setOnClickListener {
                setSelected(adapterPosition)
                onCategoryClick(category)
            }
        }
    }
}

