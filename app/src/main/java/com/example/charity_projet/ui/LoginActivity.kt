package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
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
        if (sessionManager.fetchAuthToken() != null) {
            navigateToMain()
            return // Empêche l'affichage de LoginActivity
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
                        val token = response.body()!!.accessToken
                        if (token != null) {
                            sessionManager.saveAuthToken(token)
                            navigateToMain()
                        } else {
                            Toast.makeText(this@LoginActivity, "Token non reçu", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
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
    }}