package com.neuropulse.data.di

import com.neuropulse.data.auth.FirebaseAuthRepositoryImpl
import com.neuropulse.data.local.UserPreferencesDataStoreImpl
import com.neuropulse.data.network.ConnectivityNetworkMonitor
import com.neuropulse.domain.repository.AuthRepository
import com.neuropulse.domain.repository.NetworkMonitor
import com.neuropulse.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DataModule — Hilt bindings for Phase 1 data layer implementations.
 *
 * Abstract module using [@Binds] (preferred over [@Provides] for interface bindings —
 * generates less bytecode and avoids an intermediate factory class).
 *
 * Both bindings are [@Singleton] because:
 * - [UserPreferencesDataStoreImpl] wraps a DataStore instance that must be singular
 * - [FirebaseAuthRepositoryImpl] wraps FirebaseAuth.getInstance() which is already a singleton
 *
 * Extension point: when adding a new auth provider (e.g. Apple Sign-In),
 * do NOT change this binding — instead add a new method to [AuthRepository] and
 * implement it in [FirebaseAuthRepositoryImpl]. See extension-points.md §6.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesDataStoreImpl,
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: ConnectivityNetworkMonitor,
    ): NetworkMonitor
}
