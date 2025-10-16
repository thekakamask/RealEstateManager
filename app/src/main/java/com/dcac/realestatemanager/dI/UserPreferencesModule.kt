package com.dcac.realestatemanager.dI

import com.dcac.realestatemanager.data.preferences.IUserPreferencesRepository
import com.dcac.realestatemanager.data.preferences.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepository
    ): IUserPreferencesRepository
}
