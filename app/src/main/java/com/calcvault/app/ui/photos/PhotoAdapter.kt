package com.calcvault.app.ui.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.calcvault.app.R
import kotlinx.coroutines.*
import java.io.File

class PhotoAdapter(private val context: Context, private val photos: List<File>) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Used to track jobs so we can cancel them when views are recycled
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_hidden_photo)

        init {
            view.setOnLongClickListener {
                val file = photos[adapterPosition]
                val builder = androidx.appcompat.app.AlertDialog.Builder(context)
                builder.setTitle("Delete")
                builder.setMessage("Move this item to Trash?")
                builder.setPositiveButton("Yes") { _, _ ->
                    com.calcvault.app.utils.TrashManager.moveToTrash(context, file)
                    if (context is PhotoVaultActivity) {
                        context.loadHiddenPhotos()
                    }
                }
                builder.setNegativeButton("No", null)
                builder.show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val file = photos[position]
        holder.imageView.setImageResource(android.R.color.darker_gray) // Placeholder

        // Tag the view so we know if the coroutine still belongs to this view when it finishes
        holder.imageView.tag = file.absolutePath

        scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val encryptedFile = EncryptedFile.Builder(
                        context,
                        file,
                        masterKey,
                        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                    ).build()

                    encryptedFile.openFileInput().use { inputStream ->
                        val bytes = inputStream.readBytes()

                        // Decode image size
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                        // Calculate inSampleSize
                        options.inSampleSize = calculateInSampleSize(options, 150, 150)

                        // Decode with inSampleSize
                        options.inJustDecodeBounds = false
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            // Check if the view is still trying to load THIS file (hasn't been recycled)
            if (holder.imageView.tag == file.absolutePath && bitmap != null) {
                holder.imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    override fun getItemCount(): Int = photos.size
}
