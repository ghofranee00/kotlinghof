package com.example.charity_projet.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // =======================================================================
    // ⚠️ CHOISISSEZ VOTRE CONFIGURATION ICI (Décommentez une seule ligne)
    // =======================================================================

    // OPTION 1 : Pour l'Émulateur Android Studio (Par défaut)
    private const val BASE_URL = "http://10.0.2.2:8089/api/"

    // OPTION 2 : Pour un Téléphone Physique (Connecté au même Wi-Fi que le PC)
    // Remplacez '192.168.1.14' par l'adresse IPv4 de votre PC (tapez 'ipconfig' dans le terminal)
    // private const val BASE_URL = "http://192.168.1.14:8089/api/"

    // OPTION 3 : Pour l'Émulateur Genymotion
    // private const val BASE_URL = "http://10.0.3.2:8089/api/"

    // =======================================================================

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: Api by lazy {
        retrofit.create(Api::class.java)
    }
}
