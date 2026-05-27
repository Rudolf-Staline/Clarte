package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.JournalEntry
import com.example.data.JournalRepository
import com.example.data.SettingsStore
import com.example.ai.GeminiReflectionService
import com.example.ai.MockReflectionService
import com.example.ai.ReflectionService
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(
    application: Application,
    private val repository: JournalRepository,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    private val geminiService = GeminiReflectionService()
    private val mockService = MockReflectionService()

    // Database entries observed as a reactive Flow
    val allEntries: StateFlow<List<JournalEntry>> = repository.allEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI state for settings
    val currentTheme: StateFlow<String> = settingsStore.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "système"
        )

    val isForceMockEnabled: StateFlow<Boolean> = settingsStore.forceMockAiFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val hidePreviewsHistory: StateFlow<Boolean> = settingsStore.hidePreviewsHistoryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val hideLatestPreviewHome: StateFlow<Boolean> = settingsStore.hideLatestPreviewHomeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val encryptedBackupEnabled: StateFlow<Boolean> = settingsStore.encryptedBackupEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val hasCompletedOnboarding: StateFlow<Boolean> = settingsStore.hasCompletedOnboardingFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isAppLockEnabled: StateFlow<Boolean> = settingsStore.isAppLockEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBiometricEnabled: StateFlow<Boolean> = settingsStore.isBiometricEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lockOnBackground: StateFlow<Boolean> = settingsStore.lockOnBackgroundFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoLockDelayMinutes: StateFlow<Int> = settingsStore.autoLockDelayMinutesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hideExportWarning: StateFlow<Boolean> = settingsStore.hideExportWarningFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val pinHash = settingsStore.pinHashFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val pinSalt = settingsStore.pinSaltFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Volatile Lock State
    // Default to true so it's locked on app start (if lock is actually enabled globally)
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastUnlockedAt: Long = 0L

    fun unlockApp() {
        _isLocked.value = false
        lastUnlockedAt = System.currentTimeMillis()
    }

    fun lockApp() {
        _isLocked.value = true
    }

    fun verifyPin(pin: String): Boolean {
        val hash = pinHash.value ?: return false
        val saltBase64 = pinSalt.value ?: return false
        val salt = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
        val computedHash = com.example.security.EncryptionManager.hashPin(pin, salt)
        if (computedHash == hash) {
            unlockApp()
            return true
        }
        return false
    }

    fun setupPin(pin: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val salt = com.example.security.EncryptionManager.generateSalt()
            val hash = com.example.security.EncryptionManager.hashPin(pin, salt)
            settingsStore.setPin(hash, android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            settingsStore.setAppLockEnabled(true)
            _isLocked.value = false
            lastUnlockedAt = System.currentTimeMillis()
        }
    }

    fun disableAppLock() {
        viewModelScope.launch {
            settingsStore.setAppLockEnabled(false)
            settingsStore.setPin(null, null)
            settingsStore.setBiometricEnabled(false)
            _isLocked.value = false
        }
    }

    fun checkAndLockIfNeeded(isBackground: Boolean) {
        if (!isAppLockEnabled.value) {
            _isLocked.value = false
            return
        }
        if (isBackground && lockOnBackground.value) {
            _isLocked.value = true
        } else if (!isBackground) {
            val delayMillis = autoLockDelayMinutes.value * 60 * 1000L
            if (delayMillis > 0 && System.currentTimeMillis() - lastUnlockedAt > delayMillis) {
                _isLocked.value = true
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setBiometricEnabled(enabled) }
    }

    fun setLockOnBackground(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setLockOnBackground(enabled) }
    }

    fun setAutoLockDelayMinutes(minutes: Int) {
        viewModelScope.launch { settingsStore.setAutoLockDelayMinutes(minutes) }
    }

    fun setHideExportWarning(hide: Boolean) {
        viewModelScope.launch { settingsStore.setHideExportWarning(hide) }
    }

    // Loading states for AI generating flow
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Temporary storage for latest generated reflection before saving
    private val _lastGeneratedReflection = MutableStateFlow<String?>(null)
    val lastGeneratedReflection: StateFlow<String?> = _lastGeneratedReflection.asStateFlow()

    fun setHidePreviewsHistory(hide: Boolean) {
        viewModelScope.launch {
            settingsStore.setHidePreviewsHistory(hide)
        }
    }

    fun setHideLatestPreviewHome(hide: Boolean) {
        viewModelScope.launch {
            settingsStore.setHideLatestPreviewHome(hide)
        }
    }

    fun setHasCompletedOnboarding(completed: Boolean) {
        viewModelScope.launch {
            settingsStore.setHasCompletedOnboarding(completed)
        }
    }

    fun updateTheme(themeName: String) {
        viewModelScope.launch {
            settingsStore.setTheme(themeName)
        }
    }

    fun setEncryptedBackupEnabled(enabled: Boolean, passphrase: String? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (enabled && !passphrase.isNullOrEmpty()) {
                val saltBytes = com.example.security.EncryptionManager.generateSalt()
                val derivedKey = com.example.security.EncryptionManager.deriveKey(passphrase, saltBytes)
                val (wrappedKey, iv) = com.example.security.EncryptionManager.wrapKey(derivedKey)
                
                settingsStore.setWrappedEncryptionKey(wrappedKey, iv)
                settingsStore.setEncryptedBackupEnabled(true)
                
                // Save salt to Firestore metadata
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    val data = mapOf(
                        "encryptionEnabled" to true,
                        "encryptionVersion" to 1,
                        "kdf" to "PBKDF2WithHmacSHA256",
                        "salt" to android.util.Base64.encodeToString(saltBytes, android.util.Base64.NO_WRAP),
                        "iterations" to 100000,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
                        .collection("metadata").document("encryption")
                        .set(data)
                }
            } else if (!enabled) {
                settingsStore.clearEncryptionData()
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
                        .collection("metadata").document("encryption")
                        .set(mapOf("encryptionEnabled" to false, "updatedAt" to System.currentTimeMillis()))
                }
            }
        }
    }

    fun disableEncryptedBackup() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            settingsStore.clearEncryptionData()
        }
    }

    fun toggleForceMock(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setForceMockAi(enabled)
        }
    }

    // Insert a new journal entry and return its generated ID through long callback
    fun insertEntry(
        content: String,
        mood: String,
        intensity: Int,
        tags: List<String>,
        writingMode: String = "Journal libre",
        analysisType: String? = null,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val entry = JournalEntry(
                content = content,
                mood = mood,
                intensity = intensity,
                tags = tags,
                writingMode = writingMode,
                analysisType = analysisType
            )
            val newId = repository.insertEntry(entry)
            onComplete(newId)
        }
    }

    // Core reflection pipeline
    fun generateReflectionForEntry(entryId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isGenerating.value = true
            _lastGeneratedReflection.value = null
            
            // Fetch entry content
            val entry = repository.getEntryById(entryId).first()
            if (entry != null) {
                // Determine service based on setting
                val useMock = settingsStore.forceMockAiFlow.first()
                val activeService: ReflectionService = if (useMock) mockService else geminiService
                
                // Call reflection pipeline
                val reflection = activeService.generateReflection(
                    content = entry.content,
                    mood = entry.mood,
                    intensity = entry.intensity,
                    tags = entry.tags,
                    writingMode = entry.writingMode,
                    analysisType = entry.analysisType
                )
                
                _lastGeneratedReflection.value = reflection
            }
            _isGenerating.value = false
            onComplete()
        }
    }

    // Save generated reflection to DB
    fun saveReflection(entryId: Long, reflection: String) {
        viewModelScope.launch {
            val entry = repository.getEntryById(entryId).first()
            if (entry != null) {
                val updated = entry.copy(
                    aiReflection = reflection,
                    hasReflection = true
                )
                repository.updateEntry(updated)
            }
        }
    }

    fun regenerateReflectionForEntry(entryId: Long, newAnalysisType: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isGenerating.value = true
            _lastGeneratedReflection.value = null
            
            val entry = repository.getEntryById(entryId).first()
            if (entry != null) {
                val updatedWithAnalysis = entry.copy(analysisType = newAnalysisType)
                repository.updateEntry(updatedWithAnalysis)

                val useMock = settingsStore.forceMockAiFlow.first()
                val activeService: ReflectionService = if (useMock) mockService else geminiService

                val reflection = activeService.generateReflection(
                    content = entry.content,
                    mood = entry.mood,
                    intensity = entry.intensity,
                    tags = entry.tags,
                    writingMode = entry.writingMode,
                    analysisType = newAnalysisType
                )

                val finalUpdated = updatedWithAnalysis.copy(
                    aiReflection = reflection,
                    hasReflection = true,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateEntry(finalUpdated)
            }
            _isGenerating.value = false
            onComplete()
        }
    }

    fun toggleFavorite(entryId: Long) {
        viewModelScope.launch {
            val entry = repository.getEntryById(entryId).first()
            if (entry != null) {
                repository.updateEntry(entry.copy(isFavorite = !entry.isFavorite))
            }
        }
    }

    fun togglePinned(entryId: Long) {
        viewModelScope.launch {
            val entry = repository.getEntryById(entryId).first()
            if (entry != null) {
                repository.updateEntry(entry.copy(isPinned = !entry.isPinned))
            }
        }
    }

    fun generateMonthlyReview(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isGenerating.value = true
            val entriesSnapshot = allEntries.value
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
            val thisMonthEntries = entriesSnapshot.filter { it.createdAt >= thirtyDaysAgo }

            if (thisMonthEntries.size < 8) {
                onComplete("Il faut au moins huit entrées ce mois-ci pour générer une revue pertinente.")
                _isGenerating.value = false
                return@launch
            }

            // Determine if we use Gemini or offline mock
            val useMock = settingsStore.forceMockAiFlow.first()
            val apiKey = BuildConfig.GEMINI_API_KEY
            val isGeminiAvailable = !useMock && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "GEMINI_API_KEY"

            if (isGeminiAvailable) {
                val entriesSummary = thisMonthEntries.joinToString("\n\n") { entry ->
                    "Date: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRENCH).format(java.util.Date(entry.createdAt))}\nMode: ${entry.writingMode}\nHumeur: ${entry.mood} (${entry.intensity}/10)\nTexte: ${entry.content}"
                }
                val prompt = """
                    Tu es un assistant d'introspection calme, rigoureux et mature.
                    Génère une "Revue du mois" en français à partir de ces entrées de journal récentes :
                    
                    $entriesSummary
                    
                    Le ton doit être sobre, attentionné et analytique, d'une grande bienveillance mais sans fausse motivation mercantile ou émotionnelle débordante.
                    
                    Structure obligatoirement ton analyse en 7 sections titrées exactement ainsi :
                    
                    1. Thème dominant du mois
                    2. Émotions les plus fréquentes
                    3. Situations ou pensées récurrentes
                    4. Ce qui semble avoir évolué
                    5. Point de vigilance
                    6. Ce que tu peux garder du mois
                    7. Orientation sobre pour le mois suivant
                """.trimIndent()

                val systemPrompt = "Tu es Clarté, un compagnon d'introspection digne et mature. Rédige une analyse mensuelle rigoureuse en français."

                try {
                    val requestJson = org.json.JSONObject().apply {
                        put("contents", org.json.JSONArray().apply {
                            put(org.json.JSONObject().apply {
                                put("parts", org.json.JSONArray().apply {
                                    put(org.json.JSONObject().apply {
                                        put("text", prompt)
                                    })
                                })
                            })
                        })
                        put("systemInstruction", org.json.JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(org.json.JSONObject().apply {
                                    put("text", systemPrompt)
                                })
                            })
                        })
                    }
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = requestJson.toString().toByteArray().toRequestBody(mediaType)
                    val request = okhttp3.Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                        .post(requestBody)
                        .build()

                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBodyStr = response.body?.string() ?: ""
                            val jsonResponse = org.json.JSONObject(responseBodyStr)
                            val candidates = jsonResponse.getJSONArray("candidates")
                            val textResult = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                            if (textResult.isNotBlank()) {
                                onComplete(textResult)
                                _isGenerating.value = false
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    // fall back
                }
            }

            // Mock monthly review generation
            delay(1500)
            if (thisMonthEntries.isEmpty()) {
                onComplete("Pas encore assez d’entrées pour dégager une tendance.")
                _isGenerating.value = false
                return@launch
            }
            val dominantMood = thisMonthEntries.groupBy { it.mood }.maxByOrNull { it.value.size }?.key ?: "Calme"
            val frequentTags = thisMonthEntries.flatMap { it.tags }.groupBy { it }.maxByOrNull { it.value.size }?.key ?: "introspection"

            val reviewText = """
                1. Thème dominant du mois
                Ce mois a été marqué par une recherche de repères. Tes ${thisMonthEntries.size} entrées tournent principalement autour du thème : "$frequentTags".
                
                2. Émotions les plus fréquentes
                L'ambiance émotionnelle persistante s'est fixée près de l'état : "$dominantMood". Ton intensité moyenne oscille autour de ${(thisMonthEntries.sumOf { it.intensity } / thisMonthEntries.size.toDouble()).let { String.format("%.1f", it) }}/10.
                
                3. Situations ou pensées récurrentes
                On observe un cycle régulier lié aux fluctuations de niveau de fatigue, exacerbant tes réflexions autour de $frequentTags.
                
                4. Ce qui semble avoir évolué
                Tu commences à marquer la séparation entre ce qui dépend de toi et ce qui t'échappe, avec plus de clairvoyance.
                
                5. Point de vigilance
                Prête une attention particulière à tes moments de forte intensité émotionnelle. Vérifie tes ressources physiques de base (sommeil, calme).
                
                6. Ce que tu peux garder du mois
                L'effort soutenu pour déposer tes mots ici. Ce recul offre une respiration nécessaire.
                
                7. Orientation sobre pour le mois suivant
                Ne t'impose pas d'objectifs émotionnels rigides. Observe la météo intérieure telle qu'elle se présente.
            """.trimIndent()

            onComplete(reviewText)
            _isGenerating.value = false
        }
    }

    fun generateDaySummary(dayEntries: List<JournalEntry>, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            if (dayEntries.isEmpty()) {
                onComplete("Aucune entrée à résumer.")
                return@launch
            }
            
            // Determine if we use Gemini or offline mock
            val useMock = settingsStore.forceMockAiFlow.first()
            val apiKey = BuildConfig.GEMINI_API_KEY
            val isGeminiAvailable = !useMock && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "GEMINI_API_KEY"

            if (isGeminiAvailable) {
                val entriesSummary = dayEntries.joinToString("\n\n") { entry ->
                    "Mode: ${entry.writingMode}\nHumeur: ${entry.mood} (${entry.intensity}/10)\nTexte: ${entry.content}"
                }
                val prompt = """
                    Fais un résumé très sobre de cette journée à partir des entrées suivantes :
                    
                    $entriesSummary
                    
                    Structure obligatoire :
                    1. Thème du jour
                    2. Émotion dominante
                    3. Ce qui semble avoir compté
                    4. Petite phrase de synthèse
                """.trimIndent()
                
                try {
                    val requestJson = org.json.JSONObject().apply {
                        put("contents", org.json.JSONArray().apply {
                            put(org.json.JSONObject().apply {
                                put("parts", org.json.JSONArray().apply {
                                    put(org.json.JSONObject().apply {
                                        put("text", prompt)
                                    })
                                })
                            })
                        })
                    }
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = requestJson.toString().toByteArray().toRequestBody(mediaType)
                    val request = okhttp3.Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                        .post(requestBody)
                        .build()

                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBodyStr = response.body?.string() ?: ""
                            val jsonResponse = org.json.JSONObject(responseBodyStr)
                            val textResult = jsonResponse.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                            if (textResult.isNotBlank()) {
                                onComplete(textResult)
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    // fall back
                }
            }

            // Mock
            delay(1000)
            val domMood = dayEntries.groupBy { it.mood }.maxByOrNull { it.value.size }?.key ?: "Calme"
            val mockRes = """
                1. Thème du jour
                Une journée de traitement intérieur.
                
                2. Émotion dominante
                Principalement : $domMood.
                
                3. Ce qui semble avoir compté
                Le besoin de s'extraire de l'urgence et de déposer les pensées.
                
                4. Petite phrase de synthèse
                Un jour qui t'a permis de reprendre contact avec ta boussole intérieure.
            """.trimIndent()
            
            onComplete(mockRes)
        }
    }

    fun generateHindsightReflection(entry: JournalEntry, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val useMock = settingsStore.forceMockAiFlow.first()
            val apiKey = BuildConfig.GEMINI_API_KEY
            val isGeminiAvailable = !useMock && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "GEMINI_API_KEY"

            if (isGeminiAvailable) {
                val prompt = """
                    Analyse cette ancienne entrée de journal avec le recul du temps qui a passé. 
                    Sois sobre, doux, non diagnostique et non thérapeutique.
                    
                    Texte original :
                    ${entry.content}
                    
                    Humeur originale : ${entry.mood} (${entry.intensity}/10)
                    
                    Structure obligatoire :
                    1. Ce que tu ressentais à ce moment-là
                    2. Ce qui semble encore vrai aujourd’hui
                    3. Ce qui était peut-être amplifié par l’émotion
                    4. Ce que tu peux comprendre avec plus de recul
                    5. Ce que cette entrée révèle de tes besoins
                    6. Une phrase de clôture calme
                """.trimIndent()
                
                try {
                    val requestJson = org.json.JSONObject().apply {
                        put("contents", org.json.JSONArray().apply {
                            put(org.json.JSONObject().apply {
                                put("parts", org.json.JSONArray().apply {
                                    put(org.json.JSONObject().apply {
                                        put("text", prompt)
                                    })
                                })
                            })
                        })
                    }
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = requestJson.toString().toByteArray().toRequestBody(mediaType)
                    val request = okhttp3.Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                        .post(requestBody)
                        .build()

                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBodyStr = response.body?.string() ?: ""
                            val jsonResponse = org.json.JSONObject(responseBodyStr)
                            val textResult = jsonResponse.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                            if (textResult.isNotBlank()) {
                                // Add header to distinguish from normal reflection
                                val finalRes = "--- RELECTURE AVEC RECUL ---\n\n$textResult"
                                // We store it as latest reflection
                                repository.updateEntry(entry.copy(aiReflection = finalRes, hasReflection = true))
                                onComplete(finalRes)
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    // fall back
                }
            }

            // Mock
            delay(1500)
            val mockRes = """
                --- RELECTURE AVEC RECUL ---
                
                1. Ce que tu ressentais à ce moment-là
                Une forte charge cognitive liée à des attentes inassouvies.
                
                2. Ce qui semble encore vrai aujourd’hui
                Le fondement de tes valeurs et tes limites restent les mêmes.
                
                3. Ce qui était peut-être amplifié par l’émotion
                L'urgence de la situation qui te semblait insurmontable sur l'instant.
                
                4. Ce que tu peux comprendre avec plus de recul
                Cette épreuve était une adaptation temporaire et non un état définitif.
                
                5. Ce que cette entrée révèle de tes besoins
                Un besoin profond d'ancrage et de clarté pour agir sereinement.
                
                6. Une phrase de clôture calme
                Le passage du temps adoucit souvent les angles les plus aigus. Observe cette évolution.
            """.trimIndent()
            
            repository.updateEntry(entry.copy(aiReflection = mockRes, hasReflection = true))
            onComplete(mockRes)
        }
    }

    fun removeEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllEntries()
        }
    }

    fun getEntryFlow(entryId: Long) = repository.getEntryById(entryId)
}

// Custom simple factory to bind database and settings elements correctly
class JournalViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getDatabase(application)
        val repository = JournalRepository(database.journalDao())
        val settingsStore = SettingsStore(application)
        return JournalViewModel(application, repository, settingsStore) as T
    }
}
