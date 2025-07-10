package com.example.inkspire.fragments

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inkspire.viewmodel.UserProfileViewModel
import com.example.inkspire.factory.UserProfileViewModelFactory
import com.bumptech.glide.Glide
import com.example.inkspire.R
import com.example.inkspire.adapter.ChallengeAdapter
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.databinding.FragmentUserProfileBinding
import com.example.inkspire.repository.ChallengeRepository

class OtherUserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var challengeAdapter: ChallengeAdapter

    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.let {
            OtherUserProfileFragmentArgs.fromBundle(it).userId
        }

        setupViewModel()
        setupRecyclerView()

        if (userId != null) {
            loadData(userId!!)
        } else {
            Toast.makeText(requireContext(), "Invalid user", Toast.LENGTH_SHORT).show()
        }

        // Nascondi eventuale pulsante di edit
        binding.editProfileButton?.visibility = View.GONE
    }

    private fun setupViewModel() {
        val factory = UserProfileViewModelFactory(UserRepository(), ChallengeRepository())
        userProfileViewModel = ViewModelProvider(this, factory)[UserProfileViewModel::class.java]
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter { selectedChallenge ->
            val action = OtherUserProfileFragmentDirections
                .actionOtherUserProfileFragmentToViewChallengeFragment(selectedChallenge.id)
            findNavController().navigate(action)
        }

        binding.challengeRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = challengeAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadData(userId: String) {
        binding.progressBar.visibility = View.VISIBLE
        userProfileViewModel.getUserData(userId)
        userProfileViewModel.getUserChallenges(userId)

        observeProfile()
        observeStats()
        observeChallenges()
    }

    private fun observeProfile() {
        userProfileViewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.progressBar.visibility = View.GONE
            if (profile != null) {
                binding.profileUsername.text = profile.username
                binding.profileBio.text = profile.bio ?: ""
                Glide.with(binding.profilePic.context)
                    .load(profile.profile_pic ?: R.drawable.ic_account_circle)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .circleCrop()
                    .into(binding.profilePic)
            } else {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeStats() {
        userProfileViewModel.stats.observe(viewLifecycleOwner) { (total, completed) ->
            binding.createdChallenges.text = total.toString()
            binding.completedChallenges.text = completed.toString()
        }
    }

    private fun observeChallenges() {
        userProfileViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.submitList(challenges)
            val isEmpty = challenges.isEmpty()
            binding.challengeRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.emptyChallengeImage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}