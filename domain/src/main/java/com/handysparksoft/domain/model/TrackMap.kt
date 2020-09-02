package com.handysparksoft.domain.model

data class TrackMap(
    val code: String,
    val name: String,
    val description: String,
    val owner: String,
    val active: Boolean,
    val creationDate: Long = System.currentTimeMillis()
)
