package ru.alzhanov.drinkkollect

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import ru.alzhanov.drinkkollect.databinding.ActivityAddPostBinding


class AddPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding
    private var imageuri: Uri? = null

    private fun openTakePicForResult() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri)
        takePictureLauncher.launch(intent)
    }

    private var takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (result.resultCode == RESULT_OK && data != null) {
                    val imageView = findViewById<ImageView>(R.id.imageDrink)
                    imageView.setImageURI(imageuri)
                    // TODO what to do with uploaded pic
                    binding.imageDrink.visibility = View.VISIBLE
                    binding.buttonAddPhoto.visibility = View.GONE
                    binding.buttonTakePhoto.visibility = View.GONE
                    binding.buttonRemovePhoto.visibility = View.VISIBLE
                }
            }
        }

    private fun openUploadPhotoForResult() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        uploadPhotoLauncher.launch(intent)
    }

    private var uploadPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (result.resultCode == RESULT_OK && data != null) {
                    val selectedUri = data.data
                    val imageView = findViewById<ImageView>(R.id.imageDrink)
                    imageView.setImageURI(selectedUri)
                    // TODO what to do with taken pic
                    binding.imageDrink.visibility = View.VISIBLE
                    binding.buttonAddPhoto.visibility = View.GONE
                    binding.buttonTakePhoto.visibility = View.GONE
                    binding.buttonRemovePhoto.visibility = View.VISIBLE
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageDrink.visibility = View.GONE
        binding.buttonRemovePhoto.visibility = View.GONE
        findViewById<Button>(R.id.buttonAddPhoto).setOnClickListener {
            if (checkStoragePermission()) {
                openUploadPhotoForResult()
            } else {
                requestStoragePermission()
            }
        }
        findViewById<Button>(R.id.buttonTakePhoto).setOnClickListener {
            if (checkCameraPermission()) {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic")
                contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
                imageuri = this.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                openTakePicForResult()
            } else {
                requestCameraPermission()
            }
        }
        findViewById<Button>(R.id.buttonRemovePhoto).setOnClickListener {
            // TODO remove pic
            binding.imageDrink.visibility = View.GONE
            binding.buttonRemovePhoto.visibility = View.GONE
            binding.buttonAddPhoto.visibility = View.VISIBLE
            binding.buttonTakePhoto.visibility = View.VISIBLE
        }

    }


    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
    }
    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestStoragePermission() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 200)
    }
}