package com.handysparksoft.trackmap.features.trackmap

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.handysparksoft.trackmap.core.platform.Scope

class MainViewModel : ViewModel(), Scope by Scope.Impl() {
    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }
}
