package com.example.startdrive.data.model

data class Presence(
    val status: String = "offline",
    val lastSeen: Long = 0L,
)
