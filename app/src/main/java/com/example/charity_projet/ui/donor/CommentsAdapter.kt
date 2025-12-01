package com.example.charity_projet.ui.donor


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.models.Commentaire
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private var comments: List<Commentaire>,
    private val listener: CommentClickListener,
    private val currentUserId: String
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    interface CommentClickListener {
        fun onDeleteClick(comment: Commentaire)
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tv_username)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        // Afficher les données
        holder.tvUsername.text = comment.user?.firstName ?: comment.user?.identifiant ?: "Anonymous"
        holder.tvDate.text = formatDate(comment.dateCreation)
        holder.tvContent.text = comment.contenu ?: ""

        // Afficher le bouton delete seulement si c'est le commentaire de l'utilisateur courant
        val isMyComment = comment.user?.getId() == currentUserId || comment.user?.identifiant == currentUserId
        holder.btnDelete.visibility = if (isMyComment) View.VISIBLE else View.GONE

        // Listener pour supprimer
        holder.btnDelete.setOnClickListener {
            listener.onDeleteClick(comment)
        }

        // Mettre en évidence mes commentaires
        if (isMyComment) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F0F8FF")) // Bleu clair
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Commentaire>) {
        comments = newComments
        notifyDataSetChanged()
    }

    private fun formatDate(date: String?): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            val dateObj = inputFormat.parse(date ?: "")
            outputFormat.format(dateObj ?: Date())
        } catch (e: Exception) {
            date ?: ""
        }
    }
}