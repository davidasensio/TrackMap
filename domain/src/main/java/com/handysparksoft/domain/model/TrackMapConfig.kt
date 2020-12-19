package com.handysparksoft.domain.model

import java.io.Serializable

data class TrackMapConfig(
    val trackMapId: String,
    val favorite: Boolean?
) : Serializable
