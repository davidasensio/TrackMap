package com.handysparksoft.data.source

import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.UserProfileData

interface RemoteDataSource {
    suspend fun saveUser(userId: String)
    suspend fun updateUser(userId: String, userProfileData: UserProfileData)
    suspend fun getUserTrackMaps(userId: String): Map<String, TrackMap>
    suspend fun saveTrackMap(userId: String, trackMapId: String, trackMap: TrackMap)
    suspend fun joinTrackMap(trackMapId: String)
    suspend fun getTrackMapById(trackMapId: String): TrackMap?
}
