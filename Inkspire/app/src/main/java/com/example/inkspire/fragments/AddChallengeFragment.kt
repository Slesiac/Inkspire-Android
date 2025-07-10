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


/*
class AddChallengeFragment : Fragment() {

    private var _binding: FragmentAddChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChallengeFormViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inizializza il ViewModel
        val factory = ChallengeFormViewModelFactory(ChallengeRepository(), UserRepository())
        viewModel = ViewModelProvider(this, factory)[ChallengeFormViewModel::class.java]

        // Click sul bottone di salvataggio
        binding.addChallengeButton.setOnClickListener {
            val title = binding.addChallengeTitle.text.toString().trim()
            val concept = binding.addChallengeConcept.text.toString().trim()
            val constraint = binding.addChallengeConstraint.text.toString().trim()
            val description = binding.addChallengeDescription.text.toString().trim()

            // Ottieni l'userId dal ViewModel
            val userId = viewModel.currentUserProfileId.value
            if (userId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val challenge = Challenge(
                id = 0,
                user_profile_id = userId,
                title = title,
                concept = concept,
                art_constraint = constraint,
                description = description.ifBlank { null },
                result_pic = null, // gestione immagini in futuro
                inserted_at = null,
                updated_at = null
            )

            viewModel.saveChallenge(challenge, isEdit = false)
        }

        // Osserva esito salvataggio
        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Challenge saved!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Failed to save challenge", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
*/


/*
class AddChallengeFragment : BaseChallengeFragment<FragmentAddChallengeBinding>(
    FragmentAddChallengeBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addChallengeButton.setOnClickListener { saveChallenge() }

        binding.addChallengeConcept.onDrawableEndClick {
            challengeViewModel.fetchRandomConcept()
        }

        binding.addChallengeConstraint.onDrawableEndClick {
            challengeViewModel.fetchRandomArtConstraint()
        }

        // Osserva LiveData per navigare solo dopo inserimento completato
        challengeViewModel.challengeInserted.observe(viewLifecycleOwner) { inserted ->
            if (inserted) {
                Toast.makeText(requireContext(), "Challenge saved!", Toast.LENGTH_SHORT).show()

                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("challenge_updated", true)

                findNavController().navigate(R.id.myProfileFragment)

                // Reset del flag per evitare navigazioni multiple
                challengeViewModel.clearChallengeInsertedFlag()
            }
        }
    }

    override fun onRandomConceptReceived(concept: String?) {
        binding.addChallengeConcept.setText(concept ?: "")
    }

    override fun onRandomArtConstraintReceived(constraint: String?) {
        binding.addChallengeConstraint.setText(constraint ?: "")
    }

    private fun saveChallenge() {
        val title = binding.addChallengeTitle.text.toString().trim()
        val concept = binding.addChallengeConcept.text.toString().trim()
        val artConstraint = binding.addChallengeConstraint.text.toString().trim()
        val description = binding.addChallengeDescription.text.toString().trim()

        val currentUserId = SupabaseManager.auth.currentUserOrNull()?.id

        if (currentUserId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty() || concept.isEmpty() || artConstraint.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val challenge = Challenge(
            id = 0,
            user_profile_id = currentUserId,
            title = title,
            concept = concept,
            art_constraint = artConstraint,
            description = description.ifEmpty { null },
            result_pic = null,
            inserted_at = null,
            updated_at = null
        )

        challengeViewModel.insertChallenge(challenge)

//        Toast.makeText(requireContext(), "Challenge saved!", Toast.LENGTH_SHORT).show()
//
//        // Notifica il fragment precedente
//        findNavController().previousBackStackEntry
//            ?.savedStateHandle
//            ?.set("challenge_updated", true)
//
//        // Il ritorno allo UserProfileFragment avverrà grazie al BottomNav o al popBackStack automatico.
    }
}

 */