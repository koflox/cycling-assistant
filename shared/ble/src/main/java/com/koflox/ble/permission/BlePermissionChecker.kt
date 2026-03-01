package com.koflox.ble.permission

interface BlePermissionChecker {
    fun requiredPermissions(): List<String>
    fun hasPermissions(): Boolean
}
