package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.models.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // --- Initialisation des vues ---
        val etIdentifiant = findViewById<EditText>(R.id.etIdentifiant)
        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val spinnerRole = findViewById<Spinner>(R.id.spinnerRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginRedirect = findViewById<TextView>(R.id.tvLoginRedirect) // NOUVEAU

        // --- Configuration du Spinner pour les rôles ---
        val roles = arrayOf("NEEDY", "DONNATEUR", "ASSOCIATION")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        // --- Gestion du clic sur le bouton d'inscription ---
        btnRegister.setOnClickListener {
            val identifiant = etIdentifiant.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val selectedRole = spinnerRole.selectedItem.toString()

            // Vérification que les champs ne sont pas vides
            if (identifiant.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Vérification que les mots de passe correspondent
            if (password != confirmPassword) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si tout est valide, procéder à l'inscription
            registerUser(identifiant, firstName, lastName, email, password, selectedRole)
        }

        // NOUVEAU : Gestion du clic pour retourner au login
        tvLoginRedirect.setOnClickListener {
            finish() // Retourne à l'activité précédente (Login)
        }
    }

    private fun registerUser(identifiant: String, firstName: String, lastName: String, email: String, password: String, role: String) {
        val request = RegisterRequest(
            username = identifiant,
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = password,
            role = role
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.register(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Inscription réussie ! Veuillez vous connecter.", Toast.LENGTH_LONG).show()
                        // Rediriger vers ProfileActivity au lieu de fermer
                        val intent = Intent(this@RegisterActivity, ProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@RegisterActivity, "Erreur ${response.code()}: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}