package com.handysparksoft.trackmap.features.profile

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.UserProfileData
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.usecases.GetUserProfileDataUseCase
import com.handysparksoft.usecases.UpdateUserProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfileDataUseCase: GetUserProfileDataUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val userHandler: UserHandler,
    private val prefs: Prefs
) : ViewModel(), Scope by Scope.Impl() {

    sealed class UiModel {
        object Loading : UiModel()
        class Content(val data: UserProfileData) : UiModel()
        class Error(val isNetworkError: Boolean, val message: String) : UiModel()
    }

    private val _model = MutableLiveData<UiModel>()
    val model: LiveData<UiModel>
        get() = _model

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
        /*if (prefs.userProfileData != null) {
            _model.value = UiModel.Content(prefs.userProfileData!!)
        } else {*/
            launch(Dispatchers.Main) {
                val userProfileDataResult = getUserProfileDataUseCase.execute(getUserId())
                if (userProfileDataResult is Result.Success) {
                    _model.value = UiModel.Content(userProfileDataResult.data)
                    prefs.userProfileData = userProfileDataResult.data
                } else if (userProfileDataResult is Result.Error) {
                    _model.value = UiModel.Error(
                        userProfileDataResult.isNetworkError,
                        "Code: ${userProfileDataResult.code}"
                    )
                }
            }
        /*}*/
    }

    fun saveUserProfile(nickname: String, fullName: String, phone: String, profileImage: Bitmap?) {
        val userId = getUserId()

        launch(Dispatchers.Main) {
            _model.value = UiModel.Loading
            val encodedImage = getEncodedImage(profileImage)
            val userProfileData = UserProfileData(userId, nickname, fullName, phone, encodedImage)
            updateUserProfileUseCase.execute(userId, userProfileData)
            _saveProfileDataEvent.value = Event(true)

            prefs.userProfileData = userProfileData
            profileDataUpdated = true
        }
    }

    private fun getEncodedImage(profileImage: Bitmap?): String? {
        return profileImage?.let { bitmap ->
            val quality = getBalancedQuality(bitmap.byteCount / 1024)
            Base64Utils.encodeImage(bitmap, quality)
        }
    }

    private fun getUserId(): String {
        return userHandler.getUserId()
    }

    private fun getBalancedQuality(kbytesCount: Int): Int {
        return when {
            kbytesCount > 40000 -> 1
            kbytesCount > 30000 -> 2
            kbytesCount > 20000 -> 5
            kbytesCount > 15000 -> 10
            kbytesCount > 10000 -> 20
            kbytesCount > 7500 -> 30
            else -> 40
        }
    }

    companion object {
        var profileDataUpdated = false
    }
}

class ProfileViewModelFactory(
    private val getUserProfileDataUseCase: GetUserProfileDataUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val userHandler: UserHandler,
    private val prefs: Prefs
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.getConstructor(
            getUserProfileDataUseCase::class.java,
            updateUserProfileUseCase::class.java,
            userHandler::class.java,
            prefs::class.java,
        )
            .newInstance(getUserProfileDataUseCase, updateUserProfileUseCase, userHandler, prefs)
}
