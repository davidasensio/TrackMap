package com.handysparksoft.data.repository

import com.handysparksoft.data.Result
import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.TrackMapParticipant
import com.handysparksoft.domain.model.UserGPSData
import com.handysparksoft.domain.model.UserProfileData

class TrackMapRepository(private val remoteDataSource: RemoteDataSource) {

    suspend fun saveUser(userId: String) {
        remoteDataSource.saveUser(userId)
    }

    suspend fun updateUserProfile(userId: String, userProfileData: UserProfileData) {
        remoteDataSource.updateUser(userId, userProfileData)
    }

    suspend fun getUserTrackMaps(userId: String): Result<Map<String, TrackMap>> {
        return remoteDataSource.getUserTrackMaps(userId)
    }

    suspend fun saveTrackMap(trackMapId: String, trackMap: TrackMap) {
        remoteDataSource.saveTrackMap(trackMapId, trackMap)
    }

    suspend fun saveUserTrackMap(userId: String, trackMapId: String, trackMap: TrackMap) {
        remoteDataSource.saveUserTrackMap(userId, trackMapId, trackMap)
    }

    suspend fun joinTrackMap(userId: String, trackMapId: String, trackMapParticipant: TrackMapParticipant) {
        remoteDataSource.joinTrackMap(userId, trackMapId, trackMapParticipant)
    }

    suspend fun getTrackMapById(trackMapId: String): Result<TrackMap?> {
        return remoteDataSource.getTrackMapById(trackMapId)
    }

    suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double) {
        return remoteDataSource.updateUserLocation(userId, latitude, longitude)
    }

    suspend fun updateUserGPSData(userId: String, userGPSData: UserGPSData) {
        return remoteDataSource.updateUserGPSData(userId, userGPSData)
    }

    suspend fun leaveTrackMap(userId: String, trackMapId: String, trackMapParticipant: TrackMapParticipant) {
        remoteDataSource.leaveTrackMap(userId, trackMapId, trackMapParticipant)
    }

    suspend fun getUserProfileData(userId: String): Result<UserProfileData> {
        return remoteDataSource.getUserProfileData(userId)
    }
}
