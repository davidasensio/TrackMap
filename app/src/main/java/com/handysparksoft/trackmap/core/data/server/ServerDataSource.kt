package com.handysparksoft.trackmap.core.data.server

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.User
import com.handysparksoft.domain.model.UserProfileData

class ServerDataSource(private val service: TrackMapService) : RemoteDataSource {

    override suspend fun saveUser(userId: String) {
        val user = User(userId, System.currentTimeMillis())
        service.saveUser(userId, user)
    }

    override suspend fun updateUser(userId: String, userProfileData: UserProfileData) {
        service.updateUserName(userId, userProfileData)
    }

    override suspend fun getUserTrackMaps(userId: String): Map<String, TrackMap> {
        return service.getUserTrackMaps(userId)
    }

    override suspend fun saveTrackMap(userId: String, trackMapId: String, trackMap: TrackMap) {
        service.saveTrackMap(trackMapId, trackMap)
        service.saveUserTrackMap(userId, trackMapId, trackMap)
    }

    override suspend fun joinTrackMap(trackMapId: String) {
//        service.joinTrackMap(trackMapId)
    }

    override suspend fun getTrackMapById(trackMapId: String): TrackMap? {
//        val userOwner = service.getUsers().values.firstOrNull { user ->
//            user.trackMaps.any { trackMap ->
//                trackMap.value.trackMapId == trackMapId &&
//                        trackMap.value.ownerId == user.id
//            }
//        }
//        return userOwner?.trackMaps?.values?.firstOrNull { it.trackMapId == trackMapId }

        return service.getTrackMaps().values.firstOrNull { it.trackMapId == trackMapId}
    }
}
