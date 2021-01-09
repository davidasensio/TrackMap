package com.handysparksoft.trackmap.features.participants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.handysparksoft.domain.model.UserProfileData
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.platform.Base64Utils
import com.handysparksoft.trackmap.databinding.ItemTrackmapParticipantBinding

class ParticipantsAdapter(
    private val userSessionId: String,
    private val trackMapOwnerId: String,
    private val onProfileImageClickListener: (userProfileImageView: View, userProfileData: UserProfileData) -> Unit,
    private val onClickListener: () -> Unit
) : RecyclerView.Adapter<ParticipantsAdapter.ViewHolder>() {

    var items: List<UserProfileData> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTrackmapParticipantBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userData = items[position]
        holder.bind(userData)
    }

    inner class ViewHolder(private val itemBinding: ItemTrackmapParticipantBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(userProfileData: UserProfileData) {
            val isOwner = userProfileData.userId == trackMapOwnerId
            val isUserSession = userProfileData.userId == userSessionId
            val nickname = userProfileData.nickname
            val yourSuffix = " (" + itemBinding.root.context.getString(R.string.you) + ")"
            val yourNickname = nickname + yourSuffix

            itemBinding.userNickname.text = if (isUserSession) yourNickname else nickname
            itemBinding.userFullname.text = userProfileData.fullName
            itemBinding.userOwnerTagView.visibility = if (isOwner) View.VISIBLE else View.GONE
            itemBinding.userProfileImage.setOnClickListener {
                onProfileImageClickListener.invoke(it, userProfileData)
            }
            itemBinding.root.setOnClickListener {
                onClickListener.invoke()
            }
            userProfileData.image?.let { encodedImage ->
                itemBinding.userProfileImage.setImageBitmap(Base64Utils.getBase64Bitmap(encodedImage))
            }
        }
    }
}
