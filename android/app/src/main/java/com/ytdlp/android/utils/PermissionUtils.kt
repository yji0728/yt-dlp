package com.ytdlp.android.utils

import android.Manifest
import android.os.Build

object PermissionUtils {
    
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        // Always required
        permissions.add(Manifest.permission.INTERNET)
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
        permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        permissions.add(Manifest.permission.WAKE_LOCK)
        
        // Storage permissions based on API level
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ - Use scoped storage
                // No special permissions needed for app-specific storage
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+
                permissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
            else -> {
                // Below Android 11
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        return permissions
    }
    
    fun getStoragePermissions(): List<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                emptyList() // Use scoped storage
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
            else -> {
                listOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }
}