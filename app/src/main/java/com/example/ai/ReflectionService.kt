package com.example.ai

interface ReflectionService {
    suspend fun generateReflection(
        content: String,
        mood: String,
        intensity: Int,
        tags: List<String>,
        writingMode: String = "Journal libre",
        analysisType: String? = null
    ): String
}
