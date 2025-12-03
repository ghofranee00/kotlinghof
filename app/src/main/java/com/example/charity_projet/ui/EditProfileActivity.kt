package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.PasswordUpdateRequest
import com.example.charity_projet.models.UserUpdateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sessionManager = SessionManager(this)

        // Initialiser les vues
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Charger les données
        loadDataFromIntent()

        btnSave.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Email invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateProfile(firstName, lastName, email)
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loadDataFromIntent() {
        val firstName = intent.getStringExtra("firstName") ?: ""
        val lastName = intent.getStringExtra("lastName") ?: ""
        val email = intent.getStringExtra("email") ?: ""

        etFirstName.setText(firstName)
        etLastName.setText(lastName)
        etEmail.setText(email)

        // Si données vides, charger depuis SessionManager
        if (firstName.isEmpty() && lastName.isEmpty() && email.isEmpty()) {
            loadFromSessionManager()
        }
    }

    private fun loadFromSessionManager() {
        val fullName = sessionManager.getUserName().orEmpty()
        val email = sessionManager.getUserEmail().orEmpty()

        // Séparer prénom et nom
        val nameParts = fullName.split(" ")
        if (nameParts.size >= 2) {
            etFirstName.setText(nameParts[0])
            etLastName.setText(nameParts.subList(1, nameParts.size).joinToString(" "))
        } else if (nameParts.size == 1) {
            etFirstName.setText(fullName)
        }
        etEmail.setText(email)
    }

    private fun updateProfile(firstName: String, lastName: String, email: String) {
        val token = sessionManager.fetchAuthToken()
        val username = sessionManager.getUsername() // 🔥 AJOUT : Récupérer le username

        if (token == null || username == null) {
            Toast.makeText(this, "Non authentifié", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 🔥 CORRECTION : Ajouter le username dans le path
                val response = RetrofitClient.instance.updateProfile(
                    username, // 🔥 AJOUT : Le username comme path parameter
                    UserUpdateRequest(firstName, lastName, email)
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val updatedUser = response.body()
                        // Mettre à jour les données locales
                        sessionManager.saveUserInfo(
                            username = username,
                            name = "$firstName $lastName",
                            email = email,
                            role = sessionManager.getUserRole() ?: "USER"
                        )

                        Toast.makeText(this@EditProfileActivity, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        // 🔥 DEBUG : Afficher plus d'infos sur l'erreur
                        Log.e("UPDATE_PROFILE", "Error: ${response.code()} - ${response.message()}")
                        val errorBody = response.errorBody()?.string()
                        Log.e("UPDATE_PROFILE", "Error body: $errorBody")

                        val errorMessage = when (response.code()) {
                            400 -> "Données invalides"
                            401 -> "Session expirée"
                            404 -> "Utilisateur non trouvé"
                            500 -> "Erreur serveur"
                            else -> "Erreur: ${response.code()}"
                        }
                        Toast.makeText(this@EditProfileActivity, "$errorMessage - $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e("UPDATE_PROFILE", "Exception: ${e.message}", e)
                    Toast.makeText(this@EditProfileActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun changePassword(currentPassword: String, newPassword: String) {
        val username = sessionManager.getUsername()

        if (username == null) {
            Toast.makeText(this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = PasswordUpdateRequest(currentPassword, newPassword)
                val response = RetrofitClient.instance.updatePassword(username, request)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "✅ Mot de passe changé avec succès!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("UPDATE_PASSWORD", "Error: ${response.code()} - $errorBody")
                        Toast.makeText(this@EditProfileActivity, "❌ Erreur: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e("UPDATE_PASSWORD", "Exception: ${e.message}", e)
                    Toast.makeText(this@EditProfileActivity, "❌ Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }    // 🔥 NOUVELLE MÉTHODE : Sauvegarde locale en fallback
    private fun saveLocally(firstName: String, lastName: String, email: String) {
        sessionManager.saveUserInfo(
            username = sessionManager.getUsername() ?: "",
            name = "$firstName $lastName",
            email = email,
            role = sessionManager.getUserRole() ?: "USER"
        )

        Toast.makeText(this, "Profil sauvegardé localement", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Changer le mot de passe")
            .setView(dialogView)
            .setPositiveButton("Changer") { dialog, which ->
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                changePassword(currentPassword, newPassword)
            }
            .setNegativeButton("Annuler", null)
            .create()

        dialog.show()
    }


    private fun showLoading(show: Boolean) {
        findViewById<ProgressBar>(R.id.progressBar).visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}