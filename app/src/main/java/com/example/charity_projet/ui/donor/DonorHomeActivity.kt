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
import com.example.charity_projet.models.Post
import com.example.charity_projet.ui.AboutUsActivity
import com.example.charity_projet.ui.ChatbotActivity
import com.example.charity_projet.ui.LoginActivity
import com.example.charity_projet.ui.NotificationsActivity
import com.example.charity_projet.ui.ProfileActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DonorHomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var btnNotifications: ImageButton
    private lateinit var btnProfile: ImageButton
    private lateinit var btnLogout: ImageButton
    private lateinit var btnHelpNow: Button
    private lateinit var btnChatbot: Button

    // TextViews for statistics
    private lateinit var tvDonationsCount: TextView
    private lateinit var tvPeopleHelped: TextView
    private lateinit var tvFoodCount: TextView
    private lateinit var tvHealthCount: TextView
    private lateinit var tvEducationCount: TextView
    private lateinit var tvClothingCount: TextView
    private lateinit var tvHousingCount: TextView
    private lateinit var tvMoneyCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_home)

        sessionManager = SessionManager(this)

        // Authentication check
        checkAuthentication()

        initializeViews()
        setupAppBarListeners()
        setupNavigation()
        setupHelpNowButton()
        setupChatbotButton()

        // Load data
        loadDashboardData()

        // Show welcome message
        Toast.makeText(this, "Welcome Donor!", Toast.LENGTH_SHORT).show()
    }

    private fun checkAuthentication() {
        val token = sessionManager.fetchAuthToken()
        Log.d("DONOR_AUTH", "Token: ${token?.take(20)}...")

        if (token == null) {
            Log.e("DONOR_AUTH", "No token found")
            redirectToLogin()
            return
        }
    }

    private fun initializeViews() {
        btnNotifications = findViewById(R.id.btn_notifications)
        btnProfile = findViewById(R.id.btn_profile)
        btnLogout = findViewById(R.id.btn_logout)
        btnHelpNow = findViewById(R.id.btn_help_now)
        btnChatbot = findViewById(R.id.btn_chatbot)

        // Initialize statistics TextViews
        tvDonationsCount = findViewById(R.id.tv_donations_count)
        tvPeopleHelped = findViewById(R.id.tv_people_helped)
        tvFoodCount = findViewById(R.id.tv_food_count)
        tvHealthCount = findViewById(R.id.tv_health_count)
        tvEducationCount = findViewById(R.id.tv_education_count)
        tvClothingCount = findViewById(R.id.tv_clothing_count)
        tvHousingCount = findViewById(R.id.tv_housing_count)
        tvMoneyCount = findViewById(R.id.tv_money_count)
    }

    private fun setupAppBarListeners() {
        btnNotifications.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun setupNavigation() {
        findViewById<Button>(R.id.nav_home).setOnClickListener {
            // Refresh data
            loadDashboardData()
            Toast.makeText(this, "Home refreshed", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.nav_posts).setOnClickListener {
            // Open posts activity
            val intent = Intent(this, DonorPostsActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.nav_donations).setOnClickListener {
            Toast.makeText(this, "My Donations - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.nav_about).setOnClickListener {
            val intent = Intent(this, AboutUsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupHelpNowButton() {
        btnHelpNow.setOnClickListener {
            // Open posts activity
            val intent = Intent(this, DonorPostsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupChatbotButton() {
        btnChatbot.setOnClickListener {
            val intent = Intent(this, ChatbotActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDashboardData() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            showToast("Not logged in")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllPosts("Bearer $token")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val posts = response.body() ?: emptyList()
                        updateDashboardStats(posts)
                        showToast("${posts.size} requests available")
                    } else {
                        showToast("Error loading data")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Network error: ${e.message}")
                    // Show demo data in case of error
                    showDemoData()
                }
            }
        }
    }

    private fun updateDashboardStats(posts: List<Post>) {
        // Count posts by type
        val foodCount = posts.count { it.typeDemande == "NOURRITURE" }
        val healthCount = posts.count { it.typeDemande == "SANTE" }
        val educationCount = posts.count { it.typeDemande == "EDUCATION" }
        val clothingCount = posts.count { it.typeDemande == "VETEMENT" }
        val housingCount = posts.count { it.typeDemande == "LOGEMENT" }
        val moneyCount = posts.count { it.typeDemande == "ARGENT" }

        val totalPosts = posts.size

        // Update interface
        tvDonationsCount.text = "0" // Replace with actual donation data
        tvPeopleHelped.text = "0"   // Replace with actual people helped data

        tvFoodCount.text = "$foodCount requests"
        tvHealthCount.text = "$healthCount requests"
        tvEducationCount.text = "$educationCount requests"
        tvClothingCount.text = "$clothingCount requests"
        tvHousingCount.text = "$housingCount requests"
        tvMoneyCount.text = "$moneyCount requests"

        // Show message if no requests
        if (totalPosts == 0) {
            showToast("No requests available at the moment")
        }
    }

    private fun showDemoData() {
        // Demo data
        tvDonationsCount.text = "5"
        tvPeopleHelped.text = "12"
        tvFoodCount.text = "3 requests"
        tvHealthCount.text = "2 requests"
        tvEducationCount.text = "1 request"
        tvClothingCount.text = "4 requests"
        tvHousingCount.text = "1 request"
        tvMoneyCount.text = "2 requests"
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        sessionManager.clearAuth()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
