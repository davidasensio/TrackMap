package com.handysparksoft.trackmap.data.server

import com.handysparksoft.trackmap.domain.TrackMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

class TrackMapRepository {
    private val service = TrackMapDb.service

    suspend fun getTrackMapList(): List<TrackMap> {
        return service.listTrackMapAsync()
    }
}
