package com.handysparksoft.trackmap.features.entries

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.platform.DateUtils
import com.handysparksoft.trackmap.databinding.ItemTrackmapBinding

class EntriesAdapter(
    private val userSession: String,
    private val onGoListener: (trackMap: TrackMap) -> Unit,
    private val onLeaveListener: (trackMap: TrackMap) -> Unit,
    private val onFavoriteListener: (trackMap: TrackMap, favorite: Boolean) -> Unit,
    private val onLiveTrackingListener: (trackMap: TrackMap, startTracking: Boolean) -> Unit,
    private val onShareListener: (trackMap: TrackMap) -> Unit,
    private val onShowParticipantsListener: (trackMap: TrackMap) -> Unit
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
    }

    inner class ViewHolder(private val itemBinding: ItemTrackmapBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        //@Suppress("DEPRECATION")
        fun bind(trackMap: TrackMap) {
            if (trackMap.participantIds != null) {
                val ownerName = trackMap.ownerName ?: trackMap.ownerId
                itemBinding.creationDateTextView.text = Html.fromHtml(
                    getCreationText(itemBinding.root, ownerName, trackMap.creationDate)
                )
                itemBinding.nameTextView.text = trackMap.name
                itemBinding.descriptionTextView.text = trackMap.description
                itemBinding.codeBottomTextView.text = trackMap.trackMapId
                itemBinding.participantsBottomTextView.text =
                    (trackMap.participantIds.size).toString()
                setFavoriteState(itemBinding.trackMapFavoriteImageButton, trackMap.favorite == true)

                setLiveTrackingState(
                    itemBinding.trackMapLiveTrackingButton,
                    trackMap.liveParticipantIds?.contains(userSession) == true
                )

                // Bind listeners
                itemBinding.codeBottomTextView.setOnClickListener {
                    onShareListener.invoke(trackMap)
                }
                itemBinding.participantsBottomTextView.setOnClickListener {
                    onShowParticipantsListener.invoke(trackMap)
                }
                itemBinding.trackMapLeaveImageButton.setOnClickListener {
                    onLeaveListener.invoke(trackMap)
                }
                itemBinding.trackMapFavoriteImageButton.setOnClickListener {
                    val selected = it.tag == true
                    setFavoriteState(itemBinding.trackMapFavoriteImageButton, !selected)
                    onFavoriteListener(trackMap, !selected)

                }
                itemBinding.trackMapLiveTrackingButton.setOnClickListener {
                    val selected = it.tag == true
                    setLiveTrackingState(itemBinding.trackMapLiveTrackingButton, !selected)
                    onLiveTrackingListener(trackMap, !selected)

                }
                itemBinding.trackMapItemContent.setOnClickListener {
                    onGoListener.invoke(trackMap)
                }
            }
        }

        private fun getCreationText(
            view: View,
            owner: String,
            creationDate: Long
        ): String {
            val owned = owner == userSession
            val ownerAlias = if (owned) view.context.getString(R.string.by_you) else owner
            val dateFromTime = DateUtils.getRelativeDateFromTime(view.context, creationDate)
            return view.context.getString(R.string.creation_template, dateFromTime, ownerAlias)
        }

        private fun setFavoriteState(imageView: ImageView, selected: Boolean) {
            if (!selected) {
                imageView.colorFilter = null
                imageView.setImageResource(R.drawable.ic_star)

            } else {
                imageView.setColorFilter(
                    ContextCompat.getColor(
                        imageView.context,
                        R.color.colorAccent
                    )
                )
                imageView.setImageResource(R.drawable.ic_star_filled)
            }
            imageView.tag = selected
        }

        private fun setLiveTrackingState(imageView: ImageView, selected: Boolean) {
            if (!selected) {
                imageView.colorFilter = null
                imageView.setImageResource(R.drawable.ic_live_tracking)
                imageView.clearAnimation()

            } else {
                imageView.setColorFilter(
                    ContextCompat.getColor(
                        imageView.context,
                        R.color.colorAlert
                    )
                )
                imageView.setImageResource(R.drawable.ic_live_tracking)
                imageView.startAnimation(
                    AnimationUtils.loadAnimation(
                        imageView.context,
                        R.anim.beat_animation
                    )
                )
            }
            imageView.tag = selected
        }
    }
}
