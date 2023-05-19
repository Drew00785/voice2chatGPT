package com.example.voice2chatgpt

data class ChatGPTResponse(
    val id: String,
    val responseObject: String?,
    val created: Int,
    val model: String,
    val usage: Usage,
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage,
    val index: Int,
    val logprobs: String?,
    val finish_reason: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

