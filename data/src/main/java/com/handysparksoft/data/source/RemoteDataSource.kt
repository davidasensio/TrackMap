package com.handysparksoft.data.source

import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.TrackMapParticipant
import com.handysparksoft.domain.model.UserGPSData
import com.handysparksoft.domain.model.UserProfileData

interface RemoteDataSource {
    suspend fun saveUser(userId: String)
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
}
