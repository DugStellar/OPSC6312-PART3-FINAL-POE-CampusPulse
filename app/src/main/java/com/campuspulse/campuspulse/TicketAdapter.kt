package com.campuspulse.campuspulse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.campuspulse.campuspulse.databinding.ItemTicketBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TicketAdapter(
    private var tickets: List<Ticket>,
    private val onItemClick: ((Ticket) -> Unit)? = null
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(val binding: ItemTicketBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val context = holder.itemView.context
        val ticket = tickets[position]

        holder.binding.tvTicketId.text = context.getString(R.string.ticket_id_prefix, ticket.getDisplayTicketId())
        holder.binding.tvEventId.text = context.getString(R.string.event_id_prefix, ticket.getDisplayEventId())

        val statusText = ticket.getDisplaySyncStatus()
        holder.binding.tvSyncStatus.text = context.getString(R.string.sync_status_prefix, statusText)

        if (ticket.isSynced || statusText.equals("Synced", ignoreCase = true)) {
            holder.binding.tvSyncStatus.setTextColor(ContextCompat.getColor(context, R.color.status_online))
        } else {
            holder.binding.tvSyncStatus.setTextColor(ContextCompat.getColor(context, R.color.status_offline))
        }

        // Dynamic Ticket Image Loading (Requirement 2 & 4)
        holder.binding.ivTicketImage.load(ticket.imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_agenda)
            error(android.R.drawable.ic_menu_agenda)
        }

        // Full card clickability & interaction
        holder.binding.root.setOnClickListener {
            onItemClick?.invoke(ticket)
            showTicketDetailsDialog(context, ticket)
        }
    }

    private fun showTicketDetailsDialog(context: android.content.Context, ticket: Ticket) {
        val qrCode = ticket.qr_code_data ?: "QR-${ticket.getDisplayTicketId()}"
        val statusText = ticket.getDisplaySyncStatus()

        val detailsMessage = """
            ${context.getString(R.string.ticket_id_prefix, ticket.getDisplayTicketId())}
            ${context.getString(R.string.event_id_prefix, ticket.getDisplayEventId())}
            ${context.getString(R.string.sync_status_prefix, statusText)}
            ${context.getString(R.string.qr_code_label, qrCode)}
        """.trimIndent()

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.ticket_details_title))
            .setIcon(android.R.drawable.ic_menu_agenda)
            .setMessage(detailsMessage)
            .setPositiveButton(context.getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()

        NotificationHelper.showNotification(
            context,
            context.getString(R.string.push_notif_title),
            "Viewing Ticket ${ticket.getDisplayTicketId()}"
        )
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(newTickets: List<Ticket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }
}