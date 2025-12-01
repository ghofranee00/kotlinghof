package com.example.charity_projet.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.models.Demande

class DemandeNeedyAdapter(
    private var demandes: List<Demande> = emptyList(),
    private val onEditClick: (Demande) -> Unit = {},
    private val onDeleteClick: (Demande) -> Unit = {}
) : RecyclerView.Adapter<DemandeNeedyAdapter.DemandeViewHolder>() {

    inner class DemandeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContenu: TextView = itemView.findViewById(R.id.tvContenu)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvEtat: TextView = itemView.findViewById(R.id.tvEtat)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(demande: Demande) {
            tvContenu.text = demande.contenu ?: "Contenu non disponible"
            tvType.text = "Type: ${demande.typeDemande ?: "Non spécifié"}"
            tvEtat.text = "État: ${demande.etat ?: "Inconnu"}"

            // Formater la date si elle existe
            tvDate.text = if (!demande.dateCreation.isNullOrEmpty()) {
                "Créée le: ${formatDate(demande.dateCreation)}"
            } else {
                "Date non disponible"
            }

            // Bouton Modifier
            btnEdit.setOnClickListener {
                onEditClick(demande)
            }

            // Bouton Supprimer
            btnDelete.setOnClickListener {
                showDeleteConfirmation(demande)
            }

            // Cacher les boutons si la demande n'est plus en attente
            val isEditable = demande.etat == "EN_ATTENTE"
            btnEdit.visibility = if (isEditable) View.VISIBLE else View.GONE
            btnDelete.visibility = if (isEditable) View.VISIBLE else View.GONE
        }

        private fun formatDate(dateString: String): String {
            return try {
                dateString.substring(0, 10)
            } catch (e: Exception) {
                dateString
            }
        }

        private fun showDeleteConfirmation(demande: Demande) {
            val context = itemView.context
            AlertDialog.Builder(context)
                .setTitle("Confirmer la suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette demande ?")
                .setPositiveButton("Supprimer") { dialog, which ->
                    onDeleteClick(demande)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemandeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_demande_needy, parent, false)
        return DemandeViewHolder(view)
    }

    override fun onBindViewHolder(holder: DemandeViewHolder, position: Int) {
        holder.bind(demandes[position])
    }

    override fun getItemCount(): Int = demandes.size

    fun updateData(newDemandes: List<Demande>) {
        demandes = newDemandes
        notifyDataSetChanged()
    }
}