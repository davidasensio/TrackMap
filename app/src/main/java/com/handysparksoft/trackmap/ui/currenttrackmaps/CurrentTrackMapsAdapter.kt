package com.handysparksoft.trackmap.ui.currenttrackmaps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.handysparksoft.trackmap.R
import com.handysparksoft.domain.model.TrackMap
import kotlinx.android.synthetic.main.item_current_trackmap.view.*

class CurrentTrackMapsAdapter(
    val listener: (trackMap: TrackMap) -> Unit
) : RecyclerView.Adapter<CurrentTrackMapsAdapter.ViewHolder>() {

    var items: List<TrackMap> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_current_trackmap, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trackMap = items[position]
        holder.bind(trackMap)
        holder.itemView.setOnClickListener { listener(trackMap) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(trackMap: TrackMap) {
            itemView.code.text = trackMap.code
            itemView.name.text = trackMap.name
        }
    }
}
