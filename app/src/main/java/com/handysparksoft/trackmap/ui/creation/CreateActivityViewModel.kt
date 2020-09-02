package com.handysparksoft.trackmap.ui.creation

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.ui.common.Scope
import com.handysparksoft.usecases.SaveTrackMapUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateActivityViewModel(private val saveTrackMapUseCase: SaveTrackMapUseCase) :
    ViewModel(),
    Scope by Scope.Impl() {

    private val trackMapCode = MutableLiveData<String>()

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

    fun createTrackMap(code: String, name: String, description: String) {
        val trackMap = TrackMap(code, name, description, "me", true, System.currentTimeMillis())
        launch(Dispatchers.Main) {
            saveTrackMapUseCase.execute(code, trackMap)
        }
    }
}

class CreateActivityViewModelFactory(private val saveTrackMapUseCase: SaveTrackMapUseCase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(saveTrackMapUseCase::class.java).newInstance(saveTrackMapUseCase)
}
