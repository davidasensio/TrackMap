package com.handysparksoft.trackmap.features.entries

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.platform.DateUtils
import kotlinx.android.synthetic.main.item_trackmap.view.*

class TrackMapEntriesAdapter(
    val onGoListener: (trackMap: TrackMap) -> Unit,
    val onLeaveListener: (trackMap: TrackMap) -> Unit
) : RecyclerView.Adapter<TrackMapEntriesAdapter.ViewHolder>() {

    var items: List<TrackMap> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_trackmap, parent, false)
        return ViewHolder(view)
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
            itemView.creationDateTextView.text = Html.fromHtml(
                getCreationText(view, trackMap.ownerId, trackMap.creationDate)
            )
            itemView.nameTextView.text = trackMap.name
            itemView.descriptionTextView.text = trackMap.description
            itemView.codeTextView.text = trackMap.trackMapId
            itemView.participantsTextView.text = (trackMap.participantIds.size).toString()
            itemView.goButton.setOnClickListener {
                onGoListener.invoke(trackMap)
            }
            itemView.leaveButton.setOnClickListener {
                onLeaveListener.invoke(trackMap)
            }
        }

        private fun getCreationText(view: View, owner: String, creationDate: Long): String {
            val dateFromTime = DateUtils.getDateFromTime(creationDate)
            return view.context.getString(R.string.creation_template, dateFromTime, owner)
        }
    }
}
