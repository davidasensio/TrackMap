package com.handysparksoft.domain.model

import java.io.Serializable

data class TrackMap(
    val trackMapId: String,
    val ownerId: String,
    val ownerName: String,
    val name: String,
    val description: String,
    val creationDate: Long = System.currentTimeMillis(),
    val participantIds: List<String>,
    val liveParticipantIds: List<String>?,
    var favorite: Boolean?
) : Serializable
