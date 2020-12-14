package com.handysparksoft.data.source

import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.*

interface RemoteDataSource {
    suspend fun saveUser(userId: String, userToken: String?)
    suspend fun updateUser(userId: String, userProfileData: UserProfileData)
    suspend fun getUserTrackMaps(userId: String): Result<Map<String, TrackMap>>
    suspend fun saveTrackMap(trackMapId: String, trackMap: TrackMap)
    suspend fun saveUserTrackMap(userId: String, trackMapId: String, trackMap: TrackMap)
    suspend fun joinTrackMap(userId: String, trackMapId: String, trackMapParticipant: TrackMapParticipant)
    suspend fun getTrackMapById(trackMapId: String): Result<TrackMap?>
    suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double)
    suspend fun updateUserGPSData(userId: String, userGPSData: UserGPSData)
    suspend fun leaveTrackMap(userId: String, trackMapId: String, trackMapParticipant: TrackMapParticipant)
    suspend fun getUserProfileData(userId: String): Result<UserProfileData>
    suspend fun getUserAccessData(userId: String): Result<UserAccessData>

    suspend fun sendPushNotification(authorization: String, pushNotification: PushNotification)
}
