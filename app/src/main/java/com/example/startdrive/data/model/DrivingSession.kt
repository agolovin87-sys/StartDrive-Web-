package com.example.startdrive.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class DrivingSession(
    @DocumentId val id: String = "",
    @get:PropertyName("instructorId") @set:PropertyName("instructorId") var instructorId: String = "",
    @get:PropertyName("cadetId") @set:PropertyName("cadetId") var cadetId: String = "",
    @get:PropertyName("startTime") @set:PropertyName("startTime") var startTime: Timestamp? = null,
    @get:PropertyName("endTime") @set:PropertyName("endTime") var endTime: Timestamp? = null,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "", // scheduled, inProgress, completed, cancelledByInstructor, cancelledByCadet
    @get:PropertyName("instructorRating") @set:PropertyName("instructorRating") var instructorRating: Int = 0,
    @get:PropertyName("cadetRating") @set:PropertyName("cadetRating") var cadetRating: Int = 0,
    @get:PropertyName("cancelledAt") @set:PropertyName("cancelledAt") var cancelledAt: Timestamp? = null,
    @get:PropertyName("completedAt") @set:PropertyName("completedAt") var completedAt: Timestamp? = null,
    @get:PropertyName("session") @set:PropertyName("session") var session: SessionState? = null,
    @get:PropertyName("openWindowId") @set:PropertyName("openWindowId") var openWindowId: String = "",
    @get:PropertyName("instructorConfirmed") @set:PropertyName("instructorConfirmed") var instructorConfirmed: Boolean = false,
    @get:PropertyName("startRequestedByInstructor") @set:PropertyName("startRequestedByInstructor") var startRequestedByInstructor: Boolean = false,
    @get:PropertyName("completedTimerRemaining") @set:PropertyName("completedTimerRemaining") var completedTimerRemaining: String? = null,
    @get:PropertyName("cancelReason") @set:PropertyName("cancelReason") var cancelReason: String? = null,
    @get:PropertyName("delayNotificationMinutes") @set:PropertyName("delayNotificationMinutes") var delayNotificationMinutes: Int? = null,
) {
    data class SessionState(
        @get:PropertyName("startTime") @set:PropertyName("startTime") var startTime: Long = 0L,
        @get:PropertyName("pausedTime") @set:PropertyName("pausedTime") var pausedTime: Long = 0L,
        @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = false,
        @get:PropertyName("cadetConfirmed") @set:PropertyName("cadetConfirmed") var cadetConfirmed: Boolean = false,
    )
}
