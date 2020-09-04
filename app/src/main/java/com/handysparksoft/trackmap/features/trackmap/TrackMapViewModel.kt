package com.handysparksoft.trackmap.features.trackmap

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler


class TrackMapViewModel(private val userHandler: UserHandler) : ViewModel(),
    Scope by Scope.Impl() {

    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }
}

class TrackMapViewModelFactory(private val userHandler: UserHandler) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(userHandler::class.java).newInstance(userHandler)
    }
}
