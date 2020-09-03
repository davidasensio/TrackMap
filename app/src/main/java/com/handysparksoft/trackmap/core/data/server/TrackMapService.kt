package com.handysparksoft.trackmap.core.data.server

import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.User
import com.handysparksoft.domain.model.UserProfileData
import com.handysparksoft.domain.model.UserTrackMaps
import retrofit2.http.*

interface TrackMapService {

    @GET("users.json")
    suspend fun getUsers(): Map<String, UserTrackMaps>

    @GET("trackMaps.json")
    suspend fun getTrackMaps(): Map<String, TrackMap>

    @GET("users/{userId}/trackMaps.json")
    suspend fun getUserTrackMaps(@Path("userId") userId: String): Map<String, TrackMap>

    @PATCH("users/{userId}.json")
    suspend fun saveUser(
        @Path("userId") userId: String,
        @Body user: User
    )

    @PATCH("users/{userId}.json")
    suspend fun updateUserName(
        @Path("userId") userId: String,
        @Body user: UserProfileData
    )

    @PUT("users/{userId}/trackMaps/{trackMapId}.json")
    suspend fun saveUserTrackMap(
        @Path("userId") userId: String,
        @Path("trackMapId") trackMapId: String,
        @Body trackMap: TrackMap
    )

    @PUT("trackMaps/{trackMapId}.json")
    suspend fun saveTrackMap(
        @Path("trackMapId") trackMapId: String,
        @Body trackMap: TrackMap
    )

    @PUT("trackMaps/{trackMapId}/participantIds/{userId}.json")
    suspend fun joinTrackMap(
        @Path("trackMapId") trackMapId: String,
        @Path("userId") userId: String,
        @Body id: TrackMap
    )
}
