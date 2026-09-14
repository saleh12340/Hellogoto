package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.RetrofitClient
import com.example.data.local.GroceryDatabase
import com.example.data.local.Invoice
import com.example.data.local.InvoiceItem
import com.example.data.model.*
import com.example.data.repository.GroceryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class OmniViewModel(application: Application) : AndroidViewModel(application) {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    private val repository: GroceryRepository
    val allInvoices: Flow<List<Invoice>>

    init {
        val invoiceDao = GroceryDatabase.getDatabase(application).invoiceDao()
        repository = GroceryRepository(invoiceDao)
        allInvoices = repository.allInvoices
    }

    fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>) {
        viewModelScope.launch {
            repository.insertInvoiceWithItems(invoice, items)
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    // Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Image State
    private val _generatedImageUrl = MutableStateFlow<String?>(null)
    val generatedImageUrl = _generatedImageUrl.asStateFlow()

    // Music State
    private val _generatedMusicUrl = MutableStateFlow<String?>(null)
    val generatedMusicUrl = _generatedMusicUrl.asStateFlow()

    fun sendMessage(text: String, modelType: String = "flash", thinking: Boolean = false, search: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            val userMessage = ChatMessage(text, "user")
            _chatMessages.value = _chatMessages.value + userMessage

            val contents = _chatMessages.value.map { msg ->
                Content(parts = listOf(Part(text = msg.text)), role = msg.role)
            }

            val tools = if (search) {
                listOf(buildJsonObject {
                    putJsonArray("tools") {
                        addJsonObject {
                            put("google_search", buildJsonObject {})
                        }
                    }
                })
            } else null

            val config = if (thinking) {
                GenerationConfig(thinkingConfig = ThinkingConfig(thinkingLevel = "high"))
            } else null

            val request = GenerateContentRequest(
                contents = contents,
                generationConfig = config,
                tools = tools,
                systemInstruction = Content(parts = listOf(Part(text = "You are OmniAI, a helpful and versatile assistant. You can chat, search the web, generate images, and compose music. Provide concise and accurate responses.")))
            )

            try {
                val response = when (modelType) {
                    "pro" -> RetrofitClient.geminiApi.generateWithPro(apiKey, request)
                    "lite" -> RetrofitClient.geminiApi.generateWithFlashLite(apiKey, request)
                    else -> RetrofitClient.geminiApi.generateWithFlash(apiKey, request)
                }

                val aiText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response"
                _chatMessages.value = _chatMessages.value + ChatMessage(aiText, "model")
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage("Error: ${e.message}", "model")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateImage(prompt: String, size: String = "1K", highQuality: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = size),
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )

            try {
                val response = if (highQuality) {
                    RetrofitClient.geminiApi.generateWithProImage(apiKey, request)
                } else {
                    RetrofitClient.geminiApi.generateWithFlashImage(apiKey, request)
                }
                
                // Note: Image generation via REST returns data in a specific way.
                // For this prototype, we'll assume it returns a part with inlineData or a URL.
                // In many cases, it returns the base64 in inlineData.
                val part = response.candidates.firstOrNull()?.content?.parts?.find { it.inlineData != null }
                if (part != null) {
                    // Convert base64 to a data URL for simple loading in Coil
                    _generatedImageUrl.value = "data:${part.inlineData?.mimeType};base64,${part.inlineData?.data}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateMusic(prompt: String, isPro: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(responseModalities = listOf("AUDIO"))
            )

            try {
                val response = if (isPro) {
                    RetrofitClient.geminiApi.generateMusicPro(apiKey, request)
                } else {
                    RetrofitClient.geminiApi.generateMusicClip(apiKey, request)
                }

                val part = response.candidates.firstOrNull()?.content?.parts?.find { it.inlineData != null }
                if (part != null) {
                    _generatedMusicUrl.value = "data:${part.inlineData?.mimeType};base64,${part.inlineData?.data}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class ChatMessage(val text: String, val role: String)
