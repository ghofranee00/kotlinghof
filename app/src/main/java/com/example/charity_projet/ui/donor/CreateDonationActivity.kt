package com.example.charity_projet.ui.donor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.DonationRequest
import com.example.charity_projet.models.Post
import com.example.charity_projet.models.User
import com.example.charity_projet.ui.LoginActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class CreateDonationActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRegion: Spinner
    private lateinit var etDetails: EditText
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvPostContent: TextView
    private lateinit var tvPostType: TextView

    private var postId: String = ""
    private var postContent: String = ""
    private var postType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d("CreateDonation", "=== onCreate START ===")
            setContentView(R.layout.activity_create_donation)

            sessionManager = SessionManager(this)

            getIntentData()

            // Vérifier si on a un post ID valide
            if (postId.isEmpty()) {
                Log.e("CreateDonation", "No post ID, finishing activity")
                finish()
                return
            }

            initializeViews()
            setupBackButton()

            setupCategorySpinner()
            setupRegionSpinner()
            setupSubmitButton()

            Log.d("CreateDonation", "=== onCreate COMPLETE ===")

        } catch (e: Exception) {
            Log.e("CreateDonation", "CRASH in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    private fun getIntentData() {
        Log.d("CreateDonation", "=== getIntentData() ===")

        // Récupérer avec valeurs par défaut
        postId = intent.getStringExtra("POST_ID") ?: ""
        postContent = intent.getStringExtra("POST_CONTENT") ?: "No content available"
        postType = intent.getStringExtra("POST_TYPE") ?: "GENERAL"

        // Debug
        Log.d("CreateDonation", "Post ID received: '$postId' (length: ${postId.length})")
        Log.d("CreateDonation", "Post Type: '$postType'")
        Log.d("CreateDonation", "Post Content preview: '${postContent.take(50)}...'")

        if (postId.isEmpty()) {
            Log.e("CreateDonation", "CRITICAL: Empty post ID!")

            // Afficher un message d'erreur détaillé
            val errorMessage = """
            Cannot create donation: No post ID provided.
            
            Possible causes:
            1. The post doesn't have an ID
            2. Error passing data between activities
            3. The post was deleted
            
            Please go back and select a different post.
        """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK") { _, _ ->
                    // Retourner à l'activité précédente
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            Log.d("CreateDonation", "Post data valid, continuing...")
        }
    }
    private fun initializeViews() {
        spinnerCategory = findViewById(R.id.spinner_category)
        spinnerRegion = findViewById(R.id.spinner_region)
        etDetails = findViewById(R.id.et_details)
        btnSubmit = findViewById(R.id.btn_submit)
        progressBar = findViewById(R.id.progress_bar)
        tvPostContent = findViewById(R.id.tv_post_content)
        tvPostType = findViewById(R.id.tv_post_type)

        tvPostContent.text = postContent
        tvPostType.text = "Type: $postType"
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "NOURRITURE",
            "LOGEMENT",
            "ARGENT",
            "VETEMENT",
            "EDUCATION",
            "SANTE"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Sélectionner automatiquement la catégorie basée sur le type de post
        val postTypeUpper = postType.uppercase()
        val position = categories.indexOfFirst { it == postTypeUpper }
        if (position != -1) {
            spinnerCategory.setSelection(position)
        }
    }

    private fun setupRegionSpinner() {
        val regions = arrayOf(
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Sousse", "Sfax",
            "Nabeul", "Bizerte", "Monastir", "Gabès", "Gafsa", "Kairouan",
            "Kasserine", "Mahdia", "Médenine", "Tataouine", "Tozeur",
            "Kébili", "Siliana", "Jendouba", "Béja", "Le Kef", "Zaghouan", "Sidi Bouzid"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, regions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRegion.adapter = adapter
    }

    private fun setupSubmitButton() {
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                showConfirmationDialog()
            }
        }
    }

    private fun validateForm(): Boolean {
        val details = etDetails.text.toString().trim()

        if (details.isEmpty()) {
            etDetails.error = "Please enter donation details"
            etDetails.requestFocus()
            return false
        }

        if (details.length < 10) {
            etDetails.error = "Please provide more details (minimum 10 characters)"
            etDetails.requestFocus()
            return false
        }

        return true
    }

    private fun showConfirmationDialog() {
        val category = spinnerCategory.selectedItem.toString()
        val region = spinnerRegion.selectedItem.toString()
        val details = etDetails.text.toString().trim()

        AlertDialog.Builder(this)
            .setTitle("Confirm Donation")
            .setMessage("Are you sure you want to submit this donation?\n\nCategory: $category\nRegion: $region")
            .setPositiveButton("Yes, Submit") { _, _ -> createDonation() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createDonation() {
        val token = sessionManager.fetchAuthToken()

        if (token == null) {
            showToast("Not authenticated")
            redirectToLogin()
            return
        }

        val category = spinnerCategory.selectedItem.toString()
        val region = spinnerRegion.selectedItem.toString()
        val details = etDetails.text.toString().trim()

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ⚠️ NE PAS ENVOYER L'EMAIL - Le backend le récupère du token
                val donationRequest = mapOf(
                    "postId" to postId,
                    "categorie" to category,
                    "region" to region,
                    "details" to details,
                    "images" to emptyList<String>()
                )

                val gson = GsonBuilder().create()
                val donationJson = gson.toJson(donationRequest)

                Log.d("CreateDonation", "=== REQUEST JSON ===")
                Log.d("CreateDonation", donationJson)

                val response = RetrofitClient.instance.createDonation(
                    token = "Bearer $token",
                    donationBody = donationJson.toRequestBody("application/json".toMediaType())
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        Log.d("CreateDonation", "✅ Donation created successfully!")
                        showToast("Donation created successfully!")
                        showSuccessAndFinish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("CreateDonation", "❌ Error ${response.code()}: $errorBody")

                        // Debug supplémentaire
                        Log.d("CreateDonation", "Token used: Bearer ${token?.take(30)}...")
                        Log.d("CreateDonation", "Post ID sent: $postId")

                        handleApiError(response.code(), errorBody)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e("CreateDonation", "❌ Exception: ${e.message}", e)
                    showToast("Error: ${e.message ?: "Network error"}")
                }
            }
        }
    }
    private fun handleApiError(errorCode: Int, errorBody: String?) {
        when (errorCode) {
            400 -> showToast("Invalid data: ${errorBody ?: "Please check all fields"}")
            401 -> {
                showToast("Session expired")
                redirectToLogin()
            }
            403 -> {
                // ⚠️ Ce message vient du backend: "Vous ne pouvez créer des donations que pour votre propre compte"
                showToast("Access denied: ${errorBody ?: "You can only create donations for your own account"}")
            }
            404 -> showToast("Post not found")
            else -> showToast("Error $errorCode: ${errorBody ?: "Unknown error"}")
        }
    }

    private fun showSuccessAndFinish() {
        // Retour avec succès
        val resultIntent = Intent()
        resultIntent.putExtra("DONATION_CREATED", true)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSubmit.isEnabled = !show
        btnSubmit.text = if (show) "Submitting..." else "Submit Donation"
        spinnerCategory.isEnabled = !show
        spinnerRegion.isEnabled = !show
        etDetails.isEnabled = !show
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun redirectToLogin() {
        sessionManager.clearAuthToken()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}