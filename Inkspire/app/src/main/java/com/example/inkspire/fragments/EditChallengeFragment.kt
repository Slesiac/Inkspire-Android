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


/*

class EditChallengeFragment : Fragment() {

    private var _binding: FragmentEditChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChallengeFormViewModel
    private var challengeId: Int = -1
    private var currentChallenge: ChallengeVW? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ottieni ID passato come argomento di navigazione
        challengeId = arguments?.let {
            EditChallengeFragmentArgs.fromBundle(it).challengeId
        } ?: -1

        // Inizializza ViewModel
        val factory = ChallengeFormViewModelFactory(ChallengeRepository(), UserRepository())
        viewModel = ViewModelProvider(this, factory)[ChallengeFormViewModel::class.java]

        // Carica la challenge da modificare
        loadChallengeData()

        // Gestione salvataggio modifiche
        binding.editChallengeButton.setOnClickListener {
            currentChallenge?.let { original ->
                val updated = original.copy(
                    title = binding.editChallengeTitle.text.toString().trim(),
                    concept = binding.editChallengeConcept.text.toString().trim(),
                    art_constraint = binding.editChallengeConstraint.text.toString().trim(),
                    description = binding.editChallengeDescription.text.toString().trim().ifBlank { null }
                )

                viewModel.saveChallenge(updated.toChallenge(), isEdit = true)
            }
        }

        // Gestione eliminazione
        binding.deleteChallengeButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Osserva esito operazioni
        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Challenge updated!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { deleted ->
            if (deleted) {
                Toast.makeText(requireContext(), "Challenge deleted", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadChallengeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val challenge = ChallengeRepository().getChallengeById(challengeId)
            if (challenge != null) {
                currentChallenge = challenge
                binding.editChallengeTitle.setText(challenge.title)
                binding.editChallengeConcept.setText(challenge.concept)
                binding.editChallengeConstraint.setText(challenge.art_constraint)
                binding.editChallengeDescription.setText(challenge.description ?: "")
                // L'immagine non è gestita per ora
            } else {
                Toast.makeText(requireContext(), "Challenge not found", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Challenge")
            .setMessage("Are you sure you want to delete this challenge?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteChallenge(challengeId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Estensione per convertire da ChallengeUser a Challenge
    private fun ChallengeVW.toChallenge(): Challenge {
        return Challenge(
            id = this.id,
            user_profile_id = this.user_profile.id,
            title = this.title,
            concept = this.concept,
            art_constraint = this.art_constraint,
            description = this.description,
            result_pic = this.result_pic,
            inserted_at = this.inserted_at,
            updated_at = this.updated_at
        )
    }
}

 */


/*
class EditChallengeFragment : BaseChallengeFragment<FragmentEditChallengeBinding>(
    FragmentEditChallengeBinding::inflate
) {
    private val args: EditChallengeFragmentArgs by navArgs()
    private lateinit var currentChallenge: Challenge

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentChallenge = args.challenge

        populateFields()
        setupListeners()
    }

    private fun populateFields() = with(binding) {
        editChallengeTitle.setText(currentChallenge.title)
        editChallengeConcept.setText(currentChallenge.concept)
        editChallengeConstraint.setText(currentChallenge.art_constraint)
        editChallengeDescription.setText(currentChallenge.description)
    }

    private fun setupListeners() = with(binding) {
        editChallengeConcept.onDrawableEndClick {
            challengeViewModel.fetchRandomConcept()
        }
        editChallengeConstraint.onDrawableEndClick {
            challengeViewModel.fetchRandomArtConstraint()
        }

        editChallengeButton.setOnClickListener { updateChallenge() }
        deleteChallengeButton.setOnClickListener { confirmDelete() }
    }

    override fun onRandomConceptReceived(concept: String?) {
        binding.editChallengeConcept.setText(concept ?: "")
    }

    override fun onRandomArtConstraintReceived(constraint: String?) {
        binding.editChallengeConstraint.setText(constraint ?: "")
    }

    private fun updateChallenge() {
        val title = binding.editChallengeTitle.text.toString().trim()
        val concept = binding.editChallengeConcept.text.toString().trim()
        val constraint = binding.editChallengeConstraint.text.toString().trim()
        val description = binding.editChallengeDescription.text.toString().trim()

        if (title.isBlank() || concept.isBlank() || constraint.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updated = currentChallenge.copy(
            title = title,
            concept = concept,
            art_constraint = constraint,
            description = description.ifEmpty { null }
        )

        challengeViewModel.updateChallenge(updated)

        Toast.makeText(requireContext(), "Challenge updated", Toast.LENGTH_SHORT).show()

        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.set("challenge_updated", true)
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Challenge")
            .setMessage("Are you sure you want to delete this challenge?")
            .setPositiveButton("Delete") { _, _ ->
                challengeViewModel.deleteChallenge(currentChallenge.id)
                Toast.makeText(requireContext(), "Challenge deleted", Toast.LENGTH_SHORT).show()

                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("challenge_updated", true)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}*/