package com.calcvault.app.ui.documents

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

class DocumentVaultActivity : BaseVaultActivity() {

    private lateinit var recyclerDocs: RecyclerView
    private val hiddenDocs = mutableListOf<File>()

    private val pickDocLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                hideDocument(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_vault)

        recyclerDocs = findViewById(R.id.recycler_docs)
        recyclerDocs.layoutManager = LinearLayoutManager(this)

        loadHiddenDocs()

        findViewById<FloatingActionButton>(R.id.fab_add_doc).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            val mimeTypes = arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain", "application/vnd.android.package-archive")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            pickDocLauncher.launch(intent)
        }
    }

    private fun getStorageDirName(): String {
        val isFake = intent.getBooleanExtra("IS_FAKE_VAULT", false)
        return if (isFake) "fake_hidden_docs" else "hidden_docs"
    }

    private fun loadHiddenDocs() {
        hiddenDocs.clear()
        val dir = File(filesDir, getStorageDirName())
        if (dir.exists()) {
            dir.listFiles()?.let { hiddenDocs.addAll(it) }
        }
        recyclerDocs.adapter = DocAdapter(hiddenDocs)
    }

    private fun hideDocument(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dir = File(filesDir, getStorageDirName())
                if (!dir.exists()) dir.mkdirs()

                val fileName = getFileName(uri) ?: "doc_${System.currentTimeMillis()}"
                val hiddenFile = File(dir, fileName)

                val masterKey = MasterKey.Builder(this@DocumentVaultActivity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val encryptedFile = EncryptedFile.Builder(
                    this@DocumentVaultActivity,
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
                    // Ignore scoped storage failures for this demo
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DocumentVaultActivity, "Document Hidden Securely!", Toast.LENGTH_SHORT).show()
                    loadHiddenDocs()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DocumentVaultActivity, "Failed to hide document", Toast.LENGTH_SHORT).show()
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

    inner class DocAdapter(private val docs: List<File>) : RecyclerView.Adapter<DocAdapter.DocViewHolder>() {
        inner class DocViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tv_note_title)

            init {
                view.setOnLongClickListener {
                    val file = docs[adapterPosition]
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this@DocumentVaultActivity)
                    builder.setTitle("Delete")
                    builder.setMessage("Move this item to Trash?")
                    builder.setPositiveButton("Yes") { _, _ ->
                        com.calcvault.app.utils.TrashManager.moveToTrash(this@DocumentVaultActivity, file)
                        loadHiddenDocs()
                    }
                    builder.setNegativeButton("No", null)
                    builder.show()
                    true
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
            // Reusing item_note layout for simplicity (just a text view with a divider)
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
            return DocViewHolder(view)
        }

        override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
            holder.tvTitle.text = docs[position].name
        }

        override fun getItemCount(): Int = docs.size
    }
}
