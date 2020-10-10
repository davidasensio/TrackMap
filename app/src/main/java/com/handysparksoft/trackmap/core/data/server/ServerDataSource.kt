package com.handysparksoft.trackmap.core.data.server

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.*
import com.handysparksoft.trackmap.core.platform.ConnectionHandler

class ServerDataSource(private val service: TrackMapService) : RemoteDataSource {

    override suspend fun saveUser(userId: String) {
        if (ConnectionHandler.isNetworkConnected) {
        val user = User(userId, System.currentTimeMillis())
        service.saveUser(userId, user)
        }
    }

    override suspend fun updateUser(userId: String, userProfileData: UserProfileData) {
        if (ConnectionHandler.isNetworkConnected) {
            service.updateUserName(userId, userProfileData)
        }
    }

    override suspend fun getUserTrackMaps(userId: String): Map<String, TrackMap> {
        return try {
            service.getUserTrackMaps(userId)
        } catch (e: Exception) {
            HashMap() // Return an empty HashMap when a request returns null
        }
    }

    override suspend fun saveTrackMap(trackMapId: String, trackMap: TrackMap) {
        if (ConnectionHandler.isNetworkConnected) {
            service.saveTrackMap(trackMapId, trackMap)
        }
    }

    override suspend fun saveUserTrackMap(userId: String, trackMapId: String, trackMap: TrackMap) {
        if (ConnectionHandler.isNetworkConnected) {
            service.saveUserTrackMap(userId, trackMapId, trackMap)
        }
    }

    override suspend fun joinTrackMap(userId: String, trackMapId: String, trackMapParticipant: TrackMapParticipant) {
        if (ConnectionHandler.isNetworkConnected) {
            service.joinTrackMap(trackMapId, trackMapParticipant)
        }
    }

    override suspend fun getTrackMapById(trackMapId: String): TrackMap? {
        if (ConnectionHandler.isNetworkConnected) {
            return service.getTrackMaps().values.firstOrNull { it.trackMapId == trackMapId }
        }
        return null
    }

    override suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double) {
        if (ConnectionHandler.isNetworkConnected) {
            service.updateUserLocation(userId, UserLocationData(latitude, longitude))
        }
    }

    override suspend fun leaveTrackMap(
        userId: String,
        trackMapId: String,
        trackMapParticipant: TrackMapParticipant
    ) {
        if (ConnectionHandler.isNetworkConnected) {
            service.deleteUserTrackMap(userId, trackMapId)
            service.deleteTrackMapParticipantId(trackMapId, trackMapParticipant)
        }
    }
}
