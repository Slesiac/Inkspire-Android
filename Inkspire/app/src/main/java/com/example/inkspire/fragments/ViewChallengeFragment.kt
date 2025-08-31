package com.example.inkspire.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.inkspire.databinding.FragmentViewChallengeBinding
import com.example.inkspire.R
import com.example.inkspire.factory.ViewChallengeViewModelFactory
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.viewmodel.ViewChallengeViewModel

class ViewChallengeFragment : Fragment(R.layout.fragment_view_challenge) {

    private var _binding: FragmentViewChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewChallengeViewModel: ViewChallengeViewModel

    private var challengeId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera l'ID passato via Safe Args
        challengeId = arguments?.let {
            ViewChallengeFragmentArgs.fromBundle(it).challengeId
        } ?: -1

        setupViewModel()
        observeChallenge()
        observeAuthorVisibility()
        binding.progressBar.visibility = View.VISIBLE
        viewChallengeViewModel.loadChallenge(challengeId)
    }

    private fun setupViewModel() {
        val factory = ViewChallengeViewModelFactory(
            ChallengeRepository(),
            UserRepository()
        )
        viewChallengeViewModel = ViewModelProvider(this, factory)[ViewChallengeViewModel::class.java]
    }

    private fun observeChallenge() {
        viewChallengeViewModel.challenge.observe(viewLifecycleOwner) { challenge ->
            binding.progressBar.visibility = View.GONE

            if (challenge != null) {
                binding.viewChallengeTitle.text = challenge.title
                binding.viewChallengeConcept.text = "Concept: ${challenge.concept}"
                binding.viewChallengeConstraint.text = "Constraint: ${challenge.art_constraint}"

                // Gestione description opzionale: se manca si nasconde la view
                if (challenge.description.isNullOrBlank()) {
                    binding.viewChallengeDescription.visibility = View.GONE
                } else {
                    binding.viewChallengeDescription.visibility = View.VISIBLE
                    binding.viewChallengeDescription.text = challenge.description
                }

                binding.viewChallengeAuthorUsername.text = challenge.username

                // Autore principale
                binding.viewChallengeAuthorUsername.text = challenge.username
                Glide.with(binding.viewChallengeAuthorImage.context)
                    .load(challenge.profile_pic ?: R.drawable.ic_account_circle)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .circleCrop()
                    .into(binding.viewChallengeAuthorImage)

                // Se forkata → mostra autore originale
                if (challenge.parent_id != null) {
                    binding.viewChallengeForkedAuthorContainer.visibility = View.VISIBLE
                    binding.viewChallengeForkedAuthorUsername.text =
                        challenge.parent_username ?: "Unknown"
                    Glide.with(binding.viewChallengeForkedAuthorImage.context)
                        .load(challenge.parent_profile_pic ?: R.drawable.ic_account_circle)
                        .placeholder(R.drawable.ic_account_circle)
                        .error(R.drawable.ic_account_circle)
                        .circleCrop()
                        .into(binding.viewChallengeForkedAuthorImage)

                    // click su autore originale → profilo
                    binding.viewChallengeForkedAuthorContainer.setOnClickListener {
                        val action = ViewChallengeFragmentDirections
                            .actionViewChallengeFragmentToOtherUserProfileFragment(
                                challenge.parent_user_id ?: ""
                            )
                        findNavController().navigate(action)
                    }
                } else {
                    binding.viewChallengeForkedAuthorContainer.visibility = View.GONE
                }

                // Immagine Challenge
                val imageUrl = challenge.result_pic?.takeIf { it.isNotBlank() }
                Glide.with(binding.viewChallengeImage.context)
                    .load(imageUrl ?: R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.viewChallengeImage)
            } else {
                Toast.makeText(requireContext(), "Challenge not found", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun observeAuthorVisibility() {
        viewChallengeViewModel.isCurrentUserAuthor.observe(viewLifecycleOwner) { isAuthor ->
            if (isAuthor) {
                binding.viewChallengeEditButton.visibility = View.VISIBLE
                binding.viewChallengeForkButton.visibility = View.GONE
            } else {
                binding.viewChallengeEditButton.visibility = View.GONE
                binding.viewChallengeForkButton.visibility = View.VISIBLE

                // Naviga al profilo dell’autore principale
                binding.viewChallengeAuthorContainer.setOnClickListener {
                    val action = ViewChallengeFragmentDirections
                        .actionViewChallengeFragmentToOtherUserProfileFragment(
                            viewChallengeViewModel.challenge.value?.user_id ?: ""
                        )
                    findNavController().navigate(action)
                }

                // Fork: apre AddChallengeFragment con campi precompilati
                binding.viewChallengeForkButton.setOnClickListener {
                    val challenge = viewChallengeViewModel.challenge.value
                    challenge?.let {
                        val action = ViewChallengeFragmentDirections
                            .actionViewChallengeFragmentToAddChallengeFragment(
                                prefillTitle = it.title,
                                prefillConcept = it.concept,
                                prefillConstraint = it.art_constraint,
                                parentChallengeId = it.id
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }

        binding.viewChallengeEditButton.setOnClickListener {
            val action = ViewChallengeFragmentDirections
                .actionViewChallengeFragmentToEditChallengeFragment(challengeId)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

