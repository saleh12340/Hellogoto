package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.RetrofitClient
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.GroceryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class OmniViewModel(application: Application) : AndroidViewModel(application) {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val repository = GroceryRepository(GroceryDatabase.getDatabase(application).invoiceDao())
    val allInvoices = repository.allInvoices
    val customers = repository.customers
    val products = repository.products
    val expenses = repository.expenses
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _generatedImageUrl = MutableStateFlow<String?>(null)
    val generatedImageUrl = _generatedImageUrl.asStateFlow()
    private val _generatedMusicUrl = MutableStateFlow<String?>(null)
    val generatedMusicUrl = _generatedMusicUrl.asStateFlow()

    fun saveInvoice(invoice: Invoice, items: List<InvoiceItem>) = viewModelScope.launch { repository.saveInvoice(invoice, items) }
    fun deleteInvoice(invoice: Invoice) = viewModelScope.launch { repository.deleteInvoice(invoice) }
    fun invoiceItems(invoiceId: Long) = repository.getItems(invoiceId)
    fun saveCustomer(customer: Customer) = viewModelScope.launch { repository.saveCustomer(customer) }
    fun deleteCustomer(customer: Customer) = viewModelScope.launch { repository.deleteCustomer(customer) }
    fun saveProduct(product: Product) = viewModelScope.launch { repository.saveProduct(product) }
    fun deleteProduct(product: Product) = viewModelScope.launch { repository.deleteProduct(product) }
    fun saveTransaction(tx: CustomerTransaction) = viewModelScope.launch { repository.saveTransaction(tx) }
    fun deleteTransaction(tx: CustomerTransaction) = viewModelScope.launch { repository.deleteTransaction(tx) }
    fun transactions(customerId: Long) = repository.getTransactions(customerId)
    fun saveExpense(expense: Expense) = viewModelScope.launch { repository.saveExpense(expense) }

    fun sendMessage(text: String, modelType: String = "flash", thinking: Boolean = false, search: Boolean = false) = viewModelScope.launch {
        _isLoading.value = true
        _chatMessages.value = _chatMessages.value + ChatMessage(text, "user")
        val contents = _chatMessages.value.map { Content(parts = listOf(Part(text = it.text)), role = it.role) }
        val tools = if (search) listOf(buildJsonObject { putJsonArray("tools") { addJsonObject { put("google_search", buildJsonObject {}) } } }) else null
        val config = if (thinking) GenerationConfig(thinkingConfig = ThinkingConfig(thinkingLevel = "high")) else null
        val request = GenerateContentRequest(contents = contents, generationConfig = config, tools = tools, systemInstruction = Content(parts = listOf(Part(text = "You are OmniAI, a helpful Arabic-first assistant."))))
        try {
            val response = when (modelType) { "pro" -> RetrofitClient.geminiApi.generateWithPro(apiKey, request); "lite" -> RetrofitClient.geminiApi.generateWithFlashLite(apiKey, request); else -> RetrofitClient.geminiApi.generateWithFlash(apiKey, request) }
            val answer = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "لا توجد استجابة"
            _chatMessages.value = _chatMessages.value + ChatMessage(answer, "model")
        } catch (e: Exception) { _chatMessages.value = _chatMessages.value + ChatMessage("تعذر الاتصال بالخدمة: ${e.message ?: "خطأ غير معروف"}", "model") }
        finally { _isLoading.value = false }
    }

    fun generateImage(prompt: String, size: String = "1K", highQuality: Boolean = true) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val request = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))), generationConfig = GenerationConfig(imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = size), responseModalities = listOf("TEXT", "IMAGE")))
            val response = if (highQuality) RetrofitClient.geminiApi.generateWithProImage(apiKey, request) else RetrofitClient.geminiApi.generateWithFlashImage(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.find { it.inlineData != null }?.let { _generatedImageUrl.value = "data:${it.inlineData?.mimeType};base64,${it.inlineData?.data}" }
        } catch (_: Exception) {} finally { _isLoading.value = false }
    }

    fun generateMusic(prompt: String, isPro: Boolean = false) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val request = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))), generationConfig = GenerationConfig(responseModalities = listOf("AUDIO")))
            val response = if (isPro) RetrofitClient.geminiApi.generateMusicPro(apiKey, request) else RetrofitClient.geminiApi.generateMusicClip(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.find { it.inlineData != null }?.let { _generatedMusicUrl.value = "data:${it.inlineData?.mimeType};base64,${it.inlineData?.data}" }
        } catch (_: Exception) {} finally { _isLoading.value = false }
    }
}

data class ChatMessage(val text: String, val role: String)
