package com.handysparksoft.trackmap.features.task

import android.content.Context
import android.util.Log
import androidx.work.*
import com.handysparksoft.domain.model.UserBatteryLevel
import com.handysparksoft.trackmap.App
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.usecases.UpdateUserBatteryLevelUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class UpdateBatteryLevelWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var userHandler: UserHandler

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var updateUserBatteryLevelUseCase: UpdateUserBatteryLevelUseCase

    init {
        (appContext as App).component.inject(this)
    }

    override suspend fun doWork(): Result {
        val userId = userHandler.getUserId()
        val batteryLevel = userHandler.getUserBatteryLevel()

        updateUserBatteryLevelUseCase.execute(
            userId,
            UserBatteryLevel(userId, batteryLevel.toLong())
        )
        Log.d(TAG, "*** Updated user battery level")
        return Result.success()
    }

    companion object {
        private const val UNIQUE_ID = "WorkManagerUserDataTaskUniqueId"
        private val TAG = UpdateBatteryLevelWorker::class.java.simpleName

        fun initBatteryLevelPeriodicWork(context: Context) {
            val updateBatteryLevelWork = PeriodicWorkRequestBuilder<UpdateBatteryLevelWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .addTag(UNIQUE_ID)
                .build()

            val workManager = WorkManager.getInstance(context.applicationContext)

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_ID,
                ExistingPeriodicWorkPolicy.KEEP,
                updateBatteryLevelWork
            )
            workManager.getWorkInfoByIdLiveData(updateBatteryLevelWork.id).observeForever {
                // if (it != null) { Log.d("periodicWorkRequest", "Status changed to ${it.state.isFinished}") }
            }
        }
    }
}
