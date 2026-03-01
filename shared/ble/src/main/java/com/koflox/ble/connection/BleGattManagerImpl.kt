package com.koflox.ble.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import com.koflox.ble.model.BleConnectionState
import com.koflox.ble.model.BleGattEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

@SuppressLint("MissingPermission")
internal class BleGattManagerImpl(
    private val context: Context,
) : BleGattManager {

    companion object {
        private val CLIENT_CHARACTERISTIC_CONFIG: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    private var bluetoothGatt: BluetoothGatt? = null

    override fun connect(address: String): Flow<BleGattEvent> = callbackFlow {
        _connectionState.value = BleConnectionState.CONNECTING
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val device = bluetoothManager.adapter?.getRemoteDevice(address)
        if (device == null) {
            _connectionState.value = BleConnectionState.DISCONNECTED
            close()
            return@callbackFlow
        }
        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        _connectionState.value = BleConnectionState.CONNECTED
                        trySend(BleGattEvent.ConnectionStateChanged(BleConnectionState.CONNECTED))
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        _connectionState.value = BleConnectionState.DISCONNECTED
                        trySend(BleGattEvent.ConnectionStateChanged(BleConnectionState.DISCONNECTED))
                        close()
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    trySend(BleGattEvent.ServicesDiscovered)
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
            ) {
                val service = characteristic.service ?: return
                trySend(
                    BleGattEvent.CharacteristicChanged(
                        serviceUuid = service.uuid,
                        characteristicUuid = characteristic.uuid,
                        data = value,
                    ),
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    @Suppress("DEPRECATION")
                    val value = characteristic.value ?: return
                    val service = characteristic.service ?: return
                    trySend(
                        BleGattEvent.CharacteristicChanged(
                            serviceUuid = service.uuid,
                            characteristicUuid = characteristic.uuid,
                            data = value,
                        ),
                    )
                }
            }
        }
        bluetoothGatt = device.connectGatt(context, false, callback)
        awaitClose {
            bluetoothGatt?.close()
            bluetoothGatt = null
            _connectionState.value = BleConnectionState.DISCONNECTED
        }
    }

    override fun disconnect() {
        _connectionState.value = BleConnectionState.DISCONNECTING
        bluetoothGatt?.disconnect()
    }

    override fun observeConnectionState(): StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    override fun enableNotifications(serviceUuid: UUID, characteristicUuid: UUID) {
        val characteristic = bluetoothGatt
            ?.getService(serviceUuid)
            ?.getCharacteristic(characteristicUuid) ?: return
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt?.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            bluetoothGatt?.writeDescriptor(descriptor)
        }
    }
}
