package com.example.spend_trend.ui.copilot

/**
 * Data model representing a single message in the Copilot chat.
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val time: String
)
