package com.example.charity_projet.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.models.Demande

class PostsAdapter(
    private val posts: List<Demande>,
    private val onItemClick: (Demande) -> Unit
) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.tvContent.text = post.contenu ?: "No content"
        holder.tvType.text = post.typeDemande ?: "Unknown type"
        holder.tvDate.text = post.dateCreation ?: "Unknown date"

        holder.itemView.setOnClickListener {
            onItemClick(post)
        }
    }

    override fun getItemCount() = posts.size
}