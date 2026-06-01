package com.neppolian3.linkoptima.di

import com.neppolian3.linkoptima.repository.AuthRepository
import com.neppolian3.linkoptima.repository.AuthRepositoryImpl
import com.neppolian3.linkoptima.repository.ProfileReviewRepository
import com.neppolian3.linkoptima.repository.ProfileReviewRepositoryImpl
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindProfileReviewRepository(impl: ProfileReviewRepositoryImpl): ProfileReviewRepository
}