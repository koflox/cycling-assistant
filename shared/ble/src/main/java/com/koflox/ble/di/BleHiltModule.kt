package com.koflox.ble.di

import android.content.Context
import com.koflox.ble.connection.BleGattManager
import com.koflox.ble.connection.BleGattManagerImpl
import com.koflox.ble.permission.BlePermissionChecker
import com.koflox.ble.permission.BlePermissionCheckerImpl
import com.koflox.ble.scanner.BleScanner
import com.koflox.ble.scanner.BleScannerImpl
import com.koflox.ble.state.BluetoothStateMonitor
import com.koflox.ble.state.BluetoothStateMonitorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object BleHiltModule {

    @Provides
    @Singleton
    fun provideBlePermissionChecker(
        @ApplicationContext context: Context,
    ): BlePermissionChecker = BlePermissionCheckerImpl(context = context)

    @Provides
    @Singleton
    fun provideBleScanner(
        @ApplicationContext context: Context,
    ): BleScanner = BleScannerImpl(context = context)

    @Provides
    fun provideBleGattManager(
        @ApplicationContext context: Context,
    ): BleGattManager = BleGattManagerImpl(context = context)

    @Provides
    @Singleton
    fun provideBluetoothStateMonitor(
        @ApplicationContext context: Context,
    ): BluetoothStateMonitor = BluetoothStateMonitorImpl(context = context)
}
