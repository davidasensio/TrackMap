package com.handysparksoft.trackmap.ui.creation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.handysparksoft.trackmap.data.server.TrackMapRepository
import com.handysparksoft.trackmap.domain.TrackMap
import com.handysparksoft.trackmap.ui.common.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateActivityViewModel : ViewModel(), Scope by Scope.Impl() {

    private val trackmapCode = MutableLiveData<String>()
    private val trackmaRepository = TrackMapRepository() //FIXME

    init {
        initScope()
    }

    fun getTrackmapCode(): LiveData<String> {
        if (trackmapCode.value == null) {
            trackmapCode.value = generateRandomCode()
        }
        return trackmapCode
    }

    private fun generateRandomCode(): String {
        val part1 = Random().nextInt(999)
        val part2 = Random().nextInt(999)
        return String.format("%03d-%03d", part1, part2)
    }

    fun createTrackmap(code: String, name: String, description: String) {
        val trackMap = TrackMap(code, name, description, "me", true, System.currentTimeMillis())
        launch(Dispatchers.Main) {
            trackmaRepository.putTrackMap(code, trackMap)
        }
    }
}
