package com.handysparksoft.trackmap.features.entries

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.usecases.GetTrackMapsUseCase
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.usecases.JoinTrackMapUseCase
import com.handysparksoft.usecases.SaveUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val getTrackMapsUseCase: GetTrackMapsUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val userHandler: UserHandler
) : ViewModel(),
    Scope by Scope.Impl() {

    sealed class UiModel {
        object Loading : UiModel()
        class Content(val data: List<TrackMap>) : UiModel()
    }

    private val _model = MutableLiveData<UiModel>()
    val model: MutableLiveData<UiModel>
        get() {
            if (_model.value == null) {
                refresh()
            }
            return _model
        }

    private val _navigation = MutableLiveData<Event<TrackMap>>()
    val navigation: LiveData<Event<TrackMap>>
        get() = _navigation

    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

    fun refresh() {
        launch(Dispatchers.Main) {
            val userId = userHandler.getUserId()
            _model.value = UiModel.Loading
            _model.value = UiModel.Content(ArrayList(getTrackMapsUseCase.execute(userId).values))
        }
    }

    fun onCurrentTrackMapClicked(trackMap: TrackMap) {
        _navigation.value = Event(trackMap)
    }

    fun saveUser() {
        launch(Dispatchers.Main) {
            saveUserUseCase.execute(userHandler.getUserId(), "default")
        }
    }

    fun joinTrackMap(trackMapCode: String) {
        launch(Dispatchers.Main) {
            joinTrackMapUseCase.execute(userHandler.getUserId(), trackMapCode)
            refresh()
        }
    }
}

class MainViewModelFactory(
    private val getTrackMapsUseCase: GetTrackMapsUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val userHandler: UserHandler
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            getTrackMapsUseCase::class.java,
            saveUserUseCase::class.java,
            joinTrackMapUseCase::class.java,
            userHandler::class.java
        ).newInstance(
            getTrackMapsUseCase,
            saveUserUseCase,
            joinTrackMapUseCase,
            userHandler
        )
    }
}
