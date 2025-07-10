package com.example.inkspire.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.inkspire.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.inkspire.databinding.FragmentEditUserProfileBinding
import com.example.inkspire.factory.UserProfileFormViewModelFactory
import com.example.inkspire.repository.StorageRepository
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.model.UserProfile
import com.example.inkspire.viewmodel.UserProfileFormViewModel
import io.ktor.http.ContentType
import kotlinx.coroutines.launch

class EditUserProfileFragment : Fragment(R.layout.fragment_edit_user_profile) {

    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userProfileViewModel: UserProfileFormViewModel
    private var selectedImageUri: Uri? = null
    private var imageRemoved: Boolean = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imageRemoved = false
            Glide.with(binding.editProfilePic.context)
                .load(uri)
                .placeholder(R.drawable.ic_account_circle)
                .error(R.drawable.ic_account_circle)
                .circleCrop()
                .into(binding.editProfilePic)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupImagePicker()
        setupDeleteImageButton()
        observeLiveData()

        val currentUserId = userProfileViewModel.getCurrentUserId()
        if (currentUserId != null) {
            userProfileViewModel.loadUserProfile(currentUserId)
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        setupSaveButton()
    }

    private fun setupViewModel() {
        val factory = UserProfileFormViewModelFactory(
            UserRepository(),
            StorageRepository()
        )
        userProfileViewModel = ViewModelProvider(this, factory)[UserProfileFormViewModel::class.java]
    }

    private fun setupImagePicker() {
        binding.editProfilePic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupDeleteImageButton() {
        binding.profileRemoveResultPicButton.setOnClickListener {
            selectedImageUri = null
            imageRemoved = true
            binding.editProfilePic.setImageResource(R.drawable.ic_account_circle)
        }
    }

    private fun observeLiveData() {
        userProfileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (profile != null && !imageRemoved && selectedImageUri == null) {
                binding.editProfileUsername.text = profile.username
                binding.editProfileBio.setText(profile.bio ?: "")

                Glide.with(binding.editProfilePic.context)
                    .load(profile.profile_pic ?: R.drawable.ic_account_circle)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .circleCrop()
                    .into(binding.editProfilePic)
            }
        }

        userProfileViewModel.saveResult.observe(viewLifecycleOwner) { success ->
            binding.saveProfileButton.isEnabled = true
            if (success) {
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_LONG).show()
            }
        }

        userProfileViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.saveProfileButton.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupSaveButton() {
        binding.saveProfileButton.setOnClickListener {
            val bioText = binding.editProfileBio.text.toString().trim()
            val currentProfile = userProfileViewModel.profile.value

            if (currentProfile == null) {
                Toast.makeText(requireContext(), "Profile not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                binding.saveProfileButton.isEnabled = false
                var imageUrl = currentProfile.profile_pic

                if (selectedImageUri != null) {
                    val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        val filePath = "profile_${System.currentTimeMillis()}.jpg"
                        val uploadedUrl = userProfileViewModel.uploadImage(
                            bucket = "profile-pics",
                            filePath = filePath,
                            byteArray = bytes,
                            contentType = ContentType.Image.JPEG
                        )
                        if (uploadedUrl != null) {
                            imageUrl = uploadedUrl
                        }
                    }
                }

                if (imageRemoved) {
                    imageUrl = null
                }

                val updatedProfile = UserProfile(
                    id = currentProfile.id,
                    username = currentProfile.username,
                    bio = bioText,
                    profile_pic = imageUrl
                )

                userProfileViewModel.saveUserProfile(updatedProfile)
                imageRemoved = false // Reset flag dopo salvataggio
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}