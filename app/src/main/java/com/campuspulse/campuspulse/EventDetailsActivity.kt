package com.campuspulse.campuspulse

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import coil.load
import com.campuspulse.campuspulse.databinding.ActivityEventDetailsBinding
import kotlinx.coroutines.launch
import java.util.UUID

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailsBinding

    override fun attachBaseContext(newBase: Context) {
        val contextWithLocale = LocaleHelper.onAttach(newBase)
        super.attachBaseContext(contextWithLocale)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferenceManager = PreferenceManager(this)
        if (preferenceManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding = ActivityEventDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventId = intent.getStringExtra("EVENT_ID") ?: ""
        val title = intent.getStringExtra("EVENT_TITLE") ?: getString(R.string.title_events)
        val desc = intent.getStringExtra("EVENT_DESC") ?: "Join us for this exclusive campus event."
        val date = intent.getStringExtra("EVENT_DATE") ?: "TBD"
        val location = intent.getStringExtra("EVENT_LOCATION") ?: "Main Campus"
        val imageUrl = intent.getStringExtra("EVENT_IMAGE_URL")

        binding.tvDetailTitle.text = title
        binding.tvDetailDescription.text = desc
        binding.tvDetailDate.text = date
        binding.tvDetailVenue.text = location
        binding.tvDetailCategory.text = getString(R.string.campus_life)

        // Load image banner with Coil
        binding.ivDetailEventImage.load(imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
            error(android.R.drawable.ic_menu_gallery)
        }

        val userId = preferenceManager.getUserId() ?: "usr_default"

        binding.btnDetailRsvp.setOnClickListener {
            binding.btnDetailRsvp.isEnabled = false
            performRsvp(eventId, userId, title, imageUrl)
        }
    }

    private fun performRsvp(eventId: String, userId: String, eventTitle: String, imageUrl: String?) {
        val db = CampusPulseDatabase.getDatabase(this)
        val isOnline = NetworkMonitor.isConnected(this)

        lifecycleScope.launch {
            val timestamp = System.currentTimeMillis()
            val newTicket = Ticket(
                id = UUID.randomUUID().toString(),
                ticketId = "TKT-${(timestamp % 100000)}",
                ticket_id = "TKT-${(timestamp % 100000)}",
                eventId = eventId,
                event_id = eventId,
                userId = userId,
                user_id = userId,
                qr_code_data = "QR-$eventId-$userId",
                sync_status = if (isOnline) "Synced" else "Pending",
                imageUrl = imageUrl,
                isSynced = isOnline
            )

            try {
                if (isOnline) {
                    val response = ApiClient.apiService.rsvpEvent(RsvpRequest(eventId, userId))
                    val syncedTicket = if (response.isSuccessful && response.body()?.ticket_id != null) {
                        newTicket.copy(
                            ticketId = response.body()!!.ticket_id!!,
                            ticket_id = response.body()!!.ticket_id!!,
                            sync_status = "Synced",
                            isSynced = true
                        )
                    } else newTicket
                    db.ticketDao().insertTicket(syncedTicket)
                    Toast.makeText(this@EventDetailsActivity, getString(R.string.rsvp_success), Toast.LENGTH_SHORT).show()
                } else {
                    val offlineTicket = newTicket.copy(sync_status = "Pending", isSynced = false)
                    db.ticketDao().insertTicket(offlineTicket)
                    Toast.makeText(this@EventDetailsActivity, getString(R.string.rsvp_offline), Toast.LENGTH_LONG).show()
                    OfflineSyncWorker.enqueue(this@EventDetailsActivity)
                }

                NotificationHelper.showNotification(
                    this@EventDetailsActivity,
                    getString(R.string.push_notif_title),
                    "RSVP confirmed for: $eventTitle",
                    eventId = eventId
                )

                finish()
            } catch (e: Exception) {
                val offlineTicket = newTicket.copy(sync_status = "Pending", isSynced = false)
                db.ticketDao().insertTicket(offlineTicket)
                Toast.makeText(this@EventDetailsActivity, getString(R.string.rsvp_offline), Toast.LENGTH_LONG).show()
                OfflineSyncWorker.enqueue(this@EventDetailsActivity)

                NotificationHelper.showNotification(
                    this@EventDetailsActivity,
                    getString(R.string.push_notif_title),
                    "RSVP saved offline for: $eventTitle",
                    eventId = eventId
                )

                finish()
            }
        }
    }
}