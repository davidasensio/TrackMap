package com.handysparksoft.trackmap.features.participants

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.UserProfileData
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.usecases.GetUserProfileDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ParticipantsViewModel(
    private val getUserProfileDataUseCase: GetUserProfileDataUseCase,
    private val userHandler: UserHandler,
    private val prefs: Prefs
) : ViewModel(), Scope by Scope.Impl() {

    sealed class UiModel {
        object Loading : UiModel()
        class Content(val data: List<UserProfileData>) : UiModel()
        class Error(val isNetworkError: Boolean, val message: String) : UiModel()
    }

    private val _model = MutableLiveData<UiModel>()
    val model: MutableLiveData<UiModel>
        get() = _model

    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

    fun refresh(trackMap: TrackMap) {
        val userId = userHandler.getUserId()
        val ownerId = trackMap.ownerId
        val usersData = mutableListOf<UserProfileData>()
        val loadedUsers = mutableListOf<UserProfileData>()
        val cachedUsers = prefs.userDataProfileMapCache

        _model.value = UiModel.Loading

        // Add already cached Users
        cachedUsers.values
            .filter { trackMap.participantIds.contains(it.userId) }
            .let { alreadyCachedUsers ->
                usersData.addAll(alreadyCachedUsers)
            }

        // Add non cached Users
        launch(Dispatchers.Main) {
            val awaitAll = trackMap.participantIds
                .filter { !cachedUsers.keys.contains(it) }
                .map { async { getUserProfileDataUseCase.execute(it) } }
                .awaitAll()

            awaitAll.forEach { result ->
                if (result is Result.Success) {
                    loadedUsers.add(result.data)
                }
            }

            loadedUsers.forEach {
                cachedUsers[it.userId] = it
            }

            // Update prefs cache
            if (loadedUsers.size > 0) {
                prefs.userDataProfileMapCache = cachedUsers
                prefs.userDataProfileMapCacheLastUpdate = System.currentTimeMillis()
            }

            usersData.addAll(loadedUsers)
            model.value = UiModel.Content(sortParticipants(usersData, userId, ownerId))
        }
    }

    private fun sortParticipants(
        participants: List<UserProfileData>,
        yourUserId: String,
        trackMapOwnerId: String
    ): List<UserProfileData> {
        return participants
            .sortedBy { it.nickname?.toLowerCase() ?: it.fullName?.toLowerCase() ?: it.userId }
            .sortedByDescending { it.userId == trackMapOwnerId }
            .sortedByDescending { it.userId == yourUserId }
    }
}

class ParticipantsViewModelFactory(
    private val getUserProfileDataUseCase: GetUserProfileDataUseCase,
    private val userHandler: UserHandler,
    private val prefs: Prefs,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            getUserProfileDataUseCase::class.java,
            userHandler::class.java,
            prefs::class.java

        ).newInstance(
            getUserProfileDataUseCase,
            userHandler,
            prefs
        )
    }
}
