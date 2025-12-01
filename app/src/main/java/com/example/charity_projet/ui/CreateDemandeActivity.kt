package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class CreateDemandeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var spinnerType: Spinner
    private lateinit var etContenu: EditText
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_demande)

        sessionManager = SessionManager(this)
        initializeViews()
        setupSpinner() // ⚠️ CORRIGÉ
        setupSubmitButton()
        setupBackButton()
    }

    private fun initializeViews() {
        spinnerType = findViewById(R.id.spinner_type)
        etContenu = findViewById(R.id.et_contenu)
        btnSubmit = findViewById(R.id.btn_submit)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun setupSpinner() {
        // ⚠️ CORRECTION: Utiliser les mêmes valeurs que le backend
        val types = arrayOf("SANTE", "ARGENT", "EDUCATION", "NOURRITURE", "VETEMENT", "LOGEMENT")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
    }

    private fun setupSubmitButton() {
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                createDemande()
            }
        }
    }

    private fun validateForm(): Boolean {
        val contenu = etContenu.text.toString().trim()

        if (contenu.isEmpty()) {
            showToast("Veuillez entrer la description de la demande")
            return false
        }

        return true
    }

    private fun createDemande() {
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            showToast("Non connecté")
            redirectToLogin()
            return
        }

        val contenu = etContenu.text.toString().trim()
        val type = spinnerType.selectedItem.toString() // ⚠️ Maintenant ça envoie "SANTE", "ARGENT", etc.

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = createDemandeApiCall(contenu, type, token)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        showToast("Demande créée avec succès!")
                        finish()
                    } else {
                        handleApiError(response.code(), response.errorBody()?.string())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Erreur: ${e.message}")
                }
            }
        }
    }

    private suspend fun createDemandeApiCall(contenu: String, type: String, token: String): Response<Demande> {
        // Format simple
        val demande = mapOf(
            "contenu" to contenu,
            "typeDemande" to type, // ⚠️ Maintenant ça envoie les bonnes valeurs
            "etat" to "EN_ATTENTE"
        )

        val gson = Gson()
        val demandeJson = gson.toJson(demande)

        val demandeBody = demandeJson.toRequestBody("application/json".toMediaType())
        val demandePart = MultipartBody.Part.createFormData("demande", null, demandeBody)

        return RetrofitClient.instance.createDemande(
            token = "Bearer $token",
            demande = demandePart,
            images = null,
            videos = null
        )
    }

    private fun handleApiError(errorCode: Int, errorBody: String?) {
        Log.e("API_ERROR", "Code: $errorCode, Body: $errorBody")

        when (errorCode) {
            400 -> showToast("Données invalides. Vérifiez les champs.")
            401 -> {
                showToast("Session expirée")
                redirectToLogin()
            }
            403 -> showToast("Accès refusé")
            else -> showToast("Erreur $errorCode")
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSubmit.isEnabled = !show
        btnSubmit.text = if (show) "Envoi en cours..." else "Créer la demande"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}