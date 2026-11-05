package com.campuspulse.campuspulse

import androidx.room.Entity
import androidx.room.PrimaryKey

// API Auth Models
data class AuthRequest(
    val email: String,
    val password: String,
    val province: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val province: String? = null
)

data class AuthResponse(
    val userId: String? = null,
    val user_id: String? = null,
    val token: String? = null,
    val message: String? = null
)

// API RSVP & Sync Models
data class RsvpRequest(
    val event_id: String,
    val user_id: String
)

data class SyncResponse(
    val success: Boolean,
    val ticket_id: String? = null
)

// Event Model
@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val venue: String? = null,
    val venue_name: String? = null,
    val date: String? = null,
    val event_date: String? = null,
    val imageUrl: String? = null,
    val isCached: Boolean = false
) {
    fun getDisplayVenue(): String = venue_name ?: venue ?: ""
    fun getDisplayDate(): String = event_date ?: date ?: ""
}

// Ticket Model
@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey val id: String,
    val ticketId: String = "",
    val ticket_id: String? = null,
    val eventId: String = "",
    val event_id: String? = null,
    val userId: String = "",
    val user_id: String? = null,
    val qr_code_data: String? = null,
    val sync_status: String? = null,
    val imageUrl: String? = null,
    val isSynced: Boolean = true
) {
    fun getDisplayTicketId(): String = ticket_id ?: ticketId
    fun getDisplayEventId(): String = event_id ?: eventId
    fun getDisplaySyncStatus(): String = sync_status ?: if (isSynced) "Synced" else "Pending"
}