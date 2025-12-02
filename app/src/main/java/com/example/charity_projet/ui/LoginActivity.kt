package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Si un token existe, on va directement à la page principale
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(username, password))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()!!

                        // 🔥 CRITIQUE: Récupérer TOUTES les infos
                        val token = loginResponse.accessToken
                        val userId = loginResponse.userId?.toString() ?: ""
                        val role = loginResponse.role ?: "USER"

                        Log.d("LOGIN", "=== LOGIN SUCCESS ===")
                        Log.d("LOGIN", "Token: ${token?.take(10)}...")
                        Log.d("LOGIN", "UserID: '$userId'")
                        Log.d("LOGIN", "Username: '$username'")
                        Log.d("LOGIN", "Role: '$role'")

                        if (token != null) {
                            // 🔥 ÉTAPE 1: Sauvegarder le token
                            sessionManager.saveAuthToken(token)

                            // 🔥 ÉTAPE 2: Sauvegarder l'ID (CRITIQUE!)
                            if (userId.isNotEmpty()) {
                                sessionManager.saveUserId(userId)
                                Log.d("LOGIN", "✅ UserID sauvegardé: '$userId'")
                            } else {
                                Log.w("LOGIN", "⚠️ UserID vide dans la réponse!")
                            }

                            // 🔥 ÉTAPE 3: Sauvegarder toutes les infos
                            sessionManager.saveUserInfo(
                                userId = userId,          // ID
                                username = username,      // Username
                                name = "",               // Nom complet (vide pour l'instant)
                                email = "",              // Email (vide pour l'instant)
                                role = role              // Rôle
                            )

                            // 🔥 DEBUG: Vérifier ce qui a été sauvegardé
                            debugSavedInfo()

                            Toast.makeText(
                                this@LoginActivity,
                                "✅ Connexion réussie!",
                                Toast.LENGTH_SHORT
                            ).show()

                            navigateToMain()
                        } else {
                            Toast.makeText(this@LoginActivity, "Token non reçu", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string() ?: "Pas de détails"
                        Log.e("LOGIN", "Erreur $errorCode: $errorBody")
                        Toast.makeText(
                            this@LoginActivity,
                            "Nom d'utilisateur ou mot de passe incorrect",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LOGIN", "Exception: ${e.message}", e)
                    Toast.makeText(this@LoginActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 🔥 NOUVELLE MÉTHODE: Debug des données sauvegardées
    private fun debugSavedInfo() {
        Log.d("LOGIN_DEBUG", "=== DONNÉES SAUVEGARDÉES ===")
        Log.d("LOGIN_DEBUG", "UserID: '${sessionManager.getUserId()}'")
        Log.d("LOGIN_DEBUG", "Username: '${sessionManager.getUsername()}'")
        Log.d("LOGIN_DEBUG", "Role: '${sessionManager.getUserRole()}'")
        Log.d("LOGIN_DEBUG", "Token présent: ${sessionManager.fetchAuthToken() != null}")

        // Afficher aussi dans un Toast
        Toast.makeText(this,
            """
            ✅ Login réussi!
            ID: ${sessionManager.getUserId() ?: "NULL"}
            User: ${sessionManager.getUsername() ?: "NULL"}
            """.trimIndent(),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateToMain() {
        val userRole = sessionManager.getUserRole()

        when (userRole?.uppercase()) {
            "NEEDY" -> {
                val intent = Intent(this, NeedyHomeActivity::class.java)
                startActivity(intent)
            }
            "ADMIN" -> {
                val intent = Intent(this, AdminHomeActivity::class.java)
                startActivity(intent)
            }
            else -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
        finish()
    }
}