package com.example.charity_projet.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences("charity_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"           // ⚠️ AJOUT: Constante pour l'ID
        const val USER_USERNAME = "user_username"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val USER_ROLE = "user_role"
        const val TOKEN_EXPIRY = "token_expiry"
    }

    fun saveUserId(userId: String) {
        Log.d("SessionManager", "📱 SAVING User ID: '$userId' (type: ${userId.javaClass.simpleName})")
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)
        editor.apply()
    }

    fun getUserId(): String? {
        val userId = prefs.getString(USER_ID, null)
        Log.d("SessionManager", "📱 RETRIEVING User ID: '$userId'")
        return userId
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    // ✅ CORRIGÉ: La méthode attend 5 paramètres
    fun saveUserInfo(userId: String, username: String, name: String, email: String, role: String) {
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)      // ⚠️ Utilisez USER_ID
        editor.putString(USER_USERNAME, username)
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_ROLE, role)
        editor.apply()
    }

    // Méthode alternative sans userId (pour compatibilité)
    fun saveUserInfo(username: String, name: String, email: String, role: String) {
        val editor = prefs.edit()
        editor.putString(USER_USERNAME, username)
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_ROLE, role)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun getUsername(): String? {
        return prefs.getString(USER_USERNAME, null)
    }

    fun getUserName(): String? {
        return prefs.getString(USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(USER_ROLE, null)
    }

    fun isLoggedIn(): Boolean {
        return !fetchAuthToken().isNullOrEmpty()
    }

    // ✅ CORRIGÉ: Ajoutez USER_ID dans clearAuth
    fun clearAuth() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.remove(USER_ID)           // ⚠️ AJOUT
        editor.remove(USER_USERNAME)
        editor.remove(USER_NAME)
        editor.remove(USER_EMAIL)
        editor.remove(USER_ROLE)
        editor.apply()
    }

    fun clearAuthToken() {
        clearAuth()
    }
}