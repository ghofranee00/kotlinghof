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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.Demande
import com.example.charity_projet.models.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnBack: ImageButton
    private lateinit var spinnerFilter: Spinner

    private var allPosts = mutableListOf<Post>()
    private var filteredPosts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        sessionManager = SessionManager(this)
        setupViews()
        setupSpinner()
        loadAllPosts()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recycler_posts)
        progressBar = findViewById(R.id.progress_bar)
        tvEmpty = findViewById(R.id.tv_empty)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnBack = findViewById(R.id.btn_back)
        spinnerFilter = findViewById(R.id.spinner_filter)

        // Setup RecyclerView - Admin voit seulement
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(posts = emptyList())
        recyclerView.adapter = adapter

        // Listeners
        btnBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener { loadAllPosts() }
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

        filteredPosts = if (selectedType == "Tous les types") {
            allPosts
        } else {
            allPosts.filter { post ->
                post.typeDemande == selectedType
            }.toMutableList()
        }

        showPosts(filteredPosts)
    }

    private fun loadAllPosts() {
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
                val response = RetrofitClient.instance.getAllPosts("Bearer $token")

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val posts = response.body() ?: emptyList()
                        allPosts = posts.toMutableList()
                        applyTypeFilter()
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

    private fun showPosts(posts: List<Post>) {
        adapter.updateData(posts)
        if (posts.isEmpty()) {
            val selectedType = spinnerFilter.selectedItem.toString()
            val message = if (selectedType == "Tous les types") {
                "Aucun post disponible"
            } else {
                "Aucun post de type $selectedType"
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