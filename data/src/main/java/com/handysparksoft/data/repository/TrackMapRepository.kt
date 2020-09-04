package com.handysparksoft.data.repository

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.TrackMap
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

    suspend fun saveTrackMap(userId: String, trackMapId: String, trackMap: TrackMap) {
        remoteDataSource.saveTrackMap(userId, trackMapId, trackMap)
    }

    suspend fun joinTrackMap(userId:String, trackMapId: String) {
        remoteDataSource.joinTrackMap(userId, trackMapId)
    }

    suspend fun getTrackMapById(trackMapId: String): TrackMap? {
        return remoteDataSource.getTrackMapById(trackMapId)
    }
}
