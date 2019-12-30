package com.handysparksoft.trackmap.data.server

import com.handysparksoft.trackmap.domain.TrackMap
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.http.GET

interface TrackMapService {

    @GET("trackmaps.json")
    suspend fun all(): Any

    @GET("trackmaps/maps/list.json")
    suspend fun listTrackMapAsync(): List<TrackMap>
}
