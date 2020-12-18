package com.handysparksoft.trackmap.core.data.server

import com.handysparksoft.data.Result
import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.*
import com.handysparksoft.trackmap.core.data.server.NetworkHelper.safeApiCall

class ServerDataSource(private val service: TrackMapService) : RemoteDataSource {


    override suspend fun saveUser(userId: String, batteryLevel: Long, userToken: String?, lastAccess: Long) {
        val user = UserAccessData(userId, batteryLevel, userToken, lastAccess)
        safeApiCall { service.saveUser(userId, user) }
    }

    override suspend fun updateUser(userId: String, userProfileData: UserProfileData) {
        safeApiCall { service.updateUserName(userId, userProfileData) }
    }

    override suspend fun getUserTrackMaps(userId: String): Result<Map<String, TrackMap>> {
        return safeApiCall { service.getUserTrackMaps(userId) }
    }

    override suspend fun saveTrackMap(trackMapId: String, trackMap: TrackMap) {
        safeApiCall { service.saveTrackMap(trackMapId, trackMap) }
    }

    override suspend fun saveUserTrackMap(userId: String, trackMapId: String, trackMap: TrackMap) {
        safeApiCall { service.saveUserTrackMap(userId, trackMapId, trackMap) }
    }

    override suspend fun joinTrackMap(
        userId: String,
        trackMapId: String,
        trackMapParticipant: TrackMapParticipant
    ) {
        safeApiCall { service.joinTrackMap(trackMapId, trackMapParticipant) }
    }

    override suspend fun getTrackMapById(trackMapId: String): Result<TrackMap?> {
        val result = safeApiCall { service.getTrackMaps() }
        return when (result) {
            is Result.Success -> {
                val data = result.data.values.firstOrNull { it.trackMapId == trackMapId }
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error(false, null, null)
                }
            }
            is Result.Error -> Result.Error(
                result.isNetworkError,
                result.code,
                result.errorResponse
            )
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double) {
        safeApiCall { service.updateUserLocation(userId, UserLocationData(latitude, longitude, System.currentTimeMillis())) }
    }

    override suspend fun updateUserGPSData(userId: String, userGPSData: UserGPSData) {
        safeApiCall { service.updateUserAltitude(userId, userGPSData) }
    }

    override suspend fun leaveTrackMap(
        userId: String,
        trackMapId: String,
        trackMapParticipant: TrackMapParticipant
    ) {
        safeApiCall {
            service.deleteUserTrackMap(userId, trackMapId)
            service.deleteTrackMapParticipantId(trackMapId, trackMapParticipant)
        }
    }

    override suspend fun getUserProfileData(userId: String): Result<UserProfileData> {
        return safeApiCall { service.getUserProfileData(userId) }
    }

    override suspend fun getUserAccessData(userId: String): Result<UserAccessData> {
        return safeApiCall { service.getUserAccessData(userId) }
    }

    override suspend fun favoriteTrackMap(userId: String, trackMapId: String, favorite: Boolean) {
        safeApiCall { service.markAsFavoriteTrackMap(userId, trackMapId, TrackMapConfig(trackMapId, favorite)) }
    }

    override suspend fun sendPushNotification(authorization: String, pushNotification: PushNotification) {
        safeApiCall { service.sendPushNotification(authorization, pushNotification) }
    }
}
