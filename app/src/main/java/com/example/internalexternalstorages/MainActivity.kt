package com.example.internalexternalstorages

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "MainActivity"
        private const val isPersistent: Boolean = true
    }

    private var isInternal = false

    private var readPermissionGranted = false
    private var writePermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  checkStoragePaths()
        //  createInternalFile()
        //  saveInternalFile("Hello my friends")
        //   readInternalFile()
        requestPermission()

        findViewById<Button>(R.id.take_photo).setOnClickListener {
            takePhoto.launch()
        }

    }

    private fun checkStoragePaths() {
        val internalM1 = getDir("custom", 0)
        val internalM2 = filesDir

        // Bu tashqi xotiradagi ilovangiz shaxsiy katalogi uchun ildiz katalogini qaytaradi.
        val externalM1 = getExternalFilesDir(null)

        val externalM2 = externalCacheDir
        val externalM3 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        Log.d(TAG, "internalM1: $internalM1")
        Log.d(TAG, "internalM2: $internalM2")

        Log.d(TAG, "externalM1: $externalM1")
        Log.d(TAG, "externalM2: $externalM2")
        Log.d(TAG, "externalM3: $externalM3")
    }

    private fun createInternalFile() {
        val fileName: String = "pdp_internal.txt"

        val file: File = if (isPersistent) {
            File(filesDir, fileName)
        } else {
            File(cacheDir, fileName)
        }

        if (!file.exists()) {
            try {
                file.createNewFile()
                Toast.makeText(this, "File ($fileName) has been created", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this, "File ($fileName) creation failed!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File ($fileName) already exists", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveInternalFile(data: String) {
        val fileName: String = "pdp_internal.txt"

        try {

            val fileOutputStream: FileOutputStream = if (isPersistent) {
                openFileOutput(fileName, MODE_PRIVATE)
            } else {
                val file = File(cacheDir, fileName)
                FileOutputStream(file)
            }

            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, "Write ($data) successful", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            Toast.makeText(this, "Write to file ($data) failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readInternalFile() {
        val fileName: String = "pdp_internal.txt"

        try {
            val fileInputStream: FileInputStream = if (isPersistent) {
                openFileInput(fileName)
            } else {
                val file = File(cacheDir, fileName)
                FileInputStream(file)
            }

            val inputStreamReader: InputStreamReader =
                InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines: ArrayList<String> = ArrayList()

            val reader: BufferedReader = BufferedReader(inputStreamReader)

            var line: String? = reader.readLine()

            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }

            val readText: String = TextUtils.join("\n", lines)

            Toast.makeText(this, "Read from file ($fileName) successful", Toast.LENGTH_SHORT).show()

            Log.d(TAG, "readText: $readText")

        } catch (e: IOException) {
            Toast.makeText(this, "Read from file ($fileName) failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteInternalFile() {
        val fileName = "pdp_internal.txt"
        val file: File = if (isPersistent) {
            File(filesDir, fileName)
        } else {
            File(cacheDir, fileName)
        }
        if (file.exists()) {
            file.delete()
            Toast.makeText(this, "File %s has been deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "File %s doesn't exist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermission() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()

        if (!readPermissionGranted) permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!writePermissionGranted) permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionsToRequest.isNotEmpty()) permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted =
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted =
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

            if (readPermissionGranted) Toast.makeText(
                this,
                "READ EXTERNAL STORAGE",
                Toast.LENGTH_SHORT
            ).show()
            if (readPermissionGranted) Toast.makeText(
                this,
                "WRITE EXTERNAL STORAGE",
                Toast.LENGTH_SHORT
            ).show()
        }


    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            val fileName = UUID.randomUUID().toString()

            val isPhotoSaved = if (isInternal) {
                savePhotoToInternalStorage(fileName, bitmap!!)
            } else {
                if (writePermissionGranted) {
                    savePhotoToExternalStorage(fileName, bitmap!!)
                } else {
                    false
                }
            }

            if (isPhotoSaved) {
                Toast.makeText(this, "photo saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "failed to save photo!", Toast.LENGTH_SHORT).show()
            }
        }


    private fun savePhotoToInternalStorage(fileName: String, bitmap: Bitmap): Boolean {
        return try {
            openFileOutput("$fileName.jpg", MODE_PRIVATE).use { fileOutPutStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fileOutPutStream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private fun savePhotoToExternalStorage(fileName: String, bitmap: Bitmap): Boolean {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }

        return try {
            contentResolver.insert(collection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outPutStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outPutStream)) {
                        throw IOException("Couldn't save bitmap.")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }


    private fun createExternalFile() {
        val fileName = "pdp_external.txt"
        val file: File = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }
        Log.d("@@@", "absolutePath: " + file.absolutePath)
        if (!file.exists()) {
            try {
                file.createNewFile()
                //  "File %s has been created"   toast
            } catch (e: IOException) {
                //   "File %s creation failed"  toast
            }
        } else {
            //  "File %s already exists"   toast
        }
    }

    private fun saveExternalFile(data: String) {
        val fileName = "pdp_external.txt"
        val file: File = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }
        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            //  "Write to %s successful"  toast
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            //  "Write to file %s failed"  toast
        }
    }

    private fun readExternalFile() {
        val fileName = "pdp_external.txt"
        val file: File = if (isPersistent)
            File(getExternalFilesDir(null), fileName)
        else
            File(externalCacheDir, fileName)

        Log.d("@@@", file.absolutePath)

        try {
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines: MutableList<String?> = java.util.ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Log.d("StorageActivity", readText)
            //  "Read from file %s successful"   toast
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            //  "Read from file %s failed"   toast
        }
    }

    private fun deleteExternalFile() {
        val fileName = "pdp_external.txt"
        val file: File = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }
        if (file.exists()) {
            file.delete()
            //  "File %s has been deleted"   toast
        } else {
            //   "File %s doesn't exist"    toast
        }
    }


    private fun savePhotoFileToInternalStorage() {}
    private fun readPhotoFileFromInternalStorage() {}

    private fun saveVideoFileToInternalStorage() {}
    private fun readVideoFileFromInternalStorage() {}

    private fun saveAudioFileToInternalStorage() {}
    private fun readAudioFileFromInternalStorage() {}


    private fun savePhotoFileToExternalStorage() {}
    private fun readPhotoFileFromExternalStorage() {}

    private fun saveVideoFileToExternalStorage() {}
    private fun readVideoFileFromExternalStorage() {}

    private fun saveAudioFileToExternalStorage() {}
    private fun readAudioFileFromExternalStorage() {}
}




