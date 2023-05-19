package com.example.voice2chatgpt

data class ChatGPTRequest(
    val model: String,
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val role: String,
    val content: String
)
