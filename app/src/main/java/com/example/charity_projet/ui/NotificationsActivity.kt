package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnMarkAllRead: Button
    private lateinit var tvUnreadCount: TextView

    private var allNotifications = mutableListOf<Notification>()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUsername() // ou l'ID de l'utilisateur
        setupViews()
        loadNotifications()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recycler_notifications)
        progressBar = findViewById(R.id.progress_bar)
        tvEmpty = findViewById(R.id.tv_empty)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnBack = findViewById(R.id.btn_back)
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read)
        tvUnreadCount = findViewById(R.id.tv_unread_count)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(
            notifications = emptyList(),
            onNotificationClick = { notification -> openNotification(notification) },
            onDeleteClick = { notification -> deleteNotification(notification) }
        )
        recyclerView.adapter = adapter

        // Listeners
        btnBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener { loadNotifications() }
        btnMarkAllRead.setOnClickListener { markAllAsRead() }
    }

    private fun loadNotifications() {
        val token = sessionManager.fetchAuthToken()
        val userId = currentUserId

        if (token == null || userId.isNullOrEmpty()) {
            showToast("Erreur d'authentification")
            return
        }

        showLoading(true)
        tvEmpty.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Récupérer toutes les notifications
                val response = RetrofitClient.instance.getNotificationsByUser(
                    token = "Bearer $token",
                    userId = userId
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val notifications = response.body() ?: emptyList()
                        allNotifications = notifications.toMutableList()
                        showNotifications(allNotifications)
                        loadUnreadCount() // Charger le compteur des non lues
                    } else {
                        when (response.code()) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé")
                            else -> showToast("Erreur: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Erreur réseau: ${e.message}")
                    showEmptyState(true, "Erreur de connexion")
                }
            }
        }
    }

    private fun loadUnreadCount() {
        val token = sessionManager.fetchAuthToken()
        val userId = currentUserId

        if (token == null || userId.isNullOrEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.compterNotificationsNonLues(
                    token = "Bearer $token",
                    userId = userId
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val unreadCount = response.body() ?: 0
                        tvUnreadCount.text = "$unreadCount non lue(s)"
                    }
                }
            } catch (e: Exception) {
                // Ignorer les erreurs pour le compteur
            }
        }
    }

    private fun openNotification(notification: Notification) {
        // Marquer comme lue quand on clique dessus
        if (!notification.lue) {
            markAsRead(notification)
        }

        // Afficher les détails de la notification
        showNotificationDetails(notification)
    }

    private fun markAsRead(notification: Notification) {
        val token = sessionManager.fetchAuthToken()
        val notificationId = notification.getId()

        if (token == null || notificationId.isNullOrEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.marquerNotificationLue(
                    token = "Bearer $token",
                    notificationId = notificationId
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Mettre à jour localement
                        notification.lue = true
                        adapter.notifyDataSetChanged()
                        loadUnreadCount()
                    }
                }
            } catch (e: Exception) {
                // Ignorer les erreurs
            }
        }
    }

    private fun markAllAsRead() {
        val token = sessionManager.fetchAuthToken()
        val userId = currentUserId

        if (token == null || userId.isNullOrEmpty()) return

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.marquerToutesNotificationsLues(
                    token = "Bearer $token",
                    userId = userId
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        showToast("Toutes les notifications marquées comme lues")
                        // Mettre à jour localement
                        allNotifications.forEach { it.lue = true }
                        adapter.notifyDataSetChanged()
                        loadUnreadCount()
                    } else {
                        showToast("Erreur lors du marquage")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Erreur réseau: ${e.message}")
                }
            }
        }
    }

    private fun deleteNotification(notification: Notification) {
        val token = sessionManager.fetchAuthToken()
        val notificationId = notification.getId()

        if (token == null || notificationId.isNullOrEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Supprimer la notification")
            .setMessage("Êtes-vous sûr de vouloir supprimer cette notification ?")
            .setPositiveButton("Supprimer") { _, _ ->
                processDeleteNotification(notification, notificationId, token)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun processDeleteNotification(notification: Notification, notificationId: String, token: String) {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.supprimerNotification(
                    token = "Bearer $token",
                    notificationId = notificationId
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        showToast("Notification supprimée")
                        // Retirer de la liste
                        allNotifications.remove(notification)
                        showNotifications(allNotifications)
                        loadUnreadCount()
                    } else {
                        showToast("Erreur lors de la suppression")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Erreur réseau: ${e.message}")
                }
            }
        }
    }

    private fun showNotificationDetails(notification: Notification) {
        AlertDialog.Builder(this)
            .setTitle(notification.titre ?: "Notification")
            .setMessage(notification.contenu ?: "Aucun contenu")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showNotifications(notifications: List<Notification>) {
        adapter.updateData(notifications)
        if (notifications.isEmpty()) {
            showEmptyState(true, "Aucune notification")
        } else {
            showEmptyState(false, "")
        }
    }

    private fun showEmptyState(show: Boolean, message: String) {
        if (show) {
            tvEmpty.text = message
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnRefresh.isEnabled = !show
        btnMarkAllRead.isEnabled = !show
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}