package com.handysparksoft.trackmap.core.di

import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.CurrentTrackMapsViewModelFactory
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
