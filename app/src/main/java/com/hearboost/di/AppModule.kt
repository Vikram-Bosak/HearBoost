package com.hearboost.di

import android.content.Context
import com.hearboost.audio.AudioEngine
import com.hearboost.audio.NoiseReductionEngine
import com.hearboost.audio.VolumeBooster
import com.hearboost.bluetooth.HeadphoneManager
import com.hearboost.settings.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAudioEngine(): AudioEngine = AudioEngine()

    @Provides
    @Singleton
    fun provideVolumeBooster(): VolumeBooster = VolumeBooster()

    @Provides
    @Singleton
    fun provideNoiseReductionEngine(): NoiseReductionEngine = NoiseReductionEngine()

    @Provides
    @Singleton
    fun provideHeadphoneManager(
        @ApplicationContext context: Context
    ): HeadphoneManager = HeadphoneManager(context)

    @Provides
    @Singleton
    fun provideSettingsManager(
        @ApplicationContext context: Context
    ): SettingsManager = SettingsManager(context)
}
