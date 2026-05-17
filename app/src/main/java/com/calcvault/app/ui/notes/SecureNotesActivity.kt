package com.calcvault.app.ui.notes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class SecureNotesActivity : BaseVaultActivity() {

    private lateinit var recyclerNotes: RecyclerView
    private val notes = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secure_notes)

        recyclerNotes = findViewById(R.id.recycler_notes)
        recyclerNotes.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.fab_add_note).setOnClickListener {
            startActivity(Intent(this, NoteEditorActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun getStorageDirName(): String {
        val isFake = intent.getBooleanExtra("IS_FAKE_VAULT", false)
        return if (isFake) "fake_secure_notes" else "secure_notes"
    }

    private fun loadNotes() {
        notes.clear()
        val dir = File(filesDir, getStorageDirName())
        if (dir.exists()) {
            dir.listFiles()?.let {
                notes.addAll(it)
            }
        }
        recyclerNotes.adapter = NoteAdapter(notes) { file ->
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("FILE_NAME", file.name)
            val isFake = this.intent.getBooleanExtra("IS_FAKE_VAULT", false)
            intent.putExtra("IS_FAKE_VAULT", isFake)
            startActivity(intent)
        }
    }

    inner class NoteAdapter(private val files: List<File>, private val onClick: (File) -> Unit) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

        inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tv_note_title)

            init {
                view.setOnClickListener { onClick(files[adapterPosition]) }
                view.setOnLongClickListener {
                    val file = files[adapterPosition]
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this@SecureNotesActivity)
                    builder.setTitle("Delete")
                    builder.setMessage("Move this item to Trash?")
                    builder.setPositiveButton("Yes") { _, _ ->
                        com.calcvault.app.utils.TrashManager.moveToTrash(this@SecureNotesActivity, file)
                        loadNotes()
                    }
                    builder.setNegativeButton("No", null)
                    builder.show()
                    true
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
            return NoteViewHolder(view)
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            val file = files[position]
            // Removing the timestamp/extension to just show the title roughly
            holder.tvTitle.text = file.name.substringBeforeLast(".txt").replace("_", " ")
        }

        override fun getItemCount(): Int = files.size
    }
}
