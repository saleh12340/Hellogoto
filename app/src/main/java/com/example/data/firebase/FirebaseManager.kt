package com.example.data.firebase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    suspend fun signInWithGoogle(context: Context) {
        val credentialManager = CredentialManager.create(context)
        
        // Note: WEB_CLIENT_ID should be in your secrets or build config if you have one.
        // For now, we assume the user has configured google-services.json correctly.
        // If not, this will need a valid client ID.
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("YOUR_WEB_CLIENT_ID") // User needs to update this
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            val googleIdToken = result.credential.data.getString("androidx.credentials.BUNDLE_KEY_ID_TOKEN")
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            auth.signInWithCredential(credential).await()
            _currentUser.value = auth.currentUser
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    suspend fun saveChatHistory(chatId: String, history: List<Map<String, String>>) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("chats").document(chatId)
            .set(mapOf("history" to history))
            .await()
    }

    suspend fun getChatHistory(chatId: String): List<Map<String, String>> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val doc = db.collection("users").document(userId)
            .collection("chats").document(chatId)
            .get()
            .await()
        return doc.get("history") as? List<Map<String, String>> ?: emptyList()
    }
}
