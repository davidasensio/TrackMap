package com.handysparksoft.trackmap.features.main

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.handysparksoft.trackmap.core.platform.Scope

class MainViewModel() : ViewModel(), Scope by Scope.Impl() {
    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }
}
