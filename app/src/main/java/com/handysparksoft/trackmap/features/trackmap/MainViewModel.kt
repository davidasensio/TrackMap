package com.handysparksoft.trackmap.features.trackmap

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.usecases.JoinTrackMapUseCase
import com.handysparksoft.usecases.SaveUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel(
    private val saveUserUseCase: SaveUserUseCase,
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val userHandler: UserHandler
) : ViewModel(),
    Scope by Scope.Impl() {

    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

    fun saveUser() {
        launch(Dispatchers.Main) {
            saveUserUseCase.execute(userHandler.getUserId(), "default")
        }
    }
}

class MainViewModelFactory(
    private val saveUserUseCase: SaveUserUseCase,
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val userHandler: UserHandler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            saveUserUseCase::class.java,
            joinTrackMapUseCase::class.java,
            userHandler::class.java
        )
            .newInstance(saveUserUseCase, joinTrackMapUseCase, userHandler)
    }

}
