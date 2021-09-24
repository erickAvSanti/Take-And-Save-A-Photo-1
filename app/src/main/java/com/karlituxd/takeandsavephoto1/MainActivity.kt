package com.karlituxd.takeandsavephoto1

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startForResult =
            this@MainActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                Log.e("MY_TAG", "on result")
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.e("MY_TAG", "result ok")
                    val f = File(currentPhotoPath)
                    findViewById<ImageView>(R.id.my_image).setImageURI(Uri.fromFile(f))

                    ContentValues().also { values ->
                        values.put(MediaStore.Images.Media.TITLE, "$currentPhotoPrefix$currentPhotoSuffix")
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, "$currentPhotoPrefix$currentPhotoSuffix")
                        values.put(MediaStore.Images.Media.DESCRIPTION, "")
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

                        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.also { url ->
                            contentResolver.openOutputStream(url)?.also { output ->
                                FileInputStream(f).copyTo(output)
                            }
                        }
                    }

                    // MediaStore.Images.Media.insertImage(contentResolver, currentPhotoPath, "my_image.jpg", "");
                }
            }
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){

            findViewById<Button>(R.id.my_button).setOnClickListener {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                    takePictureIntent ->
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        ex.message?.let { it1 -> Log.e("MY_TAG", it1) }
                        null
                    }
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startForResult.launch(takePictureIntent)
                    }
                }
            }
        }else{
            findViewById<Button>(R.id.my_button).isEnabled = false
        }
    }

    private lateinit var currentPhotoPath: String
    private lateinit var currentPhotoPrefix: String
    private lateinit var currentPhotoSuffix: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        currentPhotoPrefix = "takeAndSavePhoto_${timeStamp}_"
        currentPhotoSuffix = ".jpg"
        return File.createTempFile(
            currentPhotoPrefix, /* prefix */
            currentPhotoSuffix, /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}