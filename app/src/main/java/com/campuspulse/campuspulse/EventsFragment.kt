package com.campuspulse.campuspulse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campuspulse.campuspulse.databinding.FragmentEventsBinding
import kotlinx.coroutines.launch

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.progressBar.visibility = View.VISIBLE

        val db = CampusPulseDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            try {
                var events = db.eventDao().getAllEvents()

                // Seed sample campus events if Room DB is currently empty
                if (events.isEmpty()) {
                    val sampleEvents = listOf(
                        Event(
                            id = "1",
                            title = "Campus Tech Hackathon 2026",
                            description = "Join top student developers for a 24-hour hackathon focused on mobile & web solutions.",
                            venue = "Computer Science Lab A",
                            date = "Oct 15, 2026"
                        ),
                        Event(
                            id = "2",
                            title = "Spring Society Marketplace",
                            description = "Discover campus clubs, join student organizations, and collect free merchandise.",
                            venue = "Student Union Plaza",
                            date = "Oct 20, 2026"
                        ),
                        Event(
                            id = "3",
                            title = "AI & Software Career Expo",
                            description = "Meet industry recruiters and software firms hiring third-year graduates.",
                            venue = "Great Hall Auditorium",
                            date = "Nov 02, 2026"
                        ),
                        Event(
                            id = "4",
                            title = "Campus Music & Culture Night",
                            description = "Annual live concert featuring student bands and performing arts.",
                            venue = "Amphitheatre",
                            date = "Nov 18, 2026"
                        )
                    )
                    db.eventDao().insertEvents(sampleEvents)
                    events = sampleEvents
                }

                // Try fetching live updates from backend API in background
                try {
                    val apiResponse = ApiClient.apiService.getEvents()
                    if (apiResponse.isSuccessful && apiResponse.body() != null) {
                        val remoteEvents = apiResponse.body()!!
                        if (remoteEvents.isNotEmpty()) {
                            db.eventDao().clearEvents()
                            db.eventDao().insertEvents(remoteEvents)
                            events = remoteEvents
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                binding.progressBar.visibility = View.GONE

                if (events.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvEvents.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvEvents.visibility = View.VISIBLE
                    binding.rvEvents.adapter = EventAdapter(events) { event ->
                        val intent = Intent(requireContext(), EventDetailsActivity::class.java).apply {
                            putExtra("EVENT_ID", event.id)
                            putExtra("EVENT_TITLE", event.title)
                            putExtra("EVENT_DESC", event.description)
                            putExtra("EVENT_DATE", event.getDisplayDate())
                            putExtra("EVENT_LOCATION", event.getDisplayVenue())
                            putExtra("EVENT_IMAGE_URL", event.imageUrl)
                        }
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}