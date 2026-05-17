package com.calcvault.app.ui.photos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File

class PhotoVaultActivity : BaseVaultActivity() {

    private lateinit var recyclerPhotos: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private val hiddenPhotos = mutableListOf<File>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                hidePhoto(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_vault)

        recyclerPhotos = findViewById(R.id.recycler_photos)
        recyclerPhotos.layoutManager = GridLayoutManager(this, 3)

        loadHiddenPhotos()

        findViewById<FloatingActionButton>(R.id.fab_add_photo).setOnClickListener {
            openGallery()
        }
    }

    private fun getStorageDirName(): String {
        val isFake = intent.getBooleanExtra("IS_FAKE_VAULT", false)
        return if (isFake) "fake_hidden_photos" else "hidden_photos"
    }

    fun loadHiddenPhotos() {
        hiddenPhotos.clear()
        val dir = File(filesDir, getStorageDirName())
        if (dir.exists()) {
            dir.listFiles()?.let { files ->
                hiddenPhotos.addAll(files)
            }
        }
        photoAdapter = PhotoAdapter(this, hiddenPhotos)
        recyclerPhotos.adapter = photoAdapter
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun hidePhoto(uri: Uri) {
        try {
            val dir = File(filesDir, getStorageDirName())
            if (!dir.exists()) dir.mkdirs()

            val fileName = getFileName(uri) ?: "hidden_img_${System.currentTimeMillis()}.jpg"
            val hiddenFile = File(dir, fileName)

            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedFile = EncryptedFile.Builder(
                this,
                hiddenFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            contentResolver.openInputStream(uri)?.use { inputStream ->
                encryptedFile.openFileOutput().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Delete original file from Gallery
            try {
                contentResolver.delete(uri, null, null)
            } catch (secEx: SecurityException) {
                // On Android 10+ scoped storage, we may need a RecoverableSecurityException flow
                // to prompt the user to allow deletion. For this foundation, we catch and ignore
                // if it fails due to permissions, but it works on older devices or with broad permissions.
                secEx.printStackTrace()
                Toast.makeText(this, "Original photo could not be deleted due to OS restrictions.", Toast.LENGTH_LONG).show()
            }

            Toast.makeText(this, "Photo Hidden Securely!", Toast.LENGTH_SHORT).show()
            loadHiddenPhotos()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to hide photo", Toast.LENGTH_SHORT).show()
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
}
