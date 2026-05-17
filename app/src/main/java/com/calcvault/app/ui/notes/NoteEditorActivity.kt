package com.calcvault.app.ui.notes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity
import java.io.File

class NoteEditorActivity : BaseVaultActivity() {

    private var editingFileName: String? = null
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        etTitle = findViewById(R.id.et_note_title)
        etContent = findViewById(R.id.et_note_content)

        editingFileName = intent.getStringExtra("FILE_NAME")

        if (editingFileName != null) {
            loadNote(editingFileName!!)
        }

        findViewById<Button>(R.id.btn_save_note).setOnClickListener {
            saveNote()
        }
    }

    private fun getMasterKey(): MasterKey {
        return MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private fun getStorageDirName(): String {
        val isFake = intent.getBooleanExtra("IS_FAKE_VAULT", false)
        return if (isFake) "fake_secure_notes" else "secure_notes"
    }

    private fun loadNote(fileName: String) {
        try {
            val dir = File(filesDir, getStorageDirName())
            val file = File(dir, fileName)

            val encryptedFile = EncryptedFile.Builder(
                this,
                file,
                getMasterKey(),
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            val content = encryptedFile.openFileInput().bufferedReader().use { it.readText() }
            etTitle.setText(fileName.substringBeforeLast(".txt").replace("_", " "))
            etContent.setText(content)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString()

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dir = File(filesDir, getStorageDirName())
            if (!dir.exists()) dir.mkdirs()

            // If editing an existing note and title changed, we should ideally delete the old one.
            // For simplicity in this iteration, we just overwrite if name matches or create new.
            val fileName = title.replace(" ", "_") + ".txt"
            val file = File(dir, fileName)

            if (file.exists()) {
                file.delete() // Delete before rewriting to encrypted file
            }

            val encryptedFile = EncryptedFile.Builder(
                this,
                file,
                getMasterKey(),
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            encryptedFile.openFileOutput().bufferedWriter().use { it.write(content) }

            Toast.makeText(this, "Note saved securely!", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
        }
    }
}
