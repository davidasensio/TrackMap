package com.handysparksoft.trackmap.di

import com.handysparksoft.trackmap.ui.creation.CreateViewModel
import com.handysparksoft.trackmap.ui.currenttrackmaps.CurrentTrackMapsViewModel
import com.handysparksoft.trackmap.ui.main.MainViewModel
import com.handysparksoft.usecases.GetTrackMapsUseCase
import com.handysparksoft.usecases.SaveTrackMapUseCase
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {

    @Provides
    fun mainViewModelProvider() = MainViewModel()

    @Provides
    fun createViewModelProvider(saveTrackMapUseCase: SaveTrackMapUseCase) =
        CreateViewModel(saveTrackMapUseCase)

    @Provides
    fun currentViewModelProvider(getTrackMapsUseCase: GetTrackMapsUseCase) =
        CurrentTrackMapsViewModel(getTrackMapsUseCase)
}
