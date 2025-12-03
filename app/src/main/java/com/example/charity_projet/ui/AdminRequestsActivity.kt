package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.Demande
import com.example.charity_projet.utils.NotificationManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.util.Date

class AdminRequestsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DemandeAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnBack: ImageButton
    private lateinit var spinnerFilter: Spinner
    private lateinit var notificationManager: NotificationManager

    private var allDemandes = mutableListOf<Demande>()
    private var filteredDemandes = mutableListOf<Demande>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_requests)

        sessionManager = SessionManager(this)
        notificationManager = NotificationManager(this) // Initialize NotificationManager

        setupViews()
        setupSpinner()
        loadAllDemandes()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recycler_requests)
        progressBar = findViewById(R.id.progress_bar)
        tvEmpty = findViewById(R.id.tv_empty)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnBack = findViewById(R.id.btn_back)
        spinnerFilter = findViewById(R.id.spinner_filter)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DemandeAdapter(
            demandes = emptyList(),
            onAcceptClick = { demande -> acceptDemande(demande) },
            onRefuseClick = { demande -> refuseDemande(demande) }
        )
        recyclerView.adapter = adapter

        // Listeners
        btnBack.setOnClickListener {
            finish()
        }

        btnRefresh.setOnClickListener {
            loadAllDemandes()
        }
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf("Tous les types", "SANTE", "ARGENT", "EDUCATION", "NOURRITURE", "VETEMENT", "LOGEMENT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyTypeFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyTypeFilter() {
        val selectedType = spinnerFilter.selectedItem.toString()

        filteredDemandes = if (selectedType == "Tous les types") {
            allDemandes
        } else {
            allDemandes.filter { demande ->
                demande.typeDemande == selectedType
            }.toMutableList()
        }

        showDemandes(filteredDemandes)
    }

    private fun acceptDemande(demande: Demande) {
        showConfirmationDialog("Accepter", "Voulez-vous accepter cette demande ?") {
            processDemandeAction(demande, "accepter")
        }
    }

    private fun refuseDemande(demande: Demande) {
        showConfirmationDialog("Refuser", "Voulez-vous refuser cette demande ?") {
            processDemandeAction(demande, "refuser")
        }
    }

    private fun showConfirmationDialog(action: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(action)
            .setMessage(message)
            .setPositiveButton("Oui") { _, _ -> onConfirm() }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun processDemandeAction(demande: Demande, action: String) {
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            showToast("Session expirée")
            redirectToLogin()
            return
        }

        val demandeId = demande.getId()
        if (demandeId.isNullOrEmpty()) {
            showToast("Erreur: ID de demande invalide")
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Création d'un body vide pour la requête PUT
                val emptyBody = RequestBody.create("application/json".toMediaType(), "{}")
                
                // CORRECTION: Utiliser le bon endpoint avec paramètre action et body
                val response = RetrofitClient.instance.traiterDemande(
                    token = "Bearer $token",
                    id = demandeId,
                    action = action, // "accepter" ou "refuser"
                    body = emptyBody
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val message = if (action == "accepter") "acceptée" else "refusée"
                        showToast("Demande $message avec succès")
                        
                        // 🔥 Trigger notification
                        notificationManager.showNotification(
                            "Demande Traitée",
                            "La demande a été $message."
                        )
                        
                        // Recharger la liste
                        loadAllDemandes()
                    } else {
                        when (response.code()) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé - Rôle admin requis")
                            404 -> {
                                showToast("Demande non trouvée")
                                Log.e("ADMIN_ERROR", "404 - Demande non trouvée: $demandeId")
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                showToast("Erreur: ${response.code()}")
                            }
                        }
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

    private fun loadAllDemandes() {
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            showToast("Non connecté")
            redirectToLogin()
            return
        }

        showLoading(true)
        tvEmpty.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllDemandes("Bearer $token")

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val demandes = response.body() ?: emptyList()
                        allDemandes = demandes.toMutableList()
                        applyTypeFilter() // Appliquer le filtre après chargement
                    } else {
                        when (response.code()) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé - Rôle admin requis")
                            else -> {
                                showToast("Erreur: ${response.code()}")
                            }
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

    private fun showDemandes(demandes: List<Demande>) {
        adapter.updateData(demandes)
        if (demandes.isEmpty()) {
            val selectedType = spinnerFilter.selectedItem.toString()
            val message = if (selectedType == "Tous les types") {
                "Aucune demande en attente"
            } else {
                "Aucune demande de type $selectedType"
            }
            showEmptyState(true, message)
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
