package com.example.charity_projet.ui

import com.example.charity_projet.R

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.SummaryReportDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.jvm.java

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalDemandes: TextView
    private lateinit var tvTotalDonations: TextView
    private lateinit var tvTotalPosts: TextView
    private lateinit var tvNeedyCount: TextView
    private lateinit var tvDonorCount: TextView
    private lateinit var tvAssociationCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        sessionManager = SessionManager(this)

        // Vérifier immédiatement si l'utilisateur est connecté
        if (sessionManager.fetchAuthToken() == null) {
            redirectToLogin()
            return // Important : arrêter l'exécution
        }

        initializeViews()
        setupBackButton()
        loadDashboardStats()
        setupQuickActions()
    }

    private fun initializeViews() {
        // ProgressBar
        progressBar = findViewById(R.id.progress_bar)

        // TextViews
        tvTotalUsers = findViewById(R.id.tv_total_users)
        tvTotalDemandes = findViewById(R.id.tv_total_demandes)
        tvTotalDonations = findViewById(R.id.tv_total_donations)
        tvTotalPosts = findViewById(R.id.tv_total_posts)
        tvNeedyCount = findViewById(R.id.tv_needy_count)
        tvDonorCount = findViewById(R.id.tv_donor_count)
        tvAssociationCount = findViewById(R.id.tv_association_count)
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun loadDashboardStats() {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token == null) {
                    withContext(Dispatchers.Main) {
                        showError("Non connecté")
                        redirectToLogin()
                    }
                    return@launch
                }

                Log.d("ADMIN_DASHBOARD", "Token: Bearer $token")
                val response = RetrofitClient.instance.getAdminSummaryReport("Bearer $token")

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val stats = response.body()
                        Log.d("ADMIN_DASHBOARD", "Stats reçues: $stats")
                        updateStatsUI(stats)
                    } else {
                        Log.e("ADMIN_DASHBOARD", "Erreur API: ${response.code()} - ${response.errorBody()?.string()}")

                        when (response.code()) {
                            403 -> {
                                showError("Accès refusé - Rôle admin requis")
                                // Rediriger vers l'écran approprié
                                redirectToUserDashboard()
                            }
                            401 -> {
                                Log.w("SESSION", "Token expiré ou invalide")
                                showError("Session expirée, veuillez vous reconnecter")
                                sessionManager.clearAuthToken()
                                redirectToLogin()
                            }
                            404 -> showError("Endpoint non trouvé - Vérifiez l'URL")
                            else -> showError("Erreur serveur: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ADMIN_DASHBOARD", "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (e is SocketTimeoutException || e is ConnectException) {
                        showError("Problème de connexion réseau")
                    } else {
                        showError("Erreur: ${e.message}")
                    }
                }
            }
        }
    }

    private fun redirectToUserDashboard() {
        val intent = Intent(this, AdminHomeActivity::class.java) // ou votre écran utilisateur
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun updateStatsUI(stats: SummaryReportDTO?) {
        // Stats principales
        tvTotalUsers.text = stats?.totalUsers?.toString() ?: "0"
        tvTotalDemandes.text = stats?.totalDemandes?.toString() ?: "0"
        tvTotalDonations.text = stats?.totalDonations?.toString() ?: "0"
        tvTotalPosts.text = stats?.totalPosts?.toString() ?: "0"

        // Stats par rôle utilisateur
        val usersByRole = stats?.usersByRole
        tvNeedyCount.text = usersByRole?.get("NEEDY")?.toString() ?: "0"
        tvDonorCount.text = usersByRole?.get("DONNATEUR")?.toString() ?: "0"
        tvAssociationCount.text = usersByRole?.get("ASSOCIATION")?.toString() ?: "0"

        Log.d("ADMIN_DASHBOARD", "UI mise à jour avec les stats")
    }

    private fun setupQuickActions() {
        findViewById<Button>(R.id.btn_manage_requests).setOnClickListener {
            val intent = Intent(this, DemandesAccepteesActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_view_posts).setOnClickListener {
            val intent = Intent(this, DemandesAccepteesActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_refresh).setOnClickListener {
            loadDashboardStats()
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Fermer cette activité
    }

    private fun redirectToLoginAndFinish() {
        sessionManager.clearAuthToken() // Nettoyer le token expiré
        redirectToLogin()
    }
}