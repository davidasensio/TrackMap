package com.handysparksoft.trackmap.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.UserProfileData
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.usecases.GetUserProfileDataUseCase
import com.handysparksoft.usecases.UpdateUserProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfileDataUseCase: GetUserProfileDataUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val userHandler: UserHandler
) : ViewModel(), Scope by Scope.Impl() {

    sealed class UiModel {
        object Loading : UiModel()
        class Content(val data: UserProfileData) : UiModel()
        class Error(val isNetworkError: Boolean, val message: String) : UiModel()
    }

    private val _model = MutableLiveData<UiModel>()
    val model: LiveData<UiModel>
        get() {
            if (_model.value == null) {
                refresh()
            }
            return _model
        }

    private val _saveProfileDataEvent = MutableLiveData<Event<Boolean>>()
    val saveProfileDataEvent: LiveData<Event<Boolean>> get() = _saveProfileDataEvent

    init {
        initScope()
    }

    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

    fun refresh() {
        launch(Dispatchers.Main) {
            val userProfileDataResult = getUserProfileDataUseCase.execute(getUserId())
            if (userProfileDataResult is Result.Success) {
                _model.value = UiModel.Content(userProfileDataResult.data)
            } else if (userProfileDataResult is Result.Error) {
                _model.value = UiModel.Error(
                    userProfileDataResult.isNetworkError,
                    "Code: ${userProfileDataResult.code}"
                )
            }
        }
    }

    fun saveUserProfile(nickname: String, fullName: String, phone: String) {
        val userId = getUserId()
        val userProfileData = UserProfileData(userId, nickname, fullName, phone)

        launch(Dispatchers.Main) {
            updateUserProfileUseCase.execute(userId, userProfileData)
            _saveProfileDataEvent.value = Event(true)
        }
    }

    fun getUserId(): String {
        return userHandler.getUserId()
    }
}

class ProfileViewModelFactory(
    private val getUserProfileDataUseCase: GetUserProfileDataUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val userHandler: UserHandler
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(
            getUserProfileDataUseCase::class.java,
            updateUserProfileUseCase::class.java,
            userHandler::class.java
        )
            .newInstance(getUserProfileDataUseCase, updateUserProfileUseCase, userHandler)
}
