package com.handysparksoft.trackmap.features.entries

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.platform.DateUtils
import com.handysparksoft.trackmap.databinding.ItemTrackmapBinding

class EntriesAdapter(
    val onGoListener: (trackMap: TrackMap) -> Unit,
    val onLeaveListener: (trackMap: TrackMap) -> Unit,
    val onShareListener: (trackMap: TrackMap) -> Unit
) : RecyclerView.Adapter<EntriesAdapter.ViewHolder>() {

    lateinit var binding: ItemTrackmapBinding
    var items: List<TrackMap> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemTrackmapBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trackMap = items[position]
        holder.bind(trackMap)
        //holder.itemView.setOnClickListener { listener(trackMap) }
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        @Suppress("DEPRECATION")
        fun bind(trackMap: TrackMap) {
            binding.creationDateTextView.text = Html.fromHtml(
                getCreationText(view, trackMap.ownerId, trackMap.creationDate)
            )
            binding.nameTextView.text = trackMap.name
            binding.descriptionTextView.text = trackMap.description
            binding.codeTextView.text = trackMap.trackMapId
            binding.participantsTextView.text = (trackMap.participantIds.size).toString()
            binding.goButton.setOnClickListener {
                onGoListener.invoke(trackMap)
            }
            binding.leaveButton.setOnClickListener {
                onLeaveListener.invoke(trackMap)
            }
            binding.trackMapShareImageButton.setOnClickListener {
                onShareListener.invoke(trackMap)
            }
        }

        private fun getCreationText(view: View, owner: String, creationDate: Long): String {
            val dateFromTime = DateUtils.getRelativeDateFromTime(view.context, creationDate)
            return view.context.getString(R.string.creation_template, dateFromTime, owner)
        }
    }
}
