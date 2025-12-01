package com.example.charity_projet.ui.donor


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.charity_projet.R

class DonationImagesAdapter(private var images: List<String>) :
    RecyclerView.Adapter<DonationImagesAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_donation_image)
        val btnRemove: ImageView = itemView.findViewById(R.id.btn_remove_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = images[position]

        Glide.with(holder.itemView.context)
            .load(imageUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(holder.imageView)

        holder.btnRemove.setOnClickListener {
            // Implémenter la suppression d'image
        }
    }

    override fun getItemCount(): Int = images.size

    fun updateImages(newImages: List<String>) {
        images = newImages
        notifyDataSetChanged()
    }
}