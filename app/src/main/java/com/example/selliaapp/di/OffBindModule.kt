package com.example.selliaapp.di


import com.example.selliaapp.data.remote.off.OpenFoodFactsRepository
import com.example.selliaapp.repository.OffRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [NUEVO] Enlaza la interfaz OffRepository con su implementación.
 * IMPORTANTE: Las referencias de tipo (paquetes) deben coincidir EXACTAMENTE.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OffBindModule {

    @Binds
    @Singleton
    abstract fun bindOffRepository(impl: OpenFoodFactsRepository): OffRepository
}
