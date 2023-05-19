package com.example.voice2chatgpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import com.example.voice2chatgpt.databinding.ActivityMainBinding
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var binding: ActivityMainBinding
    private val messages = mutableListOf<Message>()
    private val messageAdapter = MessageAdapter(messages)
    private val VOICE_RECOGNITION_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textToSpeech = TextToSpeech(this, this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        binding.fab.setOnClickListener {
            startVoiceRecognition()
        }
    }
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt))
        }
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                val userMessage = results[0]
                messages.add(Message(userMessage, true))
                messageAdapter.notifyDataSetChanged()
                messageAdapter.notifyItemInserted(messages.size - 1)
                binding.recyclerView.scrollToPosition(messages.size - 1)
                sendMessageToChatGPT(userMessage)
            }
        }
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "This language is not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show()
            Log.e("TTS Error", "Initialization failed")
        }
    }

    private fun sendMessageToChatGPT(message: String) {
        val requestBody = ChatGPTRequest(
            model = "gpt-3.5-turbo",
            // model = "gpt-4",
            messages = messages.map { ChatMessage(role = if (it.isUser) "user" else "assistant", content = it.text) }
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.chatGPTApi.getCompletion(requestBody)
                if (response.isSuccessful && response.body() != null) {
                    val chatGPTResponse = response.body()?.choices?.getOrNull(0)?.message?.content?.trim() ?: ""
                    Log.e("API Response", response.body().toString())

                    withContext(Dispatchers.Main) {
                        messages.add(Message(chatGPTResponse, false))
                        messageAdapter.notifyDataSetChanged()
                        binding.recyclerView.scrollToPosition(messages.size - 1)

                        textToSpeech.speak(chatGPTResponse, TextToSpeech.QUEUE_FLUSH, Bundle(), "")

                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}
