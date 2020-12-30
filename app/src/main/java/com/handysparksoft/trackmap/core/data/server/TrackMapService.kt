package com.handysparksoft.trackmap.core.data.server

import com.handysparksoft.domain.model.*
import retrofit2.http.*

interface TrackMapService {

    @GET("users.json")
    suspend fun getUsers(): Map<String, UserTrackMaps>

    @GET("users/{userId}.json")
    suspend fun getUserAccessData(@Path("userId") userId: String): UserAccessData

    @GET("users/{userId}.json")
    suspend fun getUserProfileData(@Path("userId") userId: String): UserProfileData

    @GET("trackMaps.json")
    suspend fun getTrackMaps(): Map<String, TrackMap>

    @GET("users/{userId}/trackMaps.json")
    suspend fun getUserTrackMaps(@Path("userId") userId: String): Map<String, TrackMap>

    @PATCH("users/{userId}.json")
    suspend fun saveUser(
        @Path("userId") userId: String,
        @Body userAccessData: UserAccessData
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

    @PATCH("users/{userId}.json")
    suspend fun updateUserAltitude(
        @Path("userId") userId: String,
        @Body user: UserGPSData
    )

    @PATCH("users/{userId}.json")
    suspend fun updateUserBatteryLevel(
        @Path("userId") userId: String,
        @Body userData: UserBatteryLevel
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

    @PATCH("users/{userId}/trackMaps/{trackMapId}.json")
    suspend fun markAsFavoriteTrackMap(
        @Path("userId") userId: String,
        @Path("trackMapId") trackMapId: String,
        @Body trackMapConfig: TrackMapConfig
    )

    @Headers("Content-Type: application/json")
    @POST("https://fcm.googleapis.com/fcm/send")
    suspend fun sendPushNotification(
        @Header("Authorization") authorization: String,
        @Body pushNotification: PushNotification
    )

    @PATCH("trackMaps/{trackMapId}.json")
    suspend fun startLiveTracking(
        @Path("trackMapId") trackMapId: String,
        @Body liveTrackingParticipant: TrackMapLiveTrackingParticipant
    )

    @PATCH("trackMaps/{trackMapId}.json")
    suspend fun stopLiveTracking(
        @Path("trackMapId") trackMapId: String,
        @Body liveTrackingParticipant: TrackMapLiveTrackingParticipant
    )
}
