package com.handysparksoft.trackmap.core.data.server

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.*

class ServerDataSource(private val service: TrackMapService) : RemoteDataSource {

    override suspend fun saveUser(userId: String) {
        val user = User(userId, System.currentTimeMillis())
        service.saveUser(userId, user)
    }

    override suspend fun updateUser(userId: String, userProfileData: UserProfileData) {
        service.updateUserName(userId, userProfileData)
    }

    override suspend fun getUserTrackMaps(userId: String): Map<String, TrackMap> {
        return try {
            service.getUserTrackMaps(userId)
        } catch (e: Exception) {
            HashMap() // Return an empty HashMap when a request returns null
        }
    }

    override suspend fun saveTrackMap(trackMapId: String, trackMap: TrackMap) {
        service.saveTrackMap(trackMapId, trackMap)
    }

    override suspend fun saveUserTrackMap(userId: String, trackMapId: String, trackMap: TrackMap) {
        service.saveUserTrackMap(userId, trackMapId, trackMap)
    }

    override suspend fun joinTrackMap(userId: String, trackMapId: String, trackMap: TrackMap, trackMapParticipant: TrackMapParticipant) {
        val participantIdsUpdated = trackMap.participantIds.toMutableList()
        participantIdsUpdated.add(userId)

        service.joinTrackMap(trackMapId, trackMapParticipant)
    }

    override suspend fun getTrackMapById(trackMapId: String): TrackMap? {
        return service.getTrackMaps().values.firstOrNull { it.trackMapId == trackMapId}
    }

    override suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double) {
        service.updateUserLocation(userId, UserLocationData(latitude, longitude))
    }
}
