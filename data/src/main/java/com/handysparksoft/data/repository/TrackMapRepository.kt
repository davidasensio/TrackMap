package com.handysparksoft.data.repository

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.TrackMapParticipant
import com.handysparksoft.domain.model.UserProfileData

class TrackMapRepository(private val remoteDataSource: RemoteDataSource) {

    suspend fun saveUser(userId: String) {
        remoteDataSource.saveUser(userId)
    }

    suspend fun updateUser(userId: String, userProfileData: UserProfileData) {
        remoteDataSource.updateUser(userId, userProfileData)
    }

    suspend fun getUserTrackMaps(userId: String): Map<String, TrackMap> {
        return remoteDataSource.getUserTrackMaps(userId)
    }

    suspend fun saveTrackMap(trackMapId: String, trackMap: TrackMap) {
        remoteDataSource.saveTrackMap(trackMapId, trackMap)
    }

    suspend fun saveUserTrackMap(userId: String, trackMapId: String, trackMap: TrackMap) {
        remoteDataSource.saveUserTrackMap(userId, trackMapId, trackMap)
    }

    suspend fun joinTrackMap(userId: String, trackMapId: String, trackMap: TrackMap, trackMapParticipant: TrackMapParticipant) {
        remoteDataSource.joinTrackMap(userId, trackMapId, trackMap, trackMapParticipant)
    }

    suspend fun getTrackMapById(trackMapId: String): TrackMap? {
        return remoteDataSource.getTrackMapById(trackMapId)
    }

    suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double) {
        return remoteDataSource.updateUserLocation(userId, latitude, longitude)
    }
}
