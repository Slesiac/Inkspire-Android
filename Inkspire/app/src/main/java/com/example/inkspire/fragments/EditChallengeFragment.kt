package com.example.inkspire.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.inkspire.R
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.inkspire.databinding.FragmentEditChallengeBinding
import com.example.inkspire.factory.ChallengeFormViewModelFactory
import com.example.inkspire.model.Challenge
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.StorageRepository
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.util.onDrawableEndClick
import com.example.inkspire.viewmodel.ChallengeFormViewModel
import io.ktor.http.ContentType
import kotlinx.coroutines.launch

class EditChallengeFragment : Fragment(R.layout.fragment_edit_challenge) {

    private var _binding: FragmentEditChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var formViewModel: ChallengeFormViewModel
    private var challengeId: Int = -1
    private var selectedImageUri: Uri? = null
    private var imageRemoved: Boolean = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imageRemoved = false // perché stai caricando una nuova immagine
            binding.editResultPic.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeId = arguments?.let {
            EditChallengeFragmentArgs.fromBundle(it).challengeId
        } ?: -1

        setupViewModel()
        setupImagePicker()
        setupRemoveImageButton()
        setupRandomConceptAndConstraint()
        observeLiveData()
        loadChallenge()
        setupButtons()
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
        binding.editResultPic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupRemoveImageButton() {
        binding.editRemoveResultPicButton.setOnClickListener {
            selectedImageUri = null
            imageRemoved = true
            binding.editResultPic.setImageResource(R.drawable.logo)
        }
    }

    private fun loadChallenge() {
        formViewModel.loadChallenge(challengeId)
    }

    private fun setupButtons() {
        binding.editChallengeButton.setOnClickListener {
            val title = binding.editChallengeTitle.text.toString().trim()
            val concept = binding.editChallengeConcept.text.toString().trim()
            val constraint = binding.editChallengeConstraint.text.toString().trim()
            val description = binding.editChallengeDescription.text.toString().trim().ifEmpty { null }

            if (title.isEmpty() || concept.isEmpty() || constraint.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val current = formViewModel.loadedChallenge.value
            if (current == null) {
                Toast.makeText(requireContext(), "Challenge not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                binding.editChallengeButton.isEnabled = false
                binding.deleteChallengeButton.isEnabled = false

                var imageUrl: String? = current.result_pic

                // Se l'utente ha caricato una nuova immagine
                selectedImageUri?.let { uri ->
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val filePath = "challenge_${System.currentTimeMillis()}.jpg"
                        val uploadedUrl = formViewModel.uploadImage(
                            bucket = "challenge-pics",
                            filePath = filePath,
                            byteArray = bytes,
                            contentType = ContentType.Image.JPEG
                        )
                        if (uploadedUrl != null) {
                            imageUrl = uploadedUrl
                        }
                    }
                }

                // Se l'utente ha rimosso l'immagine
                if (imageRemoved) {
                    imageUrl = null
                }

                val updated = Challenge(
                    id = current.id,
                    user_profile_id = current.user_id,
                    title = title,
                    concept = concept,
                    art_constraint = constraint,
                    description = description,
                    result_pic = imageUrl,
                    inserted_at = current.inserted_at,
                    updated_at = current.updated_at
                )

                formViewModel.saveChallenge(updated, isEdit = true)
            }
        }

        binding.deleteChallengeButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupRandomConceptAndConstraint() {
        binding.editChallengeConcept.onDrawableEndClick {
            formViewModel.fetchRandomConcept()
        }
        binding.editChallengeConstraint.onDrawableEndClick {
            formViewModel.fetchRandomArtConstraint()
        }
    }

    private fun observeLiveData() {
        formViewModel.loadedChallenge.observe(viewLifecycleOwner) { challenge ->
            if (challenge != null) {
                binding.editChallengeTitle.setText(challenge.title)
                binding.editChallengeConcept.setText(challenge.concept)
                binding.editChallengeConstraint.setText(challenge.art_constraint)
                binding.editChallengeDescription.setText(challenge.description ?: "")

                Glide.with(binding.editResultPic.context)
                    .load(challenge.result_pic ?: R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.editResultPic)
            } else {
                Toast.makeText(requireContext(), "Challenge not found", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.homeFragment)
            }
        }

        formViewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Challenge updated!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    R.id.homeFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, inclusive = true)
                        .build()
                )
            } else {
                Toast.makeText(requireContext(), "Failed to update challenge", Toast.LENGTH_LONG).show()
            }
        }

        formViewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Challenge deleted!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    R.id.homeFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, inclusive = true)
                        .build()
                )
            } else {
                Toast.makeText(requireContext(), "Failed to delete challenge", Toast.LENGTH_LONG).show()
            }
        }

        formViewModel.randomConcept.observe(viewLifecycleOwner) { concept ->
            binding.editChallengeConcept.setText(concept)
        }

        formViewModel.randomArtConstraint.observe(viewLifecycleOwner) { constraint ->
            binding.editChallengeConstraint.setText(constraint)
        }

        formViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.editChallengeButton.isEnabled = !isLoading
            binding.deleteChallengeButton.isEnabled = !isLoading
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Challenge")
            .setMessage("Are you sure you want to delete this challenge?")
            .setPositiveButton("Yes") { _, _ ->
                formViewModel.deleteChallenge(challengeId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}