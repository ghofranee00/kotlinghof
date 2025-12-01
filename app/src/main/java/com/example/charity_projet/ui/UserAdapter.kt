package com.example.charity_projet.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAdapter(
    private var users: List<User>,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tv_username)
        val tvEmail: TextView = itemView.findViewById(R.id.tv_email)
        val tvRole: TextView = itemView.findViewById(R.id.tv_role)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.tvUsername.text = user.identifiant ?: "Nom inconnu"
        holder.tvEmail.text = user.email ?: "Email non fourni"

        val role = user.role ?: "Non défini"
        holder.tvRole.text = "Rôle: $role"

        // Couleur selon le rôle
        when (role) {
            "ADMIN" -> holder.tvRole.setBackgroundColor(Color.parseColor("#FF0000"))
            "NEEDY" -> holder.tvRole.setBackgroundColor(Color.parseColor("#2196F3"))
            "DONOR" -> holder.tvRole.setBackgroundColor(Color.parseColor("#4CAF50"))
            else -> holder.tvRole.setBackgroundColor(Color.parseColor("#9E9E9E"))
        }

        // Date
        val date = user.dateCreation ?: ""
        holder.tvDate.text = if (date.isNotEmpty()) {
            try {
                "Inscrit le: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date))}"
            } catch (e: Exception) {
                "Date inconnue"
            }
        } else {
            "Date inconnue"
        }

        // Bouton de suppression
        holder.btnDelete.setOnClickListener {
            onDeleteClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}