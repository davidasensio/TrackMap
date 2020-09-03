package com.handysparksoft.trackmap.core.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

interface Scope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var job: Job

    fun initScope() {
        job = SupervisorJob()
    }

    fun destroyScope() {
        job.cancel()
    }

    class Impl : Scope {
        override lateinit var job: Job
    }
}
