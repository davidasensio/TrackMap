package com.handysparksoft.trackmap.features.entries

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.usecases.GetTrackMapsUseCase
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.core.platform.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CurrentTrackMapsViewModel(private val getTrackMapsUseCase: GetTrackMapsUseCase) : ViewModel(),
    Scope by Scope.Impl() {

    sealed class UiModel {
        object Loading : UiModel()
        class Content(val data: List<TrackMap>) : UiModel()
    }

    private val _model = MutableLiveData<UiModel>()
    val model: MutableLiveData<UiModel>
        get() {
            if (_model.value == null) {
                refresh()
            }
            return _model
        }

    private val _navigation = MutableLiveData<Event<TrackMap>>()
    val navigation: LiveData<Event<TrackMap>>
        get() = _navigation

    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

    private fun refresh() {
        launch(Dispatchers.Main) {
            _model.value = UiModel.Loading
            _model.value = UiModel.Content(ArrayList(getTrackMapsUseCase.execute().values))
        }
    }

    fun onCurrentTrackMapClicked(trackMap: TrackMap) {
        _navigation.value = Event(trackMap)
    }
}

class CurrentTrackMapsViewModelFactory(private val getTrackMapsUseCase: GetTrackMapsUseCase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(getTrackMapsUseCase::class.java)
            .newInstance(getTrackMapsUseCase)
    }
}
