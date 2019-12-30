package com.handysparksoft.trackmap.domain

data class TrackMap(
    val code: String,
    val name: String,
    val description: String,
    val owner: String,
    val active: Boolean,
    val creationDate: Long = System.currentTimeMillis()
)
