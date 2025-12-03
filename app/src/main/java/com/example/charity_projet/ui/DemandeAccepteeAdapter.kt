package com.example.charity_projet.ui

import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.databinding.ItemDemandeAccepteeBinding
import com.example.charity_projet.models.CommentRequest
import com.example.charity_projet.models.Commentaire
import com.example.charity_projet.models.Demande
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DemandeAccepteeAdapter(
    private var demandes: List<Demande>,
    private val context: Context
) : RecyclerView.Adapter<DemandeAccepteeAdapter.ViewHolder>() {

    private val apiService = RetrofitClient.instance
    private val adapterScope = CoroutineScope(Dispatchers.Main)

    inner class ViewHolder(private val binding: ItemDemandeAccepteeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(demande: Demande) {
            binding.tvTitre.text = "Titre : ${demande.contenu ?: "Titre non disponible"}"
            binding.tvType.text = "Type : ${demande.typeDemande ?: "Non spécifié"}"

            binding.tvLikeCount.text = "${demande.likes} Likes"
            binding.tvCommentCount.text = "${demande.comments.size} Commentaires"
            binding.tvShareCount.text = "${demande.shares} Partages"

            binding.ivLike.setOnClickListener {
                adapterScope.launch {
                    try {
                        val response = apiService.likeDemande(demande.getId() ?: "")
                        if (response.isSuccessful) {
                            val updatedDemande = response.body()
                            updatedDemande?.let {
                                demande.likes = it.likes
                                binding.tvLikeCount.text = "${demande.likes} Likes"
                                val pos = bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
                            }
                            Toast.makeText(context, "Vous avez aimé cette demande !", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erreur lors du like", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.ivShare.setOnClickListener {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Demande: ${demande.contenu}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
            }

            binding.ivComment.setOnClickListener {
                val editText = EditText(context)
                AlertDialog.Builder(context)
                    .setTitle("Ajouter un commentaire")
                    .setView(editText)
                    .setPositiveButton("Envoyer") { _, _ ->
                        val contenuCommentaire = editText.text.toString()
                        if (contenuCommentaire.isNotBlank()) {
                            adapterScope.launch {
                                try {
                                    val response = apiService.addComment(
                                        demande.getId() ?: "",
                                        CommentRequest(contenuCommentaire)
                                    )
                                    if (response.isSuccessful) {
                                        val nouveauCommentaire: Commentaire? = response.body()
                                        nouveauCommentaire?.let {
                                            demande.comments = demande.comments + it
                                            binding.tvCommentCount.text = "${demande.comments.size} Commentaires"
                                            val pos = bindingAdapterPosition
                                            if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
                                        }
                                        Toast.makeText(context, "Commentaire ajouté", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Erreur lors de l'ajout du commentaire", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .setNegativeButton("Annuler", null)
                    .create()
                    .show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDemandeAccepteeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(demandes[position])
    }

    override fun getItemCount(): Int = demandes.size

    fun updateData(newList: List<Demande>) {
        demandes = newList
        notifyDataSetChanged()
    }
}