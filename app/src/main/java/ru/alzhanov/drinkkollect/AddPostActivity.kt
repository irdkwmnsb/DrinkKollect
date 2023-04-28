package ru.alzhanov.drinkkollect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import ru.alzhanov.drinkkollect.databinding.ActivityAddPostBinding


class AddPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding
    private fun openSomeActivityForResult() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (result.resultCode == RESULT_OK && data != null) {
                    val selectedUri = data.data
                    val imageView = findViewById<ImageView>(R.id.imageDrink)
                    imageView.setImageURI(selectedUri)
                    binding.imageDrink.visibility = View.VISIBLE
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageDrink.visibility = View.GONE
        val selectButton = findViewById<Button>(R.id.buttonAddPhoto)
        selectButton.setOnClickListener {
            openSomeActivityForResult()
        }

    }

}