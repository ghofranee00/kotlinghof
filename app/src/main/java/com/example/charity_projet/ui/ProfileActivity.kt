
package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.LoginRequest
import com.example.charity_projet.ui.donor.DonorHomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnEditProfile: Button
    private lateinit var btnGoToNeedyHome: Button
    private lateinit var btnGoToAdminHome: Button // Nouveau bouton pour admin
    private lateinit var btnBack: ImageButton // Bouton de retour
    private lateinit var btnGoToDonorHome: Button // Nouveau bouton pour donor

    // Ajouter ces variables pour stocker les données
    private var currentFirstName: String = ""
    private var currentLastName: String = ""
    private var currentEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)
        initializeViews()
        loadUserProfile()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserRole = findViewById(R.id.tvUserRole)
        btnLogout = findViewById(R.id.btnLogout)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnGoToNeedyHome = findViewById(R.id.btnGoToNeedyHome)
        btnGoToAdminHome = findViewById(R.id.btnGoToAdminHome) // Initialiser le bouton admin
        btnGoToDonorHome = findViewById(R.id.btnGoToDonorHome) // Initialiser le bouton donor

        btnBack = findViewById(R.id.btnBack) // Bouton de retour
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            logoutUser()
        }

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("firstName", currentFirstName)
            intent.putExtra("lastName", currentLastName)
            intent.putExtra("email", currentEmail)
            startActivity(intent)
        }

        // Listener pour le bouton Needy Home
        btnGoToNeedyHome.setOnClickListener {
            goToNeedyHome()
        }

        // Listener pour le bouton Admin Home
        btnGoToAdminHome.setOnClickListener {
            goToAdminHome()
        }
        // Listener pour le bouton Donor Home
        btnGoToDonorHome.setOnClickListener {
            goToDonorHome()
        }
        // Listener pour le bouton de retour
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun goToNeedyHome() {
        val intent = Intent(this, NeedyHomeActivity::class.java)
        startActivity(intent)
    }

    private fun goToAdminHome() {
        val intent = Intent(this, AdminHomeActivity::class.java)
        startActivity(intent)
    }

    private fun goToDonorHome() {
        val intent = Intent(this, DonorHomeActivity::class.java)
        startActivity(intent)
    }
    private fun loadUserProfile() {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.instance.getProfile("Bearer $token")

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val user = response.body()
                            user?.let {
                                // Stocker les données dans les variables
                                currentFirstName = it.firstName ?: ""
                                currentLastName = it.lastName ?: ""
                                currentEmail = it.email ?: ""

                                // Afficher les données
                                tvUserName.text = "${currentFirstName} ${currentLastName}"
                                tvUserEmail.text = currentEmail
                                tvUserRole.text = it.role ?: "No role"

                                // Sauvegarder dans SessionManager aussi
                                sessionManager.saveUserInfo(
                                    username = sessionManager.getUsername() ?: "",
                                    name = "${currentFirstName} ${currentLastName}",
                                    email = currentEmail,
                                    role = it.role ?: "USER"
                                )

                                // Afficher/masquer les boutons selon le rôle
                                updateButtonVisibility(it.role)

                                Toast.makeText(this@ProfileActivity, "Profile loaded successfully", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            loadFromLocalData()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        loadFromLocalData()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadFromLocalData() {
        // Utiliser les données du SessionManager
        val fullName = sessionManager.getUserName().orEmpty()
        currentEmail = sessionManager.getUserEmail().orEmpty()

        // Séparer prénom et nom
        val nameParts = fullName.split(" ")
        if (nameParts.size >= 2) {
            currentFirstName = nameParts[0]
            currentLastName = nameParts.subList(1, nameParts.size).joinToString(" ")
        } else {
            currentFirstName = fullName
            currentLastName = ""
        }

        // Afficher les données
        tvUserName.text = fullName
        tvUserEmail.text = currentEmail

        val userRole = sessionManager.getUserRole() ?: "No role"
        tvUserRole.text = userRole

        // Mettre à jour la visibilité des boutons
        updateButtonVisibility(userRole)
    }

    private fun updateButtonVisibility(userRole: String?) {
        val role = userRole?.uppercase() ?: ""

        when (role) {
            "NEEDY" -> {
                btnGoToNeedyHome.visibility = View.VISIBLE
                btnGoToAdminHome.visibility = View.GONE  // CORRIGÉ : View.GONE
                btnGoToDonorHome.visibility = View.GONE  // CORRIGÉ : View.GONE
            }
            "ADMIN" -> {
                btnGoToNeedyHome.visibility = View.GONE  // CORRIGÉ : View.GONE
                btnGoToAdminHome.visibility = View.VISIBLE
                btnGoToDonorHome.visibility = View.GONE  // CORRIGÉ : View.GONE
            }
            "DONNATEUR", "ASSOCIATION" -> {  // Si vous voulez gérer deux rôles
                btnGoToNeedyHome.visibility = View.GONE  // CORRIGÉ : View.GONE
                btnGoToAdminHome.visibility = View.GONE  // CORRIGÉ : View.GONE
                btnGoToDonorHome.visibility = View.VISIBLE
            }
            else -> {
                btnGoToNeedyHome.visibility = View.GONE  // CORRIGÉ : View.GONE
                btnGoToAdminHome.visibility = View.GONE  // CORRIGÉ : View.GONE
                btnGoToDonorHome.visibility = View.GONE  // CORRIGÉ : View.GONE
            }
        }
    }
    private fun logoutUser() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        RetrofitClient.instance.logout()
                    } catch (e: Exception) {
                        println("Logout API error: ${e.message}")
                    }
                }

                sessionManager.clearAuthToken()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

  /*  override fun onBackPressed() {
        // Rediriger vers la page appropriée selon le rôle
        val userRole = sessionManager.getUserRole()?.uppercase()

        when (userRole) {
            "ADMIN" -> {
                val intent = Intent(this, AdminHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            "NEEDY" -> {
                val intent = Intent(this, NeedyHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> {
                super.onBackPressed() // Comportement par défaut
            }
        }
    }*/
}