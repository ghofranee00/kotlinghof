package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        sessionManager = SessionManager(this)
        setupNavigation()
        loadAdminDashboard() // Charger le dashboard par défaut
    }

    private fun setupNavigation() {
        // Top bar actions
        findViewById<ImageButton>(R.id.btn_notifications).setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.btn_profile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btn_logout).setOnClickListener {
            logoutUser()
        }

        // Bottom navigation
        findViewById<Button>(R.id.nav_dashboard).setOnClickListener {
            loadAdminDashboard()
        }

        findViewById<Button>(R.id.nav_users).setOnClickListener {
            loadUsersList()
        }

        findViewById<Button>(R.id.nav_stats).setOnClickListener {
            loadRequestsList()
        }

        findViewById<Button>(R.id.nav_settings).setOnClickListener {
            loadPostsList()
        }

        findViewById<Button>(R.id.nav_Donations).setOnClickListener {
            loadDonationsList()
        }
    }

    private fun loadAdminDashboard() {
        val intent = Intent(this, AdminDashboardActivity::class.java)
        startActivity(intent)
    }
    private fun logoutUser() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.instance.logout()
            } catch (e: Exception) {
                println("Logout API error: ${e.message}")
            }
        }

        sessionManager.clearAuthToken()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun loadUsersList() {
        val intent = Intent(this, UsersActivity::class.java)
        startActivity(intent)
    }

    private fun loadRequestsList() {
        val intent = Intent(this, AdminRequestsActivity::class.java)
        startActivity(intent)
    }

    private fun loadPostsList() {
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
    }

    private fun loadDonationsList() {
        showToast("Gestion Donations - À implémenter")
        // val intent = Intent(this, DonationsActivity::class.java)
        // startActivity(intent)
    }

    private fun updateTitle(title: String) {
        findViewById<TextView>(R.id.tv_title).text = title
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}