package com.handysparksoft.trackmap.features.entries

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.UserBatteryLevel
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.usecases.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val getTrackMapsUseCase: GetTrackMapsUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val leaveTrackMapUseCase: LeaveTrackMapUseCase,
    private val favoriteTrackMapUseCase: FavoriteTrackMapUseCase,
    private val updateUserBatteryLevelUseCase: UpdateUserBatteryLevelUseCase,
    private val userHandler: UserHandler,
    private val prefs: Prefs
) : ViewModel(), Scope by Scope.Impl() {

    sealed class UiModel {
        object Loading : UiModel()
        class Content(val data: List<TrackMap>) : UiModel()
        class Error(val isNetworkError: Boolean, val message: String) : UiModel()
    }

    private val _model = MutableLiveData<UiModel>()
    val model: MutableLiveData<UiModel>
        get() {
            if (_model.value == null) {
                refresh()
            }
            return _model
        }

    private val _goEvent = MutableLiveData<Event<TrackMap>>()
    val goEvent: LiveData<Event<TrackMap>>
        get() = _goEvent

    private val _leaveEvent = MutableLiveData<Event<TrackMap>>()
    val leaveEvent: LiveData<Event<TrackMap>>
        get() = _leaveEvent

    private val _shareEvent = MutableLiveData<Event<TrackMap>>()
    val shareEvent: LiveData<Event<TrackMap>>
        get() = _shareEvent

    private var currentTrackMaps = mutableListOf<TrackMap>()

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

            val userTrackMaps = getTrackMapsUseCase.execute(userId)
            if (userTrackMaps is Result.Success) {
                currentTrackMaps = ArrayList(userTrackMaps.data.values)
                sortByName()
            } else if (userTrackMaps is Result.Error) {
                _model.value =
                    UiModel.Error(userTrackMaps.isNetworkError, "Code: ${userTrackMaps.code}")
            }
        }
    }

    fun onGoTrackMapClicked(trackMap: TrackMap) {
        _goEvent.value = Event(trackMap)
    }

    fun onLeaveTrackMapClicked(trackMap: TrackMap) {
        _leaveEvent.value = Event(trackMap)
    }

    fun onShareTrackMapClicked(trackMap: TrackMap) {
        _shareEvent.value = Event(trackMap)
    }

    fun onFavoriteTrackMapClicked(trackMap: TrackMap, markAsFavorite: Boolean) {
        launch(Dispatchers.Main) {
            favoriteTrackMapUseCase.execute(
                userHandler.getUserId(),
                trackMap.trackMapId,
                markAsFavorite
            )
        }
    }

    fun saveUser() {
        launch(Dispatchers.Main) {
            saveUserUseCase.execute(
                userHandler.getUserId(),
                prefs.userToken,
                System.currentTimeMillis()
            )
        }
    }

    fun updateUserBatteryLevel() {
        val userId = userHandler.getUserId()
        val batteryLevel = userHandler.getUserBatteryLevel()

        launch(Dispatchers.Main) {
            updateUserBatteryLevelUseCase.execute(
                userId,
                UserBatteryLevel(userId, batteryLevel.toLong())
            )
        }
    }

    fun leave(trackMap: TrackMap) {
        val userId = userHandler.getUserId()
        launch(Dispatchers.Main) {
            leaveTrackMapUseCase.execute(userId, trackMap.trackMapId)

            refresh()
        }
    }

    fun sortByDate() {
        _model.value = UiModel.Content(currentTrackMaps.sortedByDescending { it.creationDate }.sortedBy { it.favorite != true })
    }

    fun sortByName() {
        _model.value = UiModel.Content(currentTrackMaps.sortedBy { it.name.toLowerCase() }.sortedBy { it.favorite != true })
    }

    fun sortByParticipants() {
        _model.value =
            UiModel.Content(currentTrackMaps.sortedByDescending { it.participantIds.size }.sortedBy { it.favorite != true })
    }

    fun sortByOwned() {
        _model.value = UiModel.Content(currentTrackMaps
            .sortedByDescending { it.creationDate }
            .sortedByDescending { it.ownerId == userHandler.getUserId() }
            .sortedBy { it.favorite != true }
        )
    }

    fun sortByLiveTracking() {
        _model.value = UiModel.Content(currentTrackMaps
            .sortedByDescending { it.creationDate }
            .sortedBy { it.favorite != true }
            .sortedByDescending { it.liveParticipantIds?.contains(userHandler.getUserId()) }
        )
    }
}

class MainViewModelFactory(
    private val getTrackMapsUseCase: GetTrackMapsUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val leaveTrackMapUseCase: LeaveTrackMapUseCase,
    private val favoriteTrackMapUseCase: FavoriteTrackMapUseCase,
    private val updateUserBatteryLevelUseCase: UpdateUserBatteryLevelUseCase,
    private val userHandler: UserHandler,
    private val prefs: Prefs
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            getTrackMapsUseCase::class.java,
            saveUserUseCase::class.java,
            leaveTrackMapUseCase::class.java,
            favoriteTrackMapUseCase::class.java,
            updateUserBatteryLevelUseCase::class.java,
            userHandler::class.java,
            prefs::class.java
        ).newInstance(
            getTrackMapsUseCase,
            saveUserUseCase,
            leaveTrackMapUseCase,
            favoriteTrackMapUseCase,
            updateUserBatteryLevelUseCase,
            userHandler,
            prefs
        )
    }
}
