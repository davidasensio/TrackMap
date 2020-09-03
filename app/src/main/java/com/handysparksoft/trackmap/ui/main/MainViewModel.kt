package com.handysparksoft.trackmap.ui.main

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.handysparksoft.trackmap.ui.common.Scope

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
