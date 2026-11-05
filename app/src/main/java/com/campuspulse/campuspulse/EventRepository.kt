package com.campuspulse.campuspulse

import android.content.Context

class EventRepository(context: Context) {

    private val db = CampusPulseDatabase.getDatabase(context)
    private val eventDao = db.eventDao()

    companion object {
        val DEFAULT_EVENTS = listOf(
            Event(
                id = "evt_101",
                title = "Annual Campus Tech Expo 2026",
                description = "Showcasing cutting-edge student software engineering projects, AI innovations, and mobile app demos with industry guest speakers.",
                venue = "Great Hall & Tech Atrium",
                venue_name = "Great Hall",
                date = "15 Oct 2026, 10:00 AM",
                event_date = "15 Oct 2026, 10:00 AM",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=600&auto=format&fit=crop",
                isCached = true
            ),
            Event(
                id = "evt_102",
                title = "SRC Student Town Hall & Policy Forum",
                description = "Open forum with Student Representative Council leaders to discuss campus facilities, academic policy updates, and student services.",
                venue = "Student Union Amphitheatre",
                venue_name = "Student Union Amphitheatre",
                date = "22 Oct 2026, 02:00 PM",
                event_date = "22 Oct 2026, 02:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1511578314322-379afb476865?w=600&auto=format&fit=crop",
                isCached = true
            ),
            Event(
                id = "evt_103",
                title = "Spring Cultural Festival & Food Market",
                description = "Celebrate campus diversity with live student music performances, food stalls, cultural exhibitions, and society clubs.",
                venue = "Campus Quad & Law Lawns",
                venue_name = "Campus Quad",
                date = "05 Nov 2026, 11:30 AM",
                event_date = "05 Nov 2026, 11:30 AM",
                imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop",
                isCached = true
            ),
            Event(
                id = "evt_104",
                title = "AI & Career Expo 2026",
                description = "Connect with top corporate engineering firms, tech startups, and recruiters hiring computer science and IT graduates.",
                venue = "Science Faculty Complex",
                venue_name = "Science Faculty Complex",
                date = "12 Nov 2026, 09:00 AM",
                event_date = "12 Nov 2026, 09:00 AM",
                imageUrl = "https://images.unsplash.com/photo-1528605248644-14dd04022da1?w=600&auto=format&fit=crop",
                isCached = true
            )
        )
    }

    suspend fun getEvents(): List<Event> {
        var localEvents = eventDao.getAllEvents()

        if (localEvents.isEmpty()) {
            eventDao.insertEvents(DEFAULT_EVENTS)
            localEvents = DEFAULT_EVENTS
        }

        try {
            val response = ApiClient.apiService.getEvents()
            if (response.isSuccessful && response.body() != null) {
                val remoteEvents = response.body()!!
                if (remoteEvents.isNotEmpty()) {
                    eventDao.insertEvents(remoteEvents)
                    localEvents = eventDao.getAllEvents()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return localEvents.ifEmpty { DEFAULT_EVENTS }
    }
}