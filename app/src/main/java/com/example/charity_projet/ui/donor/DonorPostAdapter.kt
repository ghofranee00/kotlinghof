package com.example.charity_projet.ui.donor

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.models.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DonorPostAdapter(
    private var posts: List<Post>,
    private val listener: PostClickListener,
    private val currentUserId: String? // Ajout de l'ID utilisateur courant
) : RecyclerView.Adapter<DonorPostAdapter.PostViewHolder>() {

    interface PostClickListener {
        fun onHelpClick(post: Post)
        fun onShareClick(post: Post)
        fun onLikeClick(post: Post)    // Nouveau
        fun onCommentClick(post: Post) // Nouveau
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tv_username)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val tvLikesCount: TextView = itemView.findViewById(R.id.tv_likes_count)
        val tvCommentsCount: TextView = itemView.findViewById(R.id.tv_comments_count)
        val ivPostImage: ImageView = itemView.findViewById(R.id.iv_post_image)
        val tvImageCount: TextView = itemView.findViewById(R.id.tv_image_count)
        val tvVideoCount: TextView = itemView.findViewById(R.id.tv_video_count)
        val buttonsLayout: LinearLayout = itemView.findViewById(R.id.buttons_layout)

        // Boutons
        val btnLike: Button = itemView.findViewById(R.id.btn_like)
        val btnComment: Button = itemView.findViewById(R.id.btn_comment)
        val btnHelp: Button = itemView.findViewById(R.id.btn_help)
        val btnShare: ImageButton = itemView.findViewById(R.id.btn_share)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donor_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Setup des données de base
        holder.tvUsername.text = post.user?.firstName ?: post.user?.identifiant ?: "Anonymous"
        holder.tvDate.text = formatDate(post.dateCreation)
        holder.tvType.text = post.typeDemande ?: "General"
        holder.tvContent.text = post.contenu ?: "No content"

        // Compteurs
        holder.tvLikesCount.text = "👍 ${post.likesCount}"
        holder.tvCommentsCount.text = "💬 ${post.commentsCount}"

        // Vérifier si l'utilisateur a déjà liké
        val isLiked = post.likedByUserIds?.contains(currentUserId) ?: false
        updateLikeButton(holder.btnLike, isLiked)

        // Couleur du bouton Help selon le type
        setupHelpButtonColor(holder.btnHelp, post.typeDemande)

        // Gestion des médias
        setupMedia(holder, post)

        // Listeners
        holder.btnLike.setOnClickListener {
            listener.onLikeClick(post)
        }

        holder.btnComment.setOnClickListener {
            listener.onCommentClick(post)
        }

        holder.btnHelp.setOnClickListener {
            listener.onHelpClick(post)
        }

        holder.btnShare.setOnClickListener {
            listener.onShareClick(post)
        }
    }

    private fun updateLikeButton(button: Button, isLiked: Boolean) {
        if (isLiked) {
            button.text = "❤️ Liked"
            button.setBackgroundColor(Color.parseColor("#FF4081")) // Rose pour "liked"
            button.setTextColor(Color.WHITE)
        } else {
            button.text = "👍 Like"
            button.setBackgroundColor(Color.parseColor("#E0E0E0")) // Gris clair
            button.setTextColor(Color.BLACK)
        }
    }

    private fun setupHelpButtonColor(button: Button, type: String?) {
        when (type?.uppercase()) {
            "NOURRITURE" -> button.setBackgroundColor(Color.parseColor("#4CAF50"))
            "LOGEMENT" -> button.setBackgroundColor(Color.parseColor("#2196F3"))
            "SANTE" -> button.setBackgroundColor(Color.parseColor("#F44336"))
            "EDUCATION" -> button.setBackgroundColor(Color.parseColor("#FF9800"))
            "VETEMENT" -> button.setBackgroundColor(Color.parseColor("#9C27B0"))
            "ARGENT" -> button.setBackgroundColor(Color.parseColor("#FFC107"))
            else -> button.setBackgroundColor(ContextCompat.getColor(button.context, R.color.purple_500))
        }
        button.setTextColor(Color.WHITE)
    }

    private fun setupMedia(holder: PostViewHolder, post: Post) {
        // Images
        if (!post.imageUrls.isNullOrEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            if (post.imageUrls.size > 1) {
                holder.tvImageCount.visibility = View.VISIBLE
                holder.tvImageCount.text = "+${post.imageUrls.size - 1}"
            } else {
                holder.tvImageCount.visibility = View.GONE
            }
            // Note: Ici vous pouvez charger l'image avec Glide/Picasso
            // Glide.with(holder.itemView.context).load(post.imageUrls[0]).into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
            holder.tvImageCount.visibility = View.GONE
        }

        // Videos
        if (!post.videoUrls.isNullOrEmpty()) {
            holder.tvVideoCount.visibility = View.VISIBLE
            holder.tvVideoCount.text = "📹 ${post.videoUrls.size} video(s)"
        } else {
            holder.tvVideoCount.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    private fun formatDate(date: String?): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            val dateObj = inputFormat.parse(date ?: "")
            outputFormat.format(dateObj ?: Date())
        } catch (e: Exception) {
            try {
                date?.substring(0, 10) ?: "Date inconnue"
            } catch (e2: Exception) {
                "Date inconnue"
            }
        }
    }
}