package com.example.inkspire.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.lifecycle.ViewModelProvider
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inkspire.viewmodel.UserProfileViewModel
import com.example.inkspire.factory.UserProfileViewModelFactory
import com.bumptech.glide.Glide
import com.example.inkspire.LoginActivity
import com.example.inkspire.R
import com.example.inkspire.adapter.ChallengeAdapter
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.databinding.FragmentUserProfileBinding
import com.example.inkspire.factory.AuthViewModelFactory
import com.example.inkspire.repository.AuthRepository
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.viewmodel.AuthViewModel

class UserProfileFragment : Fragment(R.layout.fragment_user_profile), MenuProvider {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupViewModel()
        setupRecyclerView()

        // Recupera l'ID dell'utente corrente
        val userId = UserRepository().getCurrentUserId()
        if (userId != null) {
            loadData(userId)
            setupEditButton()
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        val userProfileFactory = UserProfileViewModelFactory(UserRepository(), ChallengeRepository())
        userProfileViewModel = ViewModelProvider(this, userProfileFactory)[UserProfileViewModel::class.java]

        val authFactory = AuthViewModelFactory(AuthRepository())
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter { selectedChallenge ->
            val action = UserProfileFragmentDirections
                .actionUserProfileFragmentToViewChallengeFragment(selectedChallenge.id)
            findNavController().navigate(action)
        }
        binding.challengeRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = challengeAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadData(userId: String) {
        // Mostra progress bar
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

    private fun setupEditButton() {
        binding.editProfileButton.setOnClickListener {
            val action = UserProfileFragmentDirections
                .actionUserProfileFragmentToEditUserProfileFragment()
            findNavController().navigate(action)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.my_profile_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.logoutMenu -> {
                showLogoutDialog()
                true
            }
            else -> false
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Logout")
            setMessage("Are you sure you want to logout?")
            setPositiveButton("Yes") { _, _ ->
                authViewModel.logout {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}