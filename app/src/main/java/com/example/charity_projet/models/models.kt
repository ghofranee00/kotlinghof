package com.example.charity_projet.models

import com.google.gson.annotations.SerializedName

// --- Modèles d'authentification ---


data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("expires_in")
    val expiresIn: Int?,
    @SerializedName("refresh_token")
    val refreshToken: String?,
    @SerializedName("user_id")
    val userId: Long?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("role")
    val role: String?
)

data class RegisterRequest(
    val username: String,  // ⚠️ Changer identifiant → username
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val role: String // ex: "USER"
)

// --- Modèles existants ---

// Modèle User (enrichi)
data class User(
    @SerializedName("id")
    private val idServer: String? = null,
    @SerializedName("_id")
    private val idAlt: String? = null,
    @SerializedName("identifiant")
    val identifiant: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("role")
    val role: String? = null,
    @SerializedName("dateCreation")
    val dateCreation: String? = null
){
    fun getId(): String? = idServer ?: idAlt

}
data class UserUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
)

data class PasswordUpdateRequest(
    val currentPassword: String,
    val newPassword: String
)

// Modèle Demande
data class Demande(
    @SerializedName("id")
    private val idServer: String? = null, // renommé pour éviter le conflit JVM

    @SerializedName("_id")
    private val idAlt: String? = null,    // reste privé

    @SerializedName("contenu")
    val contenu: String? = null,

    @SerializedName("typeDemande")
    val typeDemande: String? = null,

    @SerializedName("etat")
    val etat: String? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("dateCreation")
    val dateCreation: String? = null,

    @SerializedName("likes")
    var likes: Int = 0,                        // var pour pouvoir modifier

    @SerializedName("comments")
    var comments: List<Commentaire> = emptyList(),

    @SerializedName("shares")
    var shares: Int = 0
) {
    fun getId(): String? = idServer ?: idAlt
}

// Modèle ApiResponse
data class ApiResponse(
    @SerializedName("statut")
    val statut: String? = null,

    @SerializedName("notifications")
    val notifications: List<NotificationResponse>? = null
)

// Modèle NotificationResponse
data class NotificationResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("destinataire")
    val destinataire: String? = null
)


// Modèle Commentaire
data class Commentaire(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("contenu")
    val contenu: String? = null,

    @SerializedName("dateCreation")
    val dateCreation: String? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("post")
    val post: Post? = null
)

// Modèle pour envoyer un commentaire
data class CommentRequest(
    val contenu: String
)
data class SummaryReportDTO(
    @SerializedName("totalDonations") val totalDonations: Long,
    @SerializedName("totalUsers") val totalUsers: Long,
    @SerializedName("donationsByCategory") val donationsByCategory: Map<String, Long>,
    @SerializedName("totalPosts") val totalPosts: Long,
    @SerializedName("totalDemandes") val totalDemandes: Long,
    @SerializedName("usersByRole") val usersByRole: Map<String, Long>
)



data class Post(
    @SerializedName("id")
    private val idServer: String? = null,

    @SerializedName("_id")
    private val idAlt: String? = null,

    @SerializedName("contenu")
    val contenu: String? = null,

    @SerializedName("typeDemande")
    val typeDemande: String? = null,

    @SerializedName("dateCreation")
    val dateCreation: String? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("imageUrls")
    val imageUrls: List<String> = emptyList(),

    @SerializedName("videoUrls")
    val videoUrls: List<String> = emptyList(),

    @SerializedName("likesCount")
    val likesCount: Int = 0,

    @SerializedName("commentsCount")
    val commentsCount: Int = 0,

    @SerializedName("likedByUserIds")
    val likedByUserIds: List<String> = emptyList()
) {
    fun getId(): String? = idServer ?: idAlt
}


data class Notification(
    @SerializedName("id")
    private val idServer: String? = null,

    @SerializedName("_id")
    private val idAlt: String? = null,

    @SerializedName("titre")
    val titre: String? = null,

    @SerializedName("contenu")
    val contenu: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("utilisateur")
    val utilisateur: User? = null,

    @SerializedName("demande")
    val demande: Demande? = null,

    @SerializedName("lue")
    var lue: Boolean = false, // ✅ Changez en 'var' au lieu de 'val'

    @SerializedName("dateCreation")
    val dateCreation: String? = null
) {
    fun getId(): String? = idServer ?: idAlt
}



data class DonationDTO(
    @SerializedName("id") val id: String? = null,
    @SerializedName("post") val post: Post? = null,
    @SerializedName("donor") val donor: User? = null,
    @SerializedName("dateDonation") val dateDonation: String? = null,
    @SerializedName("categorie") val categorie: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("details") val details: String? = null,
    @SerializedName("images") val images: List<String>? = emptyList(),
    @SerializedName("status") val status: String? = null
) {
    fun isEnAttente(): Boolean = status == "EN_ATTENTE"
    fun isAcceptee(): Boolean = status == "ACCEPTEE"
    fun isRefusee(): Boolean = status == "REFUSEE"
}

// Enum pour les catégories
enum class DonationCategorie {
    NOURRITURE,
    HABILLEMENT,
    MEDICAMENTS,
    EQUIPEMENTS,
    ARGENT,
    AUTRE
}

// Enum pour les statuts
enum class DonationStatus {
    EN_ATTENTE,
    ACCEPTEE,
    REFUSEE,

}


// DonationRequest.kt
data class DonationRequest(
    @SerializedName("postId")
    val postId: String,

    @SerializedName("donorId")
    val donorId: String,

    @SerializedName("categorie")
    val categorie: String,

    @SerializedName("region")
    val region: String,

    @SerializedName("details")
    val details: String,

    @SerializedName("images")
    val images: List<String> = emptyList()
)
