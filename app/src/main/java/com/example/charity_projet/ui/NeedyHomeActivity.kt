package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NeedyHomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_needy_home)

        sessionManager = SessionManager(this)
        setupNavigation()
        setupWelcomeSection()
    }

    private fun setupNavigation() {
        // Navigation Bottom
        findViewById<Button>(R.id.nav_about).setOnClickListener {
            showAboutUsActivity()
        }

        findViewById<Button>(R.id.nav_create).setOnClickListener {
            showCreateDemandeActivity()
        }
        findViewById<Button>(R.id.nav_mes_demandes).setOnClickListener {
            showMesDemandesActivity()
        }
        findViewById<Button>(R.id.nav_posts).setOnClickListener {
            showPostsActivity()
        }

        // Top Bar buttons
        findViewById<ImageButton>(R.id.btn_notifications).setOnClickListener {
            showNotificationsActivity()
        }

        findViewById<ImageButton>(R.id.btn_profile).setOnClickListener {
            showProfileActivity()
        }

        findViewById<ImageButton>(R.id.btn_logout).setOnClickListener {
            logoutUser()
        }
    }

    private fun setupWelcomeSection() {
        // Setup help button to go directly to CreateDemandeActivity
        findViewById<Button>(R.id.btn_help).setOnClickListener {
            showCreateDemandeActivity()
        }
    }

    private fun showAboutUsActivity() {
        val intent = Intent(this, AboutUsActivity::class.java)
        startActivity(intent)
    }

    private fun showCreateDemandeActivity() {
        val intent = Intent(this, CreateDemandeActivity::class.java)
        startActivity(intent)
    }

    private fun showPostsActivity() {
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
    }

    private fun showMesDemandesActivity() {
        val intent = Intent(this, MesDemandesActivity::class.java)
        startActivity(intent)
    }

    private fun showNotificationsActivity() {
        val intent = Intent(this, NotificationsActivity::class.java)
        startActivity(intent)
    }

    private fun showProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
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
}