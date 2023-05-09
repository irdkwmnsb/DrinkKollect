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
import androidx.navigation.fragment.findNavController
import drinkollect.v1.DrinkollectOuterClass
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
                    updateBindingWithPic()
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
                    imageuri = data.data
                    val imageView = view?.findViewById<ImageView>(R.id.imageDrink)
                    imageView?.setImageURI(imageuri)
                    updateBindingWithPic()
                }
            }
        }

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
        if (savedInstanceState?.getString("imageuri") != null) {
            imageuri = Uri.parse(savedInstanceState.getString("imageuri"))
        }
        if (imageuri != null) {
            binding.imageDrink.setImageURI(imageuri)
            updateBindingWithPic()
        } else {
            updateBindingWithoutPic()
        }
        binding.editTextDrinkName.editText?.setText(savedInstanceState?.getString("drinkName"))
        binding.editTextDrinkDescription.editText?.setText(savedInstanceState?.getString("drinkDescription"))
        binding.editTextDrinkLocation.editText?.setText(savedInstanceState?.getString("drinkLocation"))
        val permissionLauncherForUploading = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openUploadPhotoForResult()
            } else {
                // TODO i guess do nothing with no permissions
            }
        }
        binding.buttonAddPhoto.setOnClickListener {
            permissionLauncherForUploading.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val permissionLauncherForCamera = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic")
                contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
                imageuri = activity?.contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                openTakePicForResult()
            } else {
                // TODO i guess do nothing with no permissions
            }
        }
        binding.buttonTakePhoto.setOnClickListener {
            permissionLauncherForCamera.launch(Manifest.permission.CAMERA)
        }
        binding.buttonRemovePhoto.setOnClickListener {
            imageuri = null
            updateBindingWithoutPic()
        }
        binding.buttonNewPostDone.setOnClickListener {
            if ((activity as MainActivity).service.getUsername() == null) {
                findNavController().navigate(R.id.action_NewPostFragment_to_LoginFragment)
            } else {
                (activity as MainActivity).service.createPostRequest(
                    binding.editTextDrinkName.editText?.text.toString(),
                    binding.editTextDrinkDescription.editText?.text.toString(),
                    binding.editTextDrinkLocation.editText?.text.toString(),
                    // TODO get S3Resource from imageuri
                    DrinkollectOuterClass.S3Resource.getDefaultInstance()
                )
                findNavController().navigate(R.id.action_NewPostFragment_to_MainScrollFragment)
            }
        }
    }

    private fun updateBindingWithoutPic() {
        binding.imageDrink.visibility = View.GONE
        binding.buttonAddPhoto.visibility = View.VISIBLE
        binding.buttonTakePhoto.visibility = View.VISIBLE
        binding.buttonRemovePhoto.visibility = View.GONE
    }

    private fun updateBindingWithPic() {
        binding.imageDrink.visibility = View.VISIBLE
        binding.buttonAddPhoto.visibility = View.GONE
        binding.buttonTakePhoto.visibility = View.GONE
        binding.buttonRemovePhoto.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            "drinkName",
            binding.editTextDrinkName.editText?.text.toString()
        )
        outState.putString(
            "drinkDescription",
            binding.editTextDrinkDescription.editText?.text.toString()
        )
        outState.putString(
            "drinkLocation",
            binding.editTextDrinkLocation.editText?.text.toString()
        )
        if (imageuri == null) {
            outState.putString("imageuri", null)
        } else {
            outState.putString("imageuri", imageuri.toString())
        }
    }
}