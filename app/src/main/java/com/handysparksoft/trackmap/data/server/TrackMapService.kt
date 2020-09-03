package com.handysparksoft.trackmap.data.server

import com.handysparksoft.domain.model.TrackMap
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface TrackMapService {

    @GET("trackmaps.json")
    suspend fun all(): Any

    @GET("trackmaps/maps/list.json")
    suspend fun listTrackMaps(): Map<String, TrackMap>

    @PUT("trackmaps/maps/list/{id}.json")
    suspend fun saveTrackMap(@Path("id") id: String, @Body trackMap: TrackMap)
}
