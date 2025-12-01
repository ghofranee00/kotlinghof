package com.example.charity_projet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.ApiResponse
import com.example.charity_projet.models.Demande
import com.example.charity_projet.ui.AdminRequestsActivity
import com.example.charity_projet.ui.DemandeAdapter
import com.example.charity_projet.ui.DemandesAccepteesActivity
import com.example.charity_projet.ui.LoginActivity
import com.example.charity_projet.ui.ProfileActivity
import com.example.charity_projet.ui.donor.DonorHomeActivity
import kotlinx.coroutines.launch
import retrofit2.Response
class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        redirectToAppropriatePage()
    }

    private fun redirectToAppropriatePage() {
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            // Non connecté → aller au login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Connecté → vérifier le rôle
            val userRole = sessionManager.getUserRole()

            when (userRole?.uppercase()) {
                "ADMIN" -> {
                    // Admin → aller directement aux demandes
                    val intent = Intent(this, AdminRequestsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                "NEEDY" -> {
                    // Needy → aller au profil ou page needy
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                "DONNATEUR", "ASSOCIATION" -> {
                    // Donnateur ou Association → aller au profil
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    // Autre rôle → aller au profil
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}