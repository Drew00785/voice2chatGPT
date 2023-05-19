package com.example.voice2chatgpt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.voice2chatgpt.databinding.MessageItemBinding
import android.util.Log
class MessageAdapter(private val messages: MutableList<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MessageItemBinding.inflate(inflater, parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.binding.messageText.text = message.text
        val params = holder.binding.messageText.layoutParams as ConstraintLayout.LayoutParams
        if (message.isUser) {
            params.startToStart = ConstraintLayout.LayoutParams.UNSET
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            holder.binding.messageText.setBackgroundResource(R.drawable.chat_bubble_user)
            Log.d("MessageAdapter", "User message: ${message.text}")
        } else {
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            holder.binding.messageText.setBackgroundResource(R.drawable.chat_bubble_gpt)
            Log.d("MessageAdapter", "Assistant message: ${message.text}")
        }
        holder.binding.messageText.layoutParams = params
    }


    override fun getItemCount(): Int {
        return messages.size
    }
}
