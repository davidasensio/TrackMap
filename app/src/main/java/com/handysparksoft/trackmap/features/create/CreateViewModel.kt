package com.handysparksoft.trackmap.features.create

import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.usecases.SaveTrackMapUseCase
import com.handysparksoft.usecases.SaveUserTrackMapUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateViewModel(
    private val saveTrackMapUseCase: SaveTrackMapUseCase,
    private val saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
    private val userHandler: UserHandler
) : ViewModel(), Scope by Scope.Impl() {

    private val trackMapCode = MutableLiveData<String>()
    private val _trackMapCreation = MutableLiveData<Event<Boolean>>()
    val trackMapCreation: LiveData<Event<Boolean>>
        get() = _trackMapCreation

    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

    fun getTrackMapCode(): LiveData<String> {
        if (trackMapCode.value == null) {
            generateRandomCode()
        }
        return trackMapCode
    }

    private var attempts = 3
    private fun generateRandomCode() {
        val part1 = Random().nextInt(999)
        val part2 = Random().nextInt(999)
        val codeAttempt = String.format("%03d-%03d", part1, part2)

        launch(Dispatchers.Main) {
            Log.d("***", "Checking whether already exits TrackMap code: $codeAttempt")
            val trackMapCodeAlreadyExists =
                saveTrackMapUseCase.checkIfTrackMapCodeAlreadyExists(codeAttempt)
            if (trackMapCodeAlreadyExists) {
                Log.d("***", "Oh no! Already exits :( (Trying next attempt)")
                if (--attempts > 0) {
                    generateRandomCode()
                }
            } else {
                Log.d("***", "Perfect! TrackMap code available!")
                trackMapCode.value = codeAttempt
            }
        }
    }

    fun createTrackMap(trackMapId: String, name: String, description: String) {
        val ownerId = userHandler.getUserId()
        val ownerName = userHandler.getUserNicknameOrFullName()

        val trackMap = TrackMap(
            trackMapId,
            ownerId,
            ownerName,
            name,
            description,
            System.currentTimeMillis(),
            listOf(ownerId),
            emptyList(),
            null,
        )
        launch(Dispatchers.Main) {
            saveTrackMapUseCase.execute(trackMapId, trackMap)
            saveUserTrackMapUseCase.execute(ownerId, trackMapId, trackMap)
            _trackMapCreation.value = Event(true)
            TrackEvent.CreatedTrackMap.track()
        }
    }
}

class CreateViewModelFactory(
    private val saveTrackMapUseCase: SaveTrackMapUseCase,
    private val saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
    private val userHandler: UserHandler
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        CreateViewModel(
            saveTrackMapUseCase,
            saveUserTrackMapUseCase,
            userHandler
        ) as T
}
