package com.example.charity_projet.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.databinding.ItemDemandeBinding
import com.example.charity_projet.models.Demande



class DemandeAdapter(
    private var demandes: List<Demande>,
    private val onAcceptClick: (Demande) -> Unit,  // ⚠️ CORRECTION
    private val onRefuseClick: (Demande) -> Unit   // ⚠️ CORRECTION
) : RecyclerView.Adapter<DemandeAdapter.DemandeViewHolder>() {

    inner class DemandeViewHolder(private val binding: ItemDemandeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(demande: Demande) {
            binding.tvContenu.text = demande.contenu ?: "Contenu non disponible"
            binding.tvType.text = "Type: ${demande.typeDemande ?: "Non spécifié"}"
            binding.tvEtat.text = "État: ${demande.etat ?: "Inconnu"}"

            val userName = demande.user?.identifiant ?: demande.user?.email ?: "Utilisateur inconnu"
            binding.tvUser.text = "Par: $userName"

            // ⚠️ CORRECTION: Utiliser les callbacks séparés
            binding.btnAccepter.setOnClickListener {
                onAcceptClick(demande)
            }

            binding.btnRefuser.setOnClickListener {
                onRefuseClick(demande)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemandeViewHolder {
        val binding = ItemDemandeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DemandeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DemandeViewHolder, position: Int) {
        holder.bind(demandes[position])
    }

    override fun getItemCount(): Int = demandes.size

    fun updateData(newDemandes: List<Demande>) {
        demandes = newDemandes
        notifyDataSetChanged()
    }

    // Nouvelle méthode pour retirer une demande
    fun removeDemande(demande: Demande) {
        demandes = demandes.toMutableList().apply { remove(demande) }
        notifyDataSetChanged()
    }
}