package com.example.startdrive.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class InstructorOpenWindow(
    @DocumentId val id: String = "",
    @get:PropertyName("instructorId") @set:PropertyName("instructorId") var instructorId: String = "",
    @get:PropertyName("cadetId") @set:PropertyName("cadetId") var cadetId: String? = null,
    @get:PropertyName("dateTime") @set:PropertyName("dateTime") var dateTime: Timestamp? = null,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "", // free, booked
)
