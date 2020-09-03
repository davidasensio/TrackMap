package com.handysparksoft.trackmap.di

import com.handysparksoft.trackmap.ui.creation.CreateViewModelFactory
import com.handysparksoft.trackmap.ui.currenttrackmaps.CurrentTrackMapsViewModelFactory
import com.handysparksoft.usecases.GetTrackMapsUseCase
import com.handysparksoft.usecases.SaveTrackMapUseCase
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {

    @Provides
    fun createViewModelFactoryProvider(saveTrackMapUseCase: SaveTrackMapUseCase) =
        CreateViewModelFactory(saveTrackMapUseCase)

    @Provides
    fun currentViewModelFactoryProvider(getTrackMapsUseCase: GetTrackMapsUseCase) =
        CurrentTrackMapsViewModelFactory(getTrackMapsUseCase)
}
