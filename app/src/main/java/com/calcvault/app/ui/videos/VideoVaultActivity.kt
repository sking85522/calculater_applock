package com.calcvault.app.ui.videos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import java.io.File

class VideoVaultActivity : BaseVaultActivity() {

    private lateinit var recyclerVideos: RecyclerView
    private val hiddenVideos = mutableListOf<File>()

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                hideVideo(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_vault)

        recyclerVideos = findViewById(R.id.recycler_videos) ?: return // Null check if layout not updated yet
        recyclerVideos.layoutManager = LinearLayoutManager(this)

        loadHiddenVideos()

        findViewById<FloatingActionButton>(R.id.fab_add_video)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            pickVideoLauncher.launch(intent)
        }
    }

    private fun getStorageDirName(): String {
        val isFake = intent.getBooleanExtra("IS_FAKE_VAULT", false)
        return if (isFake) "fake_hidden_videos" else "hidden_videos"
    }

    private fun loadHiddenVideos() {
        hiddenVideos.clear()
        val dir = File(filesDir, getStorageDirName())
        if (dir.exists()) {
            dir.listFiles()?.let { hiddenVideos.addAll(it) }
        }
        recyclerVideos.adapter = VideoAdapter(hiddenVideos)
    }

    private fun hideVideo(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dir = File(filesDir, getStorageDirName())
                if (!dir.exists()) dir.mkdirs()

                val fileName = getFileName(uri) ?: "vid_${System.currentTimeMillis()}.mp4"
                val hiddenFile = File(dir, fileName)

                val masterKey = MasterKey.Builder(this@VideoVaultActivity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val encryptedFile = EncryptedFile.Builder(
                    this@VideoVaultActivity,
                    hiddenFile,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()

                contentResolver.openInputStream(uri)?.use { inputStream ->
                    encryptedFile.openFileOutput().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                try {
                    contentResolver.delete(uri, null, null)
                } catch (e: SecurityException) {
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@VideoVaultActivity, "Video Hidden Securely!", Toast.LENGTH_SHORT).show()
                    loadHiddenVideos()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@VideoVaultActivity, "Failed to hide video", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) result = result?.substring(cut + 1)
        }
        return result
    }

    inner class VideoAdapter(private val videos: List<File>) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
        inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tv_note_title)

            init {
                view.setOnLongClickListener {
                    val file = videos[adapterPosition]
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this@VideoVaultActivity)
                    builder.setTitle("Delete")
                    builder.setMessage("Move this item to Trash?")
                    builder.setPositiveButton("Yes") { _, _ ->
                        com.calcvault.app.utils.TrashManager.moveToTrash(this@VideoVaultActivity, file)
                        loadHiddenVideos()
                    }
                    builder.setNegativeButton("No", null)
                    builder.show()
                    true
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
            return VideoViewHolder(view)
        }

        override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
            holder.tvTitle.text = videos[position].name
        }

        override fun getItemCount(): Int = videos.size
    }
}
