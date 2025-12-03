package com.example.charity_projet.api

import com.example.charity_projet.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface Api {

    // --- Authentification ---

    @POST("keycloak/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("keycloak/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>

    @GET("keycloak/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>
    @POST("keycloak/logout")
    suspend fun logout(): Response<Void>

    @PUT("keycloak/profile/{username}")
    suspend fun updateProfile(
        @Path("username") username: String,
        @Body updateDto: UserUpdateRequest
    ): Response<User>
    @PUT("keycloak/profile/{username}/password")
    suspend fun updatePassword(
        @Path("username") username: String,
        @Body passwordDto: PasswordUpdateRequest
    ): Response<Void>
    @GET("admin/summary-report")
    suspend fun getAdminSummaryReport(@Header("Authorization") token: String): Response<SummaryReportDTO>


    @Multipart
    @POST("demandes/ajouter")
    suspend fun createDemande(
        @Header("Authorization") token: String,
        @Part demande: MultipartBody.Part,
        @Part images: List<MultipartBody.Part>?,
        @Part videos: List<MultipartBody.Part>?
    ): Response<Demande>

    // 🔥 Nouvel endpoint pour récupérer les demandes du NEEDY
    @GET("demandes/needy/mes-demandes/en-attente")
    suspend fun getMesDemandesEnAttente(
        @Header("Authorization") token: String
    ): Response<List<Demande>>

    @GET("demandes/needy/mes-demandes")
    suspend fun getMesDemandes(
        @Header("Authorization") token: String
    ): Response<List<Demande>>
    // Endpoint pour modifier une demande
    @PUT("demandes/needy/{id}")
    suspend fun updateDemandeNeedy(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("userId") userId: String,
        @Body demande: Demande
    ): Response<Demande>

    // Endpoint pour supprimer une demande
    @DELETE("demandes/needy/{id}")
    suspend fun deleteDemandeNeedy(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("userId") userId: String
    ): Response<ApiResponse>

    @GET("demandes/acceptees")
    suspend fun getDemandesAcceptees(): Response<List<Demande>>

    @GET("demandes/liste")
    suspend fun getAllDemandes(
        @Header("Authorization") token: String
    ): Response<List<Demande>>

    // ⚠️ Correction précédente: ajout du Body et modification URL pour traiterDemande
    @PUT("demandes/statut/{id}")
    suspend fun traiterDemande(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("action") action: String,
        @Body body: RequestBody
    ): Response<ApiResponse>

    @GET("demandes/etat/{etat}")
    suspend fun getDemandesByEtat(@Path("etat") etat: String): Response<List<Demande>>

    @PUT("demandes/accept-all")
    suspend fun accepterToutesDemandes(): Response<ApiResponse>
    @POST("demandes/{id}/like")
    suspend fun likeDemande(@Path("id") id: String): Response<Demande>

    @POST("demandes/{id}/comment")
    suspend fun addComment(
        @Path("id") demandeId: String,
        @Body commentRequest: CommentRequest
    ): Response<Commentaire>

    @GET("demandes/{id}/comments")
    suspend fun getComments(@Path("id") demandeId: String): Response<List<Commentaire>>


    @GET("posts")
    suspend fun getAllPosts(
        @Header("Authorization") token: String
    ): Response<List<Post>>

    @GET("posts/user/{userId}")
    suspend fun getPostsByUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<List<Post>>

    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body post: Post
    ): Response<Post>


    // Likes - Pas besoin de body, seulement le token
    @POST("posts/{id}/like")  // ⚠️ Change de PUT à POST si nécessaire
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Path("id") postId: String
    ): Response<Post>

    @DELETE("posts/{id}/like")  // ⚠️ Utilise DELETE pour unlike
    suspend fun unlikePost(
        @Header("Authorization") token: String,
        @Path("id") postId: String
    ): Response<Post>

    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Body commentRequest: CommentRequest
    ): Response<Commentaire>  // Renvoie un objet Commentaire


    @GET("users/liste")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): Response<List<User>>



    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse>


    // Notifications
    // ⚠️ Correction: retrait préfixe api/
    @GET("notifications/user/{userId}")
    suspend fun getNotificationsByUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<List<Notification>>

    @GET("notifications/user/{userId}/non-lues")
    suspend fun getNotificationsNonLues(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<List<Notification>>

    @PUT("notifications/{id}/marquer-lue")
    suspend fun marquerNotificationLue(
        @Header("Authorization") token: String,
        @Path("id") notificationId: String
    ): Response<Notification>

    @PUT("notifications/user/{userId}/marquer-toutes-lues")
    suspend fun marquerToutesNotificationsLues(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<ApiResponse>

    @GET("notifications/user/{userId}/count-non-lues")
    suspend fun compterNotificationsNonLues(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<Long>

    @DELETE("notifications/{id}")
    suspend fun supprimerNotification(
        @Header("Authorization") token: String,
        @Path("id") notificationId: String
    ): Response<ApiResponse>
    
    @DELETE("notifications/user/{userId}/toutes")
    suspend fun supprimerToutesNotifications(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<ApiResponse>

    // 🔹 Créer une donation
    @POST("api/donations") // ou @POST("donations") selon votre configuration
    @Headers("Content-Type: application/json")
    suspend fun createDonation(
        @Header("Authorization") token: String,
        @Body donationBody: RequestBody
    ): Response<ResponseBody>
    // 🔹 Récupérer toutes les donations de l'utilisateur
    @GET("donations/user/{userId}")
    suspend fun getUserDonations(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<List<DonationDTO>>

    // 🔹 Récupérer une donation par ID
    @GET("donations/{id}")
    suspend fun getDonationById(
        @Header("Authorization") token: String,
        @Path("id") donationId: String
    ): Response<DonationDTO>

    // 🔹 Mettre à jour le statut d'une donation (Admin)
    @PUT("donations/{id}/status")
    suspend fun updateDonationStatus(
        @Header("Authorization") token: String,
        @Path("id") donationId: String,
        @Query("action") action: String // "accepter" ou "refuser"
    ): Response<DonationDTO>

    // 🔹 Supprimer une donation
    @DELETE("donations/{id}")
    suspend fun deleteDonation(
        @Header("Authorization") token: String,
        @Path("id") donationId: String
    ): Response<Void>
    // Pour récupérer les commentaires
    @GET("posts/{postId}/comments")
    suspend fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: String
    ): Response<List<Commentaire>>

    // Pour supprimer un commentaire
    @DELETE("posts/comments/{commentId}")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: String
    ): Response<Map<String, Any>>
    // Likes


    // Chatbot endpoint
    @POST("http://10.0.2.2:5000/chat") // Assuming chatbot server runs on localhost:5000
    suspend fun sendMessageToChatbot(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
