package com.example.drivenmap.feat_map.di

import com.example.drivenmap.feat_map.data.repositories.MapRepositoryImpl
import com.example.drivenmap.feat_map.domain.repositories.MapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsMapRepository(mapRepoImpl:MapRepositoryImpl):MapRepository
}