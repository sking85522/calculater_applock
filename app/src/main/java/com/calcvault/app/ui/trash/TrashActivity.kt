package com.calcvault.app.ui.trash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calcvault.app.R
import com.calcvault.app.ui.base.BaseVaultActivity
import com.calcvault.app.utils.TrashManager
import java.io.File

class TrashActivity : BaseVaultActivity() {

    private lateinit var recyclerTrash: RecyclerView
    private val trashFiles = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        recyclerTrash = findViewById(R.id.recycler_trash)
        recyclerTrash.layoutManager = LinearLayoutManager(this)

        loadTrash()
    }

    private fun loadTrash() {
        trashFiles.clear()
        trashFiles.addAll(TrashManager.getTrashFiles(this))
        recyclerTrash.adapter = TrashAdapter(trashFiles) { file ->
            showTrashOptions(file)
        }
    }

    private fun showTrashOptions(file: File) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Trash Options")
        builder.setMessage("What do you want to do with this file?")

        builder.setPositiveButton("Restore") { _, _ ->
            TrashManager.restoreFromTrash(this, file)
            Toast.makeText(this, "File Restored", Toast.LENGTH_SHORT).show()
            loadTrash()
        }

        builder.setNegativeButton("Delete Permanently") { _, _ ->
            TrashManager.permanentlyDelete(file)
            Toast.makeText(this, "File Deleted Forever", Toast.LENGTH_SHORT).show()
            loadTrash()
        }

        builder.setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    inner class TrashAdapter(private val files: List<File>, private val onClick: (File) -> Unit) : RecyclerView.Adapter<TrashAdapter.TrashViewHolder>() {
        inner class TrashViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tv_note_title)
            init {
                view.setOnClickListener { onClick(files[adapterPosition]) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
            return TrashViewHolder(view)
        }

        override fun onBindViewHolder(holder: TrashViewHolder, position: Int) {
            holder.tvTitle.text = files[position].name.substringAfter("__")
        }

        override fun getItemCount(): Int = files.size
    }
}
