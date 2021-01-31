package com.handysparksoft.trackmap.features.entries

import android.content.Context
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.UserBatteryLevel
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.features.notification.PushNotificationHandler
import com.handysparksoft.usecases.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EntriesViewModel(
    private val getTrackMapsUseCase: GetTrackMapsUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val leaveTrackMapUseCase: LeaveTrackMapUseCase,
    private val stopLiveTrackingUseCase: StopLiveTrackingUseCase,
    private val favoriteTrackMapUseCase: FavoriteTrackMapUseCase,
    private val updateUserBatteryLevelUseCase: UpdateUserBatteryLevelUseCase,
    private val userHandler: UserHandler,
    private val prefs: Prefs,
    private val getTrackMapByIdUseCase: GetTrackMapByIdUseCase,
    private val pushNotificationHandler: PushNotificationHandler,
    private val locationForegroundServiceHandler: LocationForegroundServiceHandler
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

    private val _pingEvent = MutableLiveData<Event<TrackMap>>()
    val pingEvent: LiveData<Event<TrackMap>>
        get() = _pingEvent

    private val _pingKOEvent = MutableLiveData<Event<TrackMap>>()
    val pingKOEvent: LiveData<Event<TrackMap>>
        get() = _pingKOEvent

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
                sortByLiveTracking()
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

    fun onPingParticipantsClicked(trackMap: TrackMap) {
        val userId = userHandler.getUserId()
        launch(Dispatchers.Main) {
            getTrackMapByIdUseCase.execute(trackMap.trackMapId)?.let {
                val trackMapTrackingActive =
                    locationForegroundServiceHandler.hasLiveTrackingAlreadyStarted(it.trackMapId)
                if (trackMapTrackingActive && isUserLiveTrackingActive(userId, it)) {
                    _pingEvent.value = Event(it)
                } else {
                    _pingKOEvent.value = Event(it)
                }
            }
        }
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

            // Besides leave TrackMap also stop tracking if necessary
            stopLiveTrackingUseCase.execute(userId, trackMap.trackMapId)

            refresh()
        }
    }

    fun pingParticipants(context: Context, trackMap: TrackMap) {
        val userId = userHandler.getUserId()
        pushNotificationHandler.pingParticipants(context, userId, trackMap)
    }

    private fun isUserLiveTrackingActive(userId: String, trackMap: TrackMap): Boolean {
        return trackMap.liveParticipantIds?.contains(userId) == true
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

class EntriesViewModelFactory(
    private val getTrackMapsUseCase: GetTrackMapsUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val leaveTrackMapUseCase: LeaveTrackMapUseCase,
    private val stopLiveTrackingUseCase: StopLiveTrackingUseCase,
    private val favoriteTrackMapUseCase: FavoriteTrackMapUseCase,
    private val updateUserBatteryLevelUseCase: UpdateUserBatteryLevelUseCase,
    private val userHandler: UserHandler,
    private val prefs: Prefs,
    private val getTrackMapByIdUseCase: GetTrackMapByIdUseCase,
    private val pushNotificationHandler: PushNotificationHandler,
    private val locationForegroundServiceHandler: LocationForegroundServiceHandler
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EntriesViewModel(
            getTrackMapsUseCase,
            saveUserUseCase,
            leaveTrackMapUseCase,
            stopLiveTrackingUseCase,
            favoriteTrackMapUseCase,
            updateUserBatteryLevelUseCase,
            userHandler,
            prefs,
            getTrackMapByIdUseCase,
            pushNotificationHandler,
            locationForegroundServiceHandler
        ) as T
    }
}
