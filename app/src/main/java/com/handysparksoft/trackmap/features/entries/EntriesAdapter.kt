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
    private val userSession: String,
    private val onGoListener: (trackMap: TrackMap) -> Unit,
    private val onLeaveListener: (trackMap: TrackMap) -> Unit,
    val onShareListener: (trackMap: TrackMap) -> Unit
) : RecyclerView.Adapter<EntriesAdapter.ViewHolder>() {

    var items: List<TrackMap> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTrackmapBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trackMap = items[position]
        holder.bind(trackMap)
        //holder.itemView.setOnClickListener { listener(trackMap) }
    }

    inner class ViewHolder(private val itemBinding: ItemTrackmapBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        //@Suppress("DEPRECATION")
        fun bind(trackMap: TrackMap) {
            if (trackMap.participantIds != null) {
                itemBinding.creationDateTextView.text = Html.fromHtml(
                    getCreationText(itemBinding.root, trackMap.ownerId, trackMap.creationDate)
                )
                itemBinding.nameTextView.text = trackMap.name
                itemBinding.descriptionTextView.text = trackMap.description
                itemBinding.codeTextView.text = trackMap.trackMapId
                itemBinding.participantsTextView.text = (trackMap.participantIds.size).toString()
                itemBinding.goButton.setOnClickListener {
                    onGoListener.invoke(trackMap)
                }
                itemBinding.leaveButton.setOnClickListener {
                    onLeaveListener.invoke(trackMap)
                }
                itemBinding.trackMapShareImageButton.setOnClickListener {
                    onShareListener.invoke(trackMap)
                }
            }
        }

        private fun getCreationText(
            view: View,
            owner: String,
            creationDate: Long
        ): String {
            val owned = owner == userSession
            val ownerAlias = if (owned) "You" else owner
            val dateFromTime = DateUtils.getRelativeDateFromTime(view.context, creationDate)
            return view.context.getString(R.string.creation_template, dateFromTime, ownerAlias)
        }
    }
}
