package com.ytdlp.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ytdlp.android.adapter.DownloadAdapter
import com.ytdlp.android.databinding.ActivityMainBinding
import com.ytdlp.android.model.DownloadItem
import com.ytdlp.android.service.DownloadService
import com.ytdlp.android.utils.PermissionUtils
import com.ytdlp.android.utils.YtDlpWrapper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var downloadAdapter: DownloadAdapter
    private val downloads = mutableListOf<DownloadItem>()
    private lateinit var ytDlpWrapper: YtDlpWrapper

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeApp()
        } else {
            Toast.makeText(this, "Storage permissions are required", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissions()
        handleIncomingIntent(intent)
    }

    private fun setupUI() {
        // Setup RecyclerView
        downloadAdapter = DownloadAdapter(downloads) { downloadItem ->
            // Handle download item click
            onDownloadItemClick(downloadItem)
        }
        
        binding.recyclerViewDownloads.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = downloadAdapter
        }

        // Setup download button
        binding.buttonDownload.setOnClickListener {
            val url = binding.editTextUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                startDownload(url)
            } else {
                Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup paste button
        binding.buttonPaste.setOnClickListener {
            // TODO: Implement paste from clipboard
            pasteFromClipboard()
        }
    }

    private fun checkPermissions() {
        val permissions = PermissionUtils.getRequiredPermissions()
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            storagePermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            initializeApp()
        }
    }

    private fun initializeApp() {
        ytDlpWrapper = YtDlpWrapper(this)
        Toast.makeText(this, "YT-DLP Android Ready", Toast.LENGTH_SHORT).show()
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedUrl.isNullOrEmpty()) {
                binding.editTextUrl.setText(sharedUrl)
            }
        }
    }

    private fun startDownload(url: String) {
        try {
            val downloadItem = DownloadItem(
                id = System.currentTimeMillis(),
                url = url,
                title = "Loading...",
                status = DownloadItem.Status.PENDING,
                progress = 0
            )
            
            downloads.add(0, downloadItem)
            downloadAdapter.notifyItemInserted(0)
            binding.recyclerViewDownloads.scrollToPosition(0)

            // Start download service
            val serviceIntent = Intent(this, DownloadService::class.java).apply {
                putExtra("download_id", downloadItem.id)
                putExtra("url", url)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            binding.editTextUrl.text?.clear()
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun onDownloadItemClick(downloadItem: DownloadItem) {
        when (downloadItem.status) {
            DownloadItem.Status.COMPLETED -> {
                // Open downloaded file
                openDownloadedFile(downloadItem)
            }
            DownloadItem.Status.ERROR -> {
                // Retry download
                retryDownload(downloadItem)
            }
            else -> {
                // Show download details
                showDownloadDetails(downloadItem)
            }
        }
    }

    private fun pasteFromClipboard() {
        // TODO: Implement clipboard paste functionality
        Toast.makeText(this, "Paste from clipboard - TODO", Toast.LENGTH_SHORT).show()
    }

    private fun openDownloadedFile(downloadItem: DownloadItem) {
        // TODO: Implement file opening
        Toast.makeText(this, "Open file - TODO", Toast.LENGTH_SHORT).show()
    }

    private fun retryDownload(downloadItem: DownloadItem) {
        downloadItem.status = DownloadItem.Status.PENDING
        downloadItem.progress = 0
        downloadAdapter.notifyDataSetChanged()
        startDownload(downloadItem.url)
    }

    private fun showDownloadDetails(downloadItem: DownloadItem) {
        // TODO: Show download details dialog
        Toast.makeText(this, "Download details - TODO", Toast.LENGTH_SHORT).show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }
}