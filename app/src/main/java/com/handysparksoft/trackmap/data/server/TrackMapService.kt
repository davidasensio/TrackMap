package com.handysparksoft.trackmap.data.server

import com.handysparksoft.trackmap.domain.TrackMap
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface TrackMapService {

    @GET("trackmaps.json")
    suspend fun all(): Any

    @GET("trackmaps/maps/list.json")
    suspend fun listTrackMapAsync(): Map<String, TrackMap>

    @PUT("trackmaps/maps/list/{id}.json")
    suspend fun putTrackMap(@Path("id") id: String, @Body trackMap: TrackMap)
}
