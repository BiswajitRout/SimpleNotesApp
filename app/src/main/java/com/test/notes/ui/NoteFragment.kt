package com.test.notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.util.*

@AndroidEntryPoint
class NoteFragment : Fragment() {

    private var note: Note? = null
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<NoteViewModel>()
    private lateinit var imageUri: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNoteData()
        setListeners()
        setObservers()
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
        binding.bSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
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
                val newNote = Note(0, title, imageUri, description, false, Date(), Date())
                viewModel.createNote(newNote)
            }
        }
    }

    private fun setNoteData() {
        val jsonNote = arguments?.getString("note")
        if (jsonNote.isNullOrEmpty()) {
            binding.tvHeader.text = resources.getString(R.string.title_add_note)
            binding.bSubmit.text = resources.getString(R.string.title_add_note)
        } else {
            binding.bDelete.visibility = View.VISIBLE
            binding.tvHeader.text = resources.getString(R.string.title_edit_note)
            note = Gson().fromJson(jsonNote, Note::class.java)
            imageUri = note?.image ?: ""
            if (imageUri.isNotEmpty())
                Glide.with(requireContext()).load(imageUri).into(binding.ivImage)
            binding.etTitle.setText(note?.title)
            binding.etDescription.setText(note?.description)
            binding.bSubmit.text = resources.getString(R.string.title_update_note)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}