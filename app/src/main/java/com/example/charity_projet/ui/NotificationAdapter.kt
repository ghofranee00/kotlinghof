package com.example.charity_projet.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.models.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit,
    private val onDeleteClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val ivUnread: ImageView = itemView.findViewById(R.id.iv_unread)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.tvTitle.text = notification.titre ?: "Notification"
        holder.tvContent.text = notification.contenu ?: "Aucun contenu"
        holder.tvType.text = notification.type ?: "Type inconnu"

        // Date
        val date = notification.dateCreation ?: ""
        holder.tvDate.text = if (date.isNotEmpty()) {
            try {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(date))
            } catch (e: Exception) {
                "Date inconnue"
            }
        } else {
            "Date inconnue"
        }

        // Indicateur non lu
        holder.ivUnread.visibility = if (notification.lue) View.GONE else View.VISIBLE

        // Clic sur l'item
        holder.itemView.setOnClickListener {
            onNotificationClick(notification)
        }

        // Bouton suppression
        holder.btnDelete.setOnClickListener {
            onDeleteClick(notification)
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}