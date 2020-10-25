package com.handysparksoft.trackmap.core.platform.viewbinding

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class FragmentViewBindingHolder<Binding : androidx.viewbinding.ViewBinding> : LifecycleObserver {
    private var _binding: Binding? = null
    val binding: Binding get() = _binding ?: throw IllegalStateException("Binding not defined")

    fun createBinding(lifecycleOwner: Fragment, blockInstantiation: () -> Binding) {
        lifecycleOwner.lifecycle.addObserver(this)
        _binding = blockInstantiation()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onViewDestroyed() {
        _binding = null
    }
}
