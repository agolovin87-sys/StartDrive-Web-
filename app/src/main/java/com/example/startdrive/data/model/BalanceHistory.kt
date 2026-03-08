package com.example.startdrive.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class BalanceHistory(
    @DocumentId val id: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: Int = 0,
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "", // credit, debit, set
    @get:PropertyName("performedBy") @set:PropertyName("performedBy") var performedBy: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Timestamp? = null,
)
