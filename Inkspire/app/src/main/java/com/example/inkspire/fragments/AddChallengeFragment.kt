package com.example.inkspire.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.inkspire.databinding.FragmentAddChallengeBinding
import com.example.inkspire.factory.ChallengeFormViewModelFactory
import com.example.inkspire.model.Challenge
import com.example.inkspire.R
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.StorageRepository
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.util.onDrawableEndClick
import com.example.inkspire.viewmodel.ChallengeFormViewModel
import io.ktor.http.ContentType
import kotlinx.coroutines.launch

class AddChallengeFragment : Fragment(R.layout.fragment_add_challenge) {

    private var _binding: FragmentAddChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var formViewModel: ChallengeFormViewModel

    private var selectedImageUri: Uri? = null
    private var imageRemoved: Boolean = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imageRemoved = false
            binding.addResultPic.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupImagePicker()
        setupDeleteImageButton()
        setupSaveButton()
        setupRandomConceptAndConstraint()
        observeLiveData()
    }

    private fun setupViewModel() {
        formViewModel = ViewModelProvider(
            this,
            ChallengeFormViewModelFactory(
                ChallengeRepository(),
                UserRepository(),
                StorageRepository()
            )
        )[ChallengeFormViewModel::class.java]
    }

    private fun setupImagePicker() {
        binding.addResultPic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupDeleteImageButton() {
        binding.addRemoveResultPicButton.setOnClickListener {
            selectedImageUri = null
            imageRemoved = true
            binding.addResultPic.setImageResource(R.drawable.logo)
            Toast.makeText(requireContext(), "Image removed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSaveButton() {
        binding.addChallengeButton.setOnClickListener {
            val title = binding.addChallengeTitle.text.toString().trim()
            val concept = binding.addChallengeConcept.text.toString().trim()
            val constraint = binding.addChallengeConstraint.text.toString().trim()
            val description = binding.addChallengeDescription.text.toString().trim().ifEmpty { null }

            if (title.isEmpty() || concept.isEmpty() || constraint.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = formViewModel.getCurrentUserId()
            if (userId == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                binding.addChallengeButton.isEnabled = false

                var imageUrl: String? = null

                if (selectedImageUri != null) {
                    val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val filePath = "challenge_${System.currentTimeMillis()}.jpg"
                        val url = formViewModel.uploadImage(
                            bucket = "challenge-pics",
                            filePath = filePath,
                            byteArray = bytes,
                            contentType = ContentType.Image.JPEG
                        )
                        imageUrl = url
                    }
                }

                if (imageRemoved) {
                    imageUrl = null
                }

                val challenge = Challenge(
                    user_profile_id = userId,
                    title = title,
                    concept = concept,
                    art_constraint = constraint,
                    description = description,
                    result_pic = imageUrl
                )

                Log.d("AddChallengeFragment", "Upload result: $imageUrl")
                formViewModel.saveChallenge(challenge, isEdit = false)
            }
        }
    }

    private fun setupRandomConceptAndConstraint() {
        binding.addChallengeConcept.onDrawableEndClick {
            formViewModel.fetchRandomConcept()
        }
        binding.addChallengeConstraint.onDrawableEndClick {
            formViewModel.fetchRandomArtConstraint()
        }
    }

    private fun observeLiveData() {
        formViewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Challenge created!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.homeFragment)
            } else {
                Toast.makeText(requireContext(), "Failed to create challenge", Toast.LENGTH_LONG).show()
            }
        }

        formViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.addChallengeButton.isEnabled = !isLoading
        }

        formViewModel.randomConcept.observe(viewLifecycleOwner) { concept ->
            binding.addChallengeConcept.setText(concept)
        }

        formViewModel.randomArtConstraint.observe(viewLifecycleOwner) { constraint ->
            binding.addChallengeConstraint.setText(constraint)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}