package com.example.charity_projet.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnBack: ImageButton
    private lateinit var spinnerFilter: Spinner

    private var allUsers = mutableListOf<User>()
    private var filteredUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        sessionManager = SessionManager(this)
        setupViews()
        setupSpinner()
        loadAllUsers()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recycler_users)
        progressBar = findViewById(R.id.progress_bar)
        tvEmpty = findViewById(R.id.tv_empty)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnBack = findViewById(R.id.btn_back)
        spinnerFilter = findViewById(R.id.spinner_filter)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Utiliser la référence de méthode
        adapter = UserAdapter(
            users = emptyList(),
            onDeleteClick = this::deleteUser
        )
        recyclerView.adapter = adapter

        // Listeners
        btnBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener { loadAllUsers() }
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf("Tous les rôles", "NEEDY", "ADMIN", "DONOR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyRoleFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyRoleFilter() {
        val selectedRole = spinnerFilter.selectedItem.toString()

        filteredUsers = if (selectedRole == "Tous les rôles") {
            allUsers
        } else {
            allUsers.filter { user ->
                user.role == selectedRole
            }.toMutableList()
        }

        showUsers(filteredUsers)
    }

    private fun loadAllUsers() {
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
                val response = RetrofitClient.instance.getAllUsers("Bearer $token")

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val users = response.body() ?: emptyList()
                        allUsers = users.toMutableList()
                        applyRoleFilter()
                    } else {
                        when (response.code()) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé")
                            else -> showToast("Erreur: ${response.code()}")
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

    private fun deleteUser(user: User) {
        val token = sessionManager.fetchAuthToken()
        val userId = user.getId()

        if (token == null || userId.isNullOrEmpty()) {
            showToast("Erreur d'authentification")
            return
        }

        // Empêcher l'admin de se supprimer lui-même
        val currentUsername = sessionManager.getUsername()
        if (user.identifiant == currentUsername) {
            showToast("Vous ne pouvez pas supprimer votre propre compte")
            return
        }

        // Confirmation avant suppression
        AlertDialog.Builder(this)
            .setTitle("Supprimer l'utilisateur")
            .setMessage("Êtes-vous sûr de vouloir supprimer le compte de ${user.identifiant} ?\nCette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                processDeleteUser(userId, token)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun processDeleteUser(userId: String, token: String) {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.deleteUser(
                    token = "Bearer $token",
                    id = userId
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        showToast("Utilisateur supprimé avec succès")
                        // Retirer l'utilisateur des listes
                        allUsers.removeAll { it.getId() == userId }
                        applyRoleFilter()
                    } else {
                        when (response.code()) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé")
                            404 -> showToast("Utilisateur non trouvé")
                            else -> showToast("Erreur: ${response.code()}")
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

    private fun showUsers(users: List<User>) {
        adapter.updateData(users)
        if (users.isEmpty()) {
            val selectedRole = spinnerFilter.selectedItem.toString()
            val message = if (selectedRole == "Tous les rôles") {
                "Aucun utilisateur trouvé"
            } else {
                "Aucun utilisateur avec le rôle $selectedRole"
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