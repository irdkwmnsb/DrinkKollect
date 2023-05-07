package ru.alzhanov.drinkkollect

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.alzhanov.drinkkollect.databinding.FragmentNewPostBinding

class NewPostFragment : Fragment() {
    private var _binding: FragmentNewPostBinding? = null
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
                if (result.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val imageView = view?.findViewById<ImageView>(R.id.imageDrink)
                    imageView?.setImageURI(imageuri)
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
                if (result.resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedUri = data.data
                    val imageView = view?.findViewById<ImageView>(R.id.imageDrink)
                    imageView?.setImageURI(selectedUri)
                    // TODO what to do with taken pic
                    binding.imageDrink.visibility = View.VISIBLE
                    binding.buttonAddPhoto.visibility = View.GONE
                    binding.buttonTakePhoto.visibility = View.GONE
                    binding.buttonRemovePhoto.visibility = View.VISIBLE
                }
            }
        }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageDrink.visibility = View.GONE
        binding.buttonRemovePhoto.visibility = View.GONE

        val permissionLauncherForUploading = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openUploadPhotoForResult()
            } else {
                // Do otherwise
            }
        }
        binding.buttonAddPhoto.setOnClickListener {
            permissionLauncherForUploading.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val permissionLauncherForCamera = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranged ->
            if (isGranged) {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic")
                contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
                imageuri = activity?.contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                openTakePicForResult()
            }
        }
            binding.buttonTakePhoto.setOnClickListener {
                permissionLauncherForCamera.launch(Manifest.permission.CAMERA)
            }
        binding.buttonRemovePhoto.setOnClickListener {
            // TODO remove pic
            binding.imageDrink.visibility = View.GONE
            binding.buttonRemovePhoto.visibility = View.GONE
            binding.buttonAddPhoto.visibility = View.VISIBLE
            binding.buttonTakePhoto.visibility = View.VISIBLE
        }
    }
}