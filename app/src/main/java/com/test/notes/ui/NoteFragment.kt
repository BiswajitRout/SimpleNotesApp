package com.test.notes.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.test.notes.R
import com.test.notes.databinding.FragmentNoteBinding
import com.test.notes.model.Note
import com.test.notes.ui.viewmodel.NoteViewModel
import com.test.notes.utils.OperationStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("OVERRIDE_DEPRECATION")
@AndroidEntryPoint
class NoteFragment : Fragment() {

    private var note: Note? = null
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<NoteViewModel>()
    private var imageUri: String = ""

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareCamera()
        setNoteData()
        setListeners()
        setObservers()
    }

    private fun prepareCamera() {
        /**Request camera permissions*/
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setObservers() {
        viewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                is OperationStatus.SUCCESS -> {
                    findNavController().popBackStack()
                }
                is OperationStatus.LOADING -> {

                }
            }
        }
    }

    private fun setListeners() {
        binding.bDelete.setOnClickListener {
            viewModel.deleteNote(note!!)
        }
        binding.fabImage.setOnClickListener {
            if (!binding.ivPreview.isVisible) {
                takePhoto()
            } else {
                Glide.with(requireContext()).load(R.drawable.ic_camera).into(binding.fabImage)
                binding.pvImage.visibility = View.VISIBLE
                binding.ivPreview.visibility = View.GONE
            }
        }
        binding.bSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            if (title.isNotEmpty() && description.isNotEmpty()) {
                if (note != null) {
                    note?.let {
                        note?.title = title
                        note?.image = imageUri
                        note?.description = description
                        note?.updatedAt = Date()
                        note?.isEdited = true
                    }
                    viewModel.updateNote(note!!)
                } else {
                    val newNote = Note(
                        _id = 0,
                        title = title,
                        description = description,
                        image = imageUri,
                        isEdited = false,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                    viewModel.createNote(newNote)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill the mandatory fields.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setNoteData() {
        val jsonNote = arguments?.getString("note")
        if (jsonNote.isNullOrEmpty()) {
            binding.tvHeader.text = resources.getString(R.string.title_add_note)
            binding.bSubmit.text = resources.getString(R.string.title_add_note)
            Glide.with(requireContext()).load(R.drawable.ic_camera).into(binding.fabImage)
            binding.pvImage.visibility = View.VISIBLE
        } else {
            binding.bDelete.visibility = View.VISIBLE
            binding.tvHeader.text = resources.getString(R.string.title_edit_note)
            note = Gson().fromJson(jsonNote, Note::class.java)
            imageUri = note?.image ?: ""
            if (imageUri.isNotEmpty()) {
                binding.ivPreview.visibility = View.VISIBLE
                Glide.with(requireContext()).load(imageUri).into(binding.ivPreview)
                Glide.with(requireContext()).load(R.drawable.ic_add_photo).into(binding.fabImage)
            }
            binding.etTitle.setText(note?.title)
            binding.etDescription.setText(note?.description)
            binding.bSubmit.text = resources.getString(R.string.title_update_note)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.pvImage.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e("Camera error", exc.message ?: "")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please allow camera permission to capture image.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val filePath = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
        val photoFile = File(outputDirectory, filePath)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            //ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("Photo capture failed:", "${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    imageUri = photoFile.absolutePath
                    CoroutineScope(Dispatchers.Main).launch {
                        Glide.with(requireContext()).load(photoFile).into(binding.ivPreview)
                        Glide.with(requireContext()).load(R.drawable.ic_add_photo)
                            .into(binding.fabImage)
                        binding.pvImage.visibility = View.GONE
                        binding.ivPreview.visibility = View.VISIBLE
                    }
                }
            })
    }
}