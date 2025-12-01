package com.example.charity_projet.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.api.SessionManager
import com.example.charity_projet.models.Demande
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/*
class DonationsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var donationsAdapter: DonationsAdapter
    private val donationsList = mutableListOf<Demande>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donations)

        sessionManager = SessionManager(this)
        initializeViews()
        setupBackButton()
        loadDonations()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recycler_donations)
        progressBar = findViewById(R.id.progress_bar)
        tvEmpty = findViewById(R.id.tv_empty)

        // Setup RecyclerView
        donationsAdapter = DonationsAdapter(donationsList) { donation ->
            showDonationDetails(donation)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = donationsAdapter
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun loadDonations() {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllDemandes()

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val demandes = response.body() ?: emptyList()
                        // Filtrer les demandes selon vos critères (ex: financières)
                        val financialDonations = demandes.filter {
                            it.typeDemande == "FINANCIAL" && it.etat == "ACCEPTEE"
                        }

                        donationsList.clear()
                        donationsList.addAll(financialDonations)
                        donationsAdapter.notifyDataSetChanged()

                        if (financialDonations.isEmpty()) {
                            showEmptyState(true)
                        } else {
                            showEmptyState(false)
                        }
                    } else {
                        showToast("Erreur lors du chargement des donations")
                        showEmptyState(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Erreur réseau: ${e.message}")
                    showEmptyState(true)
                }
            }
        }
    }

    private fun showDonationDetails(donation: Demande) {
        Toast.makeText(this, "Détails donation: ${donation.contenu?.take(50)}...", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        tvEmpty.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}*/