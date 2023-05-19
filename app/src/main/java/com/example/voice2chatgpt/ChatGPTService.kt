package com.example.voice2chatgpt

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatGPTService {

    @Headers("Content-Type: application/json")
    @POST("https://api.openai.com/v1/chat/completions")
    suspend fun getCompletion(@Body requestBody: ChatGPTRequest): Response<ChatGPTResponse>
}

