package com.koflox.ble.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.koflox.ble.model.BleDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("MissingPermission")
internal class BleScannerImpl(
    private val context: Context,
) : BleScanner {

    override fun scan(serviceUuids: List<UUID>, timeoutMs: Long): Flow<BleDevice> = callbackFlow {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val scanner = bluetoothManager.adapter?.bluetoothLeScanner
        if (scanner == null) {
            close()
            return@callbackFlow
        }
        val seenAddresses = mutableSetOf<String>()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return
                val address = device.address ?: return
                val name = device.name ?: result.scanRecord?.deviceName
                val uuids = result.scanRecord?.serviceUuids?.map { it.uuid }
                if (name != null && !uuids.isNullOrEmpty() && seenAddresses.add(address)) {
                    trySend(BleDevice(address = address, name = name, serviceUuids = uuids))
                }
            }
        }
        val filters = serviceUuids.map { uuid ->
            ScanFilter.Builder().setServiceUuid(ParcelUuid(uuid)).build()
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()
        scanner.startScan(filters, settings, callback)
        launch {
            delay(timeoutMs)
            close()
        }
        awaitClose {
            scanner.stopScan(callback)
        }
    }
}
