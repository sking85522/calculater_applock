package com.calcvault.app.utils

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import java.io.File
import java.io.FileOutputStream

class CameraHelper(private val context: Context) {

    fun takeIntruderSelfie() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var frontCameraId: String? = null
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = cameraId
                    break
                }
            }

            if (frontCameraId == null) return

            // Simple background thread logic for camera
            val handlerThread = HandlerThread("CameraThread")
            handlerThread.start()
            val handler = Handler(handlerThread.looper)

            manager.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    capturePhoto(camera, handler)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                }
            }, handler)

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun capturePhoto(cameraDevice: CameraDevice, handler: Handler) {
        try {
            val imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)
            val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            imageReader.setOnImageAvailableListener({ reader ->
                var image: android.media.Image? = null
                try {
                    image = reader.acquireLatestImage()
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)
                    saveImage(bytes)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    image?.close()
                    cameraDevice.close()
                    handler.looper.quitSafely()
                }
            }, handler)

            cameraDevice.createCaptureSession(listOf(imageReader.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), null, handler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, handler)
        } catch (e: Exception) {
            e.printStackTrace()
            cameraDevice.close()
        }
    }

    private fun saveImage(bytes: ByteArray) {
        val dir = File(context.filesDir, "intruders")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "intruder_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { it.write(bytes) }

        sendBreakInAlertEmail(file)
    }

    private fun sendBreakInAlertEmail(file: File) {
        // This is a stub for the email sending logic.
        // In a real app, this would use a background worker (like WorkManager)
        // and an SMTP library (like JavaMail API) or a backend service API
        // to send the intruder photo to the user's registered recovery email silently.

        // println("Simulating sending email alert with attachment: ${file.name}")
    }
}
