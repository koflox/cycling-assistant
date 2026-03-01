package com.koflox.ble.di

import com.koflox.ble.connection.BleGattManager
import com.koflox.ble.connection.BleGattManagerImpl
import com.koflox.ble.permission.BlePermissionChecker
import com.koflox.ble.permission.BlePermissionCheckerImpl
import com.koflox.ble.scanner.BleScanner
import com.koflox.ble.scanner.BleScannerImpl
import com.koflox.ble.state.BluetoothStateMonitor
import com.koflox.ble.state.BluetoothStateMonitorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val bleModule = module {
    single<BlePermissionChecker> {
        BlePermissionCheckerImpl(
            context = androidContext()
        )
    }
    single<BleScanner> {
        BleScannerImpl(
            context = androidContext()
        )
    }
    factory<BleGattManager> {
        BleGattManagerImpl(
            context = androidContext()
        )
    }
    single<BluetoothStateMonitor> {
        BluetoothStateMonitorImpl(
            context = androidContext()
        )
    }
}
