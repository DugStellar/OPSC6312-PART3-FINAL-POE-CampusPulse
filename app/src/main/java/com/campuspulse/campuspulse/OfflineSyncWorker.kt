package com.campuspulse.campuspulse

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class OfflineSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = CampusPulseDatabase.getDatabase(applicationContext)
        val unsyncedTickets = db.ticketDao().getUnsyncedTickets()

        var syncedCount = 0
        for (ticket in unsyncedTickets) {
            val eventId = ticket.getDisplayEventId()
            val userId = if (!ticket.user_id.isNullOrEmpty()) ticket.user_id else ticket.userId

            try {
                val response = ApiClient.apiService.rsvpEvent(RsvpRequest(eventId, userId))
                if (response.isSuccessful) {
                    val updatedTicket = ticket.copy(
                        sync_status = "Synced",
                        isSynced = true
                    )
                    db.ticketDao().insertTicket(updatedTicket)
                    syncedCount++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (syncedCount > 0) {
            NotificationHelper.showNotification(
                applicationContext,
                applicationContext.getString(R.string.push_notif_title),
                applicationContext.getString(R.string.sync_completed)
            )
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }
    }
}