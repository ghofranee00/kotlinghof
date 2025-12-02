package com.example.charity_projet.ui.donor

import android.os.Bundle
import android.util.Log
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
import com.example.charity_projet.models.Commentaire
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentsActivity : AppCompatActivity(), CommentsAdapter.CommentClickListener {

    private lateinit var sessionManager: SessionManager
    private lateinit var currentUserId: String
    private lateinit var postId: String
    private lateinit var postContent: String
    private lateinit var postType: String

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommentsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var etComment: EditText
    private lateinit var btnSend: Button
    private lateinit var tvPostContent: TextView
    private lateinit var tvPostType: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        // Récupérer les données
        postId = intent.getStringExtra("POST_ID") ?: ""
        postContent = intent.getStringExtra("POST_CONTENT") ?: "No content"
        postType = intent.getStringExtra("POST_TYPE") ?: "General"

        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUserId() ?: ""

        // Vérifier les données
        if (postId.isEmpty() || currentUserId.isEmpty()) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadComments()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recycler_view_comments)
        progressBar = findViewById(R.id.progress_bar)
        tvEmpty = findViewById(R.id.tv_empty)
        btnBack = findViewById(R.id.btn_back)
        etComment = findViewById(R.id.et_comment)
        btnSend = findViewById(R.id.btn_send_comment)
        tvPostContent = findViewById(R.id.tv_post_content)
        tvPostType = findViewById(R.id.tv_post_type)

        tvPostContent.text = postContent
        tvPostType.text = postType

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CommentsAdapter(emptyList(), this, currentUserId)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnSend.setOnClickListener {
            val commentText = etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                postComment(commentText)
            } else {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadComments() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            showToast("Please login first")
            finish()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getComments(
                    token = "Bearer $token",
                    postId = postId
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        val comments = response.body() ?: emptyList()
                        adapter.updateComments(comments)
                        showEmptyState(comments.isEmpty())
                    } else {
                        showToast("Error loading comments: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Network error: ${e.message}")
                }
            }
        }
    }

    private fun postComment(commentText: String) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            showToast("Please login first")
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val commentRequest = CommentRequest(contenu = commentText)
                val response = RetrofitClient.instance.addComment(
                    token = "Bearer $token",
                    postId = postId,
                    commentRequest = commentRequest
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        showToast("Comment posted!")
                        etComment.text.clear()
                        loadComments()
                    } else {
                        showToast("Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Network error: ${e.message}")
                }
            }
        }
    }

    // Interface CommentClickListener
    override fun onDeleteClick(comment: Commentaire) {
        AlertDialog.Builder(this)
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                deleteComment(comment.id ?: "")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteComment(commentId: String) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            showToast("Please login first")
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.deleteComment(
                    token = "Bearer $token",
                    commentId = commentId
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        showToast("Comment deleted!")
                        loadComments()
                    } else {
                        showToast("Error deleting comment: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Network error: ${e.message}")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSend.isEnabled = !show
        etComment.isEnabled = !show
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            tvEmpty.text = "No comments yet"
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}