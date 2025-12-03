package com.example.charity_projet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charity_projet.R
import com.example.charity_projet.api.RetrofitClient
import com.example.charity_projet.models.ChatMessage
import com.example.charity_projet.models.ChatRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatbotActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        recyclerView = findViewById(R.id.recyclerViewChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        chatAdapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        // Message de bienvenue
        addMessage("Bonjour ! Je suis SoliBot, votre assistant solidarité. Comment puis-je vous aider aujourd'hui ?", false)

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                etMessage.text.clear()
            }
        }
    }

    private fun sendMessage(message: String) {
        addMessage(message, true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.sendMessageToChatbot(ChatRequest(message))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val reply = response.body()!!.reply
                        addMessage(reply, false)
                    } else {
                        addMessage("Désolé, je ne peux pas répondre pour le moment.", false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addMessage("Erreur de connexion au serveur chatbot.", false)
                }
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    inner class ChatAdapter(private val messages: List<ChatMessage>) :
        RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

        inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvBotMessage: TextView = itemView.findViewById(R.id.tvBotMessage)
            val tvUserMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_message, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val message = messages[position]
            if (message.isUser) {
                holder.tvUserMessage.text = message.message
                holder.tvUserMessage.visibility = View.VISIBLE
                holder.tvBotMessage.visibility = View.GONE
            } else {
                holder.tvBotMessage.text = message.message
                holder.tvBotMessage.visibility = View.VISIBLE
                holder.tvUserMessage.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = messages.size
    }
}
