package com.neppolian3.linkoptima.di

import com.neppolian3.linkoptima.service.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService = GeminiService()
}