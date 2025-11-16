package com.labs.fleamarketapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.labs.fleamarketapp.R
import com.labs.fleamarketapp.util.UIHelper

class SelectedImageAdapter(
    private val onRemove: (Uri) -> Unit
) : ListAdapter<Uri, SelectedImageAdapter.ImageViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val preview: ShapeableImageView = itemView.findViewById(R.id.previewImage)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(uri: Uri) {
            UIHelper.loadImage(preview, uri.toString())
            removeButton.setOnClickListener { onRemove(uri) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean = oldItem == newItem
    }
}

