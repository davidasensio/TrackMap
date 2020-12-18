package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository

class FavoriteTrackMapUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, trackMapId: String, favorite: Boolean) {
        trackMapRepository.favoriteTrackMap(userId, trackMapId, favorite)
    }
}
