package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.Demande
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.util.Date
class MesDemandesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DemandeNeedyAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mes_demandes)

        sessionManager = SessionManager(this)
        setupViews()
        loadMesDemandes()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        // Setup RecyclerView avec les callbacks
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DemandeNeedyAdapter(
            onEditClick = { demande -> editDemande(demande) },
            onDeleteClick = { demande -> deleteDemande(demande) }
        )
        recyclerView.adapter = adapter

        // Back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun editDemande(demande: Demande) {
        showEditDialog(demande)
    }

    private fun showEditDialog(demande: Demande) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_demande, null)
        val etContenu = dialogView.findViewById<EditText>(R.id.etEditContenu)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerEditType)

        // Pré-remplir les champs
        etContenu.setText(demande.contenu)

        // Setup spinner avec la sélection actuelle
        val types = arrayOf("SANTE", "ARGENT", "EDUCATION", "NOURRITURE", "VETEMENT", "LOGEMENT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        // Sélectionner le type actuel
        val currentType = demande.typeDemande ?: "SANTE"
        val position = types.indexOf(currentType)
        if (position >= 0) {
            spinnerType.setSelection(position)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Modifier la demande")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { dialog, which ->
                val nouveauContenu = etContenu.text.toString().trim()
                val nouveauType = spinnerType.selectedItem.toString()

                if (nouveauContenu.isNotEmpty()) {
                    updateDemande(demande, nouveauContenu, nouveauType)
                } else {
                    showToast("Le contenu ne peut pas être vide")
                }
            }
            .setNegativeButton("Annuler", null)
            .create()

        dialog.show()
    }

    private fun updateDemande(demande: Demande, nouveauContenu: String, nouveauType: String) {
        val token = sessionManager.fetchAuthToken()
        val userId = sessionManager.getUsername()

        if (token == null || userId == null) {
            showToast("Erreur d'authentification")
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Créer l'objet Demande mis à jour
                val demandeUpdate = Demande(
                    contenu = nouveauContenu,
                    typeDemande = nouveauType,
                    etat = demande.etat // Garder le même état
                )

                val response = RetrofitClient.instance.updateDemandeNeedy(
                    token = "Bearer $token",
                    id = demande.getId() ?: "",
                    userId = userId,
                    demande = demandeUpdate
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        showToast("Demande modifiée avec succès")
                        // Recharger la liste
                        loadMesDemandes()
                    } else {
                        when (response.code()) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé")
                            404 -> showToast("Demande non trouvée")
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                showToast("Erreur: ${response.code()} - $errorBody")
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

    private fun deleteDemande(demande: Demande) {
        val token = sessionManager.fetchAuthToken()
        val userId = sessionManager.getUsername()

        if (token == null || userId == null) {
            showToast("Erreur d'authentification")
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.deleteDemandeNeedy(
                    token = "Bearer $token",
                    id = demande.getId() ?: "",
                    userId = userId
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        showToast("Demande supprimée avec succès")
                        // Recharger la liste
                        loadMesDemandes()
                    } else {
                        when (response.code()) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé")
                            404 -> showToast("Demande non trouvée")
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                showToast("Erreur: ${response.code()} - $errorBody")
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

    private fun loadMesDemandes() {
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            showToast("Non connecté")
            redirectToLogin()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getMesDemandesEnAttente("Bearer $token")

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val demandes = response.body() ?: emptyList()
                        if (demandes.isEmpty()) {
                            showEmptyState(true, "Aucune demande en attente")
                        } else {
                            showEmptyState(false, "")
                            adapter.updateData(demandes)
                        }
                    } else {
                        when (response.code()) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé")
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                showToast("Erreur: ${response.code()} - $errorBody")
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