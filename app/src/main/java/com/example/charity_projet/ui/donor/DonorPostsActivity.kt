package com.example.charity_projet.ui.donor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.CommentRequest
import com.example.charity_projet.models.Post
import com.example.charity_projet.ui.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DonorPostsActivity : AppCompatActivity(), DonorPostAdapter.PostClickListener {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DonorPostAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnBack: ImageButton
    private lateinit var spinnerFilter: Spinner

    private var allPosts = mutableListOf<Post>()
    private var filteredPosts = mutableListOf<Post>()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_posts)

        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUserId() // Récupérer l'ID utilisateur

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

        // Setup RecyclerView avec le nouvel adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DonorPostAdapter(emptyList(), this, currentUserId) // Passer currentUserId
        recyclerView.adapter = adapter

        // Listeners
        btnBack.setOnClickListener { finish() }
        btnRefresh.setOnClickListener { loadAllPosts() }
    }

    private fun setupSpinner() {
        val filterOptions = arrayOf(
            "Tous les types",
            "SANTE",
            "ARGENT",
            "EDUCATION",
            "NOURRITURE",
            "VETEMENT",
            "LOGEMENT"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
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
                        showToast("${posts.size} posts chargés")
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
        adapter.updatePosts(posts)
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

    // Interface PostClickListener - TOUTES LES MÉTHODES
    override fun onHelpClick(post: Post) {
        val postId = post.getId() ?: run {
            Log.e("DonorPosts", "Post ID is null! Post: $post")
            Toast.makeText(this, "Error: Invalid post", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("DonorPosts", "onHelpClick - Post ID: $postId")

        AlertDialog.Builder(this)
            .setTitle("Help - ${post.typeDemande ?: "Request"}")
            .setMessage("Do you want to help with this request?\n\n${post.contenu ?: "No content"}")
            .setPositiveButton("Yes, I want to help") { dialog, which ->
                val intent = Intent(this, CreateDonationActivity::class.java).apply {
                    putExtra("POST_ID", postId)
                    putExtra("POST_CONTENT", post.contenu ?: "")
                    putExtra("POST_TYPE", post.typeDemande ?: "GENERAL")
                }
                startActivityForResult(intent, REQUEST_CODE_CREATE_DONATION)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onShareClick(post: Post) {
        val shareText = """
            📢 Post Charity App
            Type: ${post.typeDemande}
            Contenu: ${post.contenu}
            Date: ${formatDate(post.dateCreation)}
            
            Rejoignez-nous pour aider !
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Post Charity App - ${post.typeDemande}")
        }
        startActivity(Intent.createChooser(shareIntent, "Partager ce post"))
    }

    override fun onLikeClick(post: Post) {
        toggleLike(post)
    }

    override fun onCommentClick(post: Post) {
        showCommentDialog(post)
    }

    private fun toggleLike(post: Post) {
        val token = sessionManager.fetchAuthToken()
        val postId = post.getId()


        // DEBUG: Afficher le token
        Log.d("DonorPosts", "=== DEBUG TOKEN ===")
        Log.d("DonorPosts", "Token complet: $token")
        Log.d("DonorPosts", "Token length: ${token?.length}")
        Log.d("DonorPosts", "User ID from session: $currentUserId")
        Log.d("DonorPosts", "Post ID: $postId")
        Log.d("DonorPosts", "=================")


        if (token == null || postId == null) {
            showToast("Veuillez vous connecter d'abord")
            return
        }

        val isCurrentlyLiked = post.likedByUserIds?.contains(currentUserId) ?: false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (isCurrentlyLiked) {
                    // DELETE pour enlever le like
                    RetrofitClient.instance.unlikePost(
                        token = "Bearer $token",
                        postId = postId
                    )
                } else {
                    // POST pour ajouter un like
                    RetrofitClient.instance.likePost(
                        token = "Bearer $token",
                        postId = postId
                    )
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val message = if (isCurrentlyLiked) "Like retiré" else "Post aimé!"
                        showToast(message)

                        // Rafraîchir les données
                        loadAllPosts()
                    } else {
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"

                        Log.e("DonorPosts", "Like erreur $errorCode: $errorBody")

                        when (errorCode) {
                            401 -> {
                                showToast("Session expirée")
                                redirectToLogin()
                            }
                            else -> showToast("Erreur: $errorCode")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Erreur réseau: ${e.message}")
                }
            }
        }
    }    private fun showCommentDialog(post: Post) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment, null)
        val etComment = dialogView.findViewById<EditText>(R.id.et_comment)

        AlertDialog.Builder(this)
            .setTitle("Add a comment")
            .setView(dialogView)
            .setPositiveButton("Post") { dialog, which ->
                val commentText = etComment.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    postComment(post, commentText)
                } else {
                    showToast("Comment cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun postComment(post: Post, commentText: String) {
        val token = sessionManager.fetchAuthToken()
        val postId = post.getId()

        if (token == null || postId == null) {
            showToast("Veuillez vous connecter d'abord")
            return
        }

        // Log pour debug
        Log.d("DonorPosts", "Envoi commentaire - PostId: $postId, Texte: $commentText")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Utilisez CommentRequest correctement
                val commentRequest = CommentRequest(contenu = commentText)

                Log.d("DonorPosts", "CommentRequest créé: $commentRequest")

                val response = RetrofitClient.instance.addComment(
                    token = "Bearer $token",
                    postId = postId,
                    commentRequest = commentRequest
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast("Commentaire posté avec succès!")
                        // Rafraîchir les posts
                        loadAllPosts()
                    } else {
                        // Debug détaillé
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string() ?: "Pas de message d'erreur"

                        Log.e("DonorPosts", "Erreur HTTP $errorCode: $errorBody")

                        when (errorCode) {
                            400 -> showToast("Erreur 400 - Requête invalide: $errorBody")
                            401 -> {
                                showToast("Session expirée, veuillez vous reconnecter")
                                redirectToLogin()
                            }
                            403 -> showToast("Accès refusé")
                            else -> showToast("Erreur serveur: $errorCode")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DonorPosts", "Exception réseau: ${e.message}", e)
                    showToast("Erreur réseau: ${e.message}")
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("DonorPosts", "onActivityResult called: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == REQUEST_CODE_CREATE_DONATION) {
            when (resultCode) {
                RESULT_OK -> {
                    Log.d("DonorPosts", "Donation created successfully!")
                    showToast("Donation created successfully!")
                    loadAllPosts()
                }
                RESULT_CANCELED -> {
                    Log.d("DonorPosts", "Donation creation cancelled")
                    showToast("Donation creation cancelled")
                }
                else -> {
                    Log.d("DonorPosts", "Unknown result code: $resultCode")
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

    private fun formatDate(date: String?): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            val dateObj = inputFormat.parse(date ?: "")
            outputFormat.format(dateObj ?: Date())
        } catch (e: Exception) {
            date ?: "Date inconnue"
        }
    }

    companion object {
        private const val REQUEST_CODE_CREATE_DONATION = 1001
    }
}