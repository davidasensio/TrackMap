package com.handysparksoft.trackmap.core.data.server

import com.handysparksoft.domain.model.*
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

    @PATCH("users/{userId}/trackMaps/{trackMapId}.json")
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

    @PATCH("trackMaps/{trackMapId}.json")
    suspend fun joinTrackMap(
        @Path("trackMapId") trackMapId: String,
        @Body trackMapParticipant: TrackMapParticipant
    )

    @PATCH("users/{userId}.json")
    suspend fun updateUserLocation(
        @Path("userId") userId: String,
        @Body user: UserLocationData
    )

    @DELETE("users/{userId}/trackMaps/{trackMapId}.json")
    suspend fun deleteUserTrackMap(
        @Path("userId") userId: String,
        @Path("trackMapId") trackMapId: String
    )

    @PATCH("trackMaps/{trackMapId}.json")
    suspend fun deleteTrackMapParticipantId(
        @Path("trackMapId") trackMapId: String,
        @Body trackMapParticipant: TrackMapParticipant
    )
}
