package com.ytdlp.android.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ytdlp.android.MainActivity
import com.ytdlp.android.R
import com.ytdlp.android.YtDlpApplication
import com.ytdlp.android.utils.YtDlpWrapper
import kotlinx.coroutines.*

class DownloadService : Service() {

    companion object {
        const val ACTION_STOP = "com.ytdlp.android.STOP_DOWNLOAD"
        private const val NOTIFICATION_ID = 1001
    }

    private var serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var ytDlpWrapper: YtDlpWrapper
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        ytDlpWrapper = YtDlpWrapper(this)
        notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopDownload()
                return START_NOT_STICKY
            }
            else -> {
                val downloadId = intent?.getLongExtra("download_id", -1) ?: -1
                val url = intent?.getStringExtra("url") ?: ""
                
                if (downloadId != -1L && url.isNotEmpty()) {
                    startDownload(downloadId, url)
                }
            }
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startDownload(downloadId: Long, url: String) {
        val notification = createNotification("Preparing download...", 0)
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                // First, get video info
                updateNotification("Getting video information...", 0)
                val videoInfo = ytDlpWrapper.extractInfo(url)
                
                if (videoInfo == null) {
                    showError("Failed to get video information")
                    return@launch
                }

                // Start download
                updateNotification("Downloading: ${videoInfo.title}", 0)
                
                val result = ytDlpWrapper.downloadVideo(url) { progress ->
                    updateNotification("Downloading: ${videoInfo.title}", progress)
                }

                when (result) {
                    is YtDlpWrapper.DownloadResult.Success -> {
                        updateNotification("Download completed: ${videoInfo.title}", 100, true)
                        // Broadcast download completion
                        broadcastDownloadComplete(downloadId, result.filePath)
                    }
                    is YtDlpWrapper.DownloadResult.Error -> {
                        showError("Download failed: ${result.message}")
                        broadcastDownloadError(downloadId, result.message)
                    }
                }
            } catch (e: Exception) {
                showError("Download error: ${e.message}")
                broadcastDownloadError(downloadId, e.message ?: "Unknown error")
            } finally {
                // Stop service after a delay to show completion
                serviceScope.launch {
                    delay(3000)
                    stopSelf()
                }
            }
        }
    }

    private fun createNotification(
        content: String, 
        progress: Int, 
        completed: Boolean = false
    ): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, DownloadService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, YtDlpApplication.DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("YT-DLP Download")
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setOngoing(!completed)
            .setAutoCancel(completed)

        if (!completed && progress > 0) {
            builder.setProgress(100, progress, false)
        }

        if (!completed) {
            builder.addAction(
                R.drawable.ic_stop,
                "Stop",
                stopPendingIntent
            )
        }

        return builder.build()
    }

    private fun updateNotification(content: String, progress: Int, completed: Boolean = false) {
        val notification = createNotification(content, progress, completed)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showError(message: String) {
        updateNotification("Error: $message", 0, true)
    }

    private fun stopDownload() {
        serviceJob.cancelChildren()
        stopForeground(true)
        stopSelf()
    }

    private fun broadcastDownloadComplete(downloadId: Long, filePath: String) {
        val intent = Intent("com.ytdlp.android.DOWNLOAD_COMPLETE").apply {
            putExtra("download_id", downloadId)
            putExtra("file_path", filePath)
        }
        sendBroadcast(intent)
    }

    private fun broadcastDownloadError(downloadId: Long, error: String) {
        val intent = Intent("com.ytdlp.android.DOWNLOAD_ERROR").apply {
            putExtra("download_id", downloadId)
            putExtra("error", error)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}