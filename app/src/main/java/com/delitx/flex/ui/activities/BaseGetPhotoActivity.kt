package com.delitx.flex.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseGetPhotoActivity : AppCompatActivity() {
    private lateinit var mPathToFile: String
    val GALLERY_REQUEST_CODE = 200
    val TAKE_PHOTO_REQUEST_CODE = 100
    val REQUEST_CAMERA_PERMISSION = 201
    private var mCanTakePhoto: Boolean? = null
    var mFile: File? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                TAKE_PHOTO_REQUEST_CODE -> {
                    val captureImage = BitmapFactory.decodeFile(mPathToFile)
                    if (captureImage != null) {
                        mFile = File(mPathToFile)
                        onTakePhoto(captureImage)
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    if (data != null) {
                        val selectedImageUri = data.data
                        val pathList = selectedImageUri!!.pathSegments
                        val imagePath = pathList[1]
                        mFile = createPhotoFile()
                        if (mFile == null) {
                            onError()
                        } else {
                            uriToFile(selectedImageUri, mFile!!)
                            onGetPhotoFromGallery(imagePath, selectedImageUri)
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty()) {
                var counter = 0
                for (i in 0..permissions.size - 1) {
                    val permission = permissions[i]
                    if (android.Manifest.permission.CAMERA == permission) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            counter++
                        }
                    }
                    if (android.Manifest.permission.WRITE_EXTERNAL_STORAGE == permission) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            counter++
                        }
                    }
                }
                if (counter == 2) {
                    mCanTakePhoto = true
                    takePicture()
                } else {
                    mCanTakePhoto = false
                }
            }
        }
    }

    abstract fun onTakePhoto(photo: Bitmap)

    abstract fun onGetPhotoFromGallery(imagePath: String, imageUri: Uri)
    abstract fun onError()

    fun getPictureFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    fun takePicture() {
        mCanTakePhoto = (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
        if (Build.VERSION.SDK_INT >= 23 && (mCanTakePhoto == false)) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_CAMERA_PERMISSION
            )
        }
        if (mCanTakePhoto == true) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(this.packageManager) != null) {
                val photoFile: File? = createPhotoFile()
                if (photoFile != null) {
                    mPathToFile = photoFile.path
                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "com.delitx.flex.fileprovider",
                        photoFile
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    intent.putExtra("return-data", true)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE)
                }
            }
        }
    }

    private fun createPhotoFile(): File? {
        val name: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File("$storageDir/$name.jpg")
        try {
            file.createNewFile()
        } catch (e: Exception) {
            Log.d("asdf", e.toString())
        }
        return file
    }

    private fun uriToFile(uri: Uri, file: File) {
        val inputStream = contentResolver.openInputStream(uri)
        var output: OutputStream? = null
        try {
            output = FileOutputStream(mFile)
            val buffer = ByteArray(4 * 1024)
            var len: Int = inputStream?.read(buffer) ?: 0
            while (len > 0) {
                output.write(buffer, 0, len)
                len = inputStream?.read(buffer) ?: 0
            }
        } finally {
            try {
                output?.close()
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}