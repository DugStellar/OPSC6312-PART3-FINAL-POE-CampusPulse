package com.campuspulse.campuspulse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.campuspulse.campuspulse.databinding.FragmentTicketsBinding
import kotlinx.coroutines.launch

class TicketsFragment : Fragment() {

    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TicketAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TicketAdapter(emptyList())
        binding.rvTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTickets.adapter = adapter

        loadTickets()
    }

    override fun onResume() {
        super.onResume()
        loadTickets()
    }

    private fun loadTickets() {
        val pref = PreferenceManager(requireContext())
        val userId = pref.getUserId() ?: "usr_default"
        val db = CampusPulseDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            try {
                val userTickets = db.ticketDao().getTicketsForUser(userId)
                val allTickets = if (userTickets.isNotEmpty()) userTickets else db.ticketDao().getAllTickets()

                if (allTickets.isNotEmpty()) {
                    adapter.updateTickets(allTickets)
                    binding.rvTickets.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                } else {
                    binding.rvTickets.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.rvTickets.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}