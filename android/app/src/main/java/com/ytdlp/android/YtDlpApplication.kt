package com.ytdlp.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.ContextCompat

class YtDlpApplication : Application() {

    companion object {
        const val DOWNLOAD_CHANNEL_ID = "download_channel"
        const val DOWNLOAD_CHANNEL_NAME = "Download Notifications"
        lateinit var instance: YtDlpApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        
        // Initialize Python environment for yt-dlp
        initializePython()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                DOWNLOAD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for video downloads"
                setShowBadge(false)
            }

            val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(downloadChannel)
        }
    }

    private fun initializePython() {
        try {
            // Initialize Python runtime for yt-dlp functionality
            // This would be implemented with Chaquopy or similar Python integration
            // For now, we'll use a placeholder
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}