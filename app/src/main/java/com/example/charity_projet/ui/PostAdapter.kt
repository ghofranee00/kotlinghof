
package com.example.charity_projet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.databinding.ItemPostBinding
import com.example.charity_projet.models.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(
    private var posts: List<Post>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvContent.text = post.contenu ?: "Aucun contenu"
            binding.tvType.text = "Type: ${post.typeDemande ?: "Inconnu"}"
            binding.tvUsername.text = "Par: ${post.user?.identifiant ?: "Utilisateur inconnu"}"
            binding.tvLikesCount.text = "Likes: ${post.likesCount}"
            binding.tvCommentsCount.text = "Commentaires: ${post.commentsCount}"

            // Date
            val date = post.dateCreation ?: ""
            binding.tvDate.text = if (date.isNotEmpty()) {
                try {
                    "Créé le: ${SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.getDefault()).format(Date(date))}"
                } catch (e: Exception) {
                    "Date inconnue"
                }
            } else {
                "Date inconnue"
            }

            // Cacher complètement la section images
            binding.ivPostImage.visibility = View.GONE
            binding.tvImageCount.visibility = View.GONE

            // Vidéos
            if (post.videoUrls.isNotEmpty()) {
                binding.tvVideoCount.visibility = View.VISIBLE
                binding.tvVideoCount.text = "📹 ${post.videoUrls.size} vidéo(s)"
            } else {
                binding.tvVideoCount.visibility = View.GONE
            }

            // Cacher les boutons d'action pour l'admin
            binding.buttonsLayout.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}