package com.handysparksoft.trackmap.ui.main

import com.handysparksoft.trackmap.ui.common.Scope

class MainPresenter : Scope by Scope.Impl() {

    interface View {
    }

    private var view: View? = null

    fun onCreate(view: View) {
        initScope()
        this.view = view
    }

    fun onDestroy() {
        cancelScope()
        this.view = null
    }
}
