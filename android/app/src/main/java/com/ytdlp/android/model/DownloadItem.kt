package com.ytdlp.android.model

data class DownloadItem(
    val id: Long,
    val url: String,
    var title: String,
    var status: Status,
    var progress: Int,
    var filePath: String? = null,
    var fileSize: Long = 0,
    var downloadedSize: Long = 0,
    var errorMessage: String? = null,
    var thumbnailUrl: String? = null,
    var duration: String? = null,
    var quality: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Status {
        PENDING,
        DOWNLOADING,
        COMPLETED,
        ERROR,
        PAUSED,
        CANCELLED
    }

    fun getProgressPercentage(): String {
        return "$progress%"
    }

    fun getFormattedFileSize(): String {
        return if (fileSize > 0) {
            formatBytes(fileSize)
        } else {
            "Unknown size"
        }
    }

    fun getFormattedDownloadedSize(): String {
        return formatBytes(downloadedSize)
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }

    fun getStatusText(): String {
        return when (status) {
            Status.PENDING -> "Pending"
            Status.DOWNLOADING -> "Downloading"
            Status.COMPLETED -> "Completed"
            Status.ERROR -> "Error"
            Status.PAUSED -> "Paused"
            Status.CANCELLED -> "Cancelled"
        }
    }
}