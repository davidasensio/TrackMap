package com.handysparksoft.domain.model

data class UserMarkerData(
    var tag: String,
    var icon: Int,
    var isShowingInfoWindow: Boolean,
    var participanLocation: ParticipantLocation?
)
