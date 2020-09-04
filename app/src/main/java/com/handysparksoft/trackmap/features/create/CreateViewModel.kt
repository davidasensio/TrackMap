package com.handysparksoft.trackmap.features.create

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.usecases.SaveTrackMapUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateViewModel(
    private val saveTrackMapUseCase: SaveTrackMapUseCase,
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
            trackMapCode.value = generateRandomCode()
        }
        return trackMapCode
    }

    private fun generateRandomCode(): String {
        val part1 = Random().nextInt(999)
        val part2 = Random().nextInt(999)
        return String.format("%03d-%03d", part1, part2)
    }

    fun createTrackMap(trackMapId: String, name: String, description: String) {
        val ownerId = userHandler.getUserId()

        val trackMap = TrackMap(
            trackMapId,
            ownerId,
            name,
            description,
            true,
            System.currentTimeMillis(),
            listOf(ownerId)
        )
        launch(Dispatchers.Main) {
            saveTrackMapUseCase.execute(ownerId, trackMapId, trackMap)
            _trackMapCreation.value = Event(true)
        }
    }
}

class CreateViewModelFactory(
    private val saveTrackMapUseCase: SaveTrackMapUseCase,
    private val userHandler: UserHandler
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(saveTrackMapUseCase::class.java, userHandler::class.java)
            .newInstance(saveTrackMapUseCase, userHandler)
}