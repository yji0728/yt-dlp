package com.ytdlp.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ytdlp.android.R
import com.ytdlp.android.databinding.ItemDownloadBinding
import com.ytdlp.android.model.DownloadItem

class DownloadAdapter(
    private val downloads: List<DownloadItem>,
    private val onItemClick: (DownloadItem) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    inner class DownloadViewHolder(
        private val binding: ItemDownloadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(downloadItem: DownloadItem) {
            binding.apply {
                textViewTitle.text = downloadItem.title
                textViewUrl.text = downloadItem.url
                textViewStatus.text = downloadItem.getStatusText()
                textViewProgress.text = downloadItem.getProgressPercentage()
                textViewFileSize.text = downloadItem.getFormattedFileSize()
                
                progressBarDownload.progress = downloadItem.progress
                
                // Set status color
                val statusColor = when (downloadItem.status) {
                    DownloadItem.Status.PENDING -> R.color.status_pending
                    DownloadItem.Status.DOWNLOADING -> R.color.status_downloading
                    DownloadItem.Status.COMPLETED -> R.color.status_completed
                    DownloadItem.Status.ERROR -> R.color.status_error
                    DownloadItem.Status.PAUSED -> R.color.status_paused
                    DownloadItem.Status.CANCELLED -> R.color.status_cancelled
                }
                
                textViewStatus.setTextColor(
                    ContextCompat.getColor(itemView.context, statusColor)
                )
                
                // Show/hide progress bar based on status
                progressBarDownload.visibility = if (
                    downloadItem.status == DownloadItem.Status.DOWNLOADING ||
                    downloadItem.status == DownloadItem.Status.PENDING
                ) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // Set error message if available
                if (downloadItem.status == DownloadItem.Status.ERROR && 
                    !downloadItem.errorMessage.isNullOrEmpty()) {
                    textViewUrl.text = downloadItem.errorMessage
                }
                
                root.setOnClickListener {
                    onItemClick(downloadItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val binding = ItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(downloads[position])
    }

    override fun getItemCount(): Int = downloads.size
}