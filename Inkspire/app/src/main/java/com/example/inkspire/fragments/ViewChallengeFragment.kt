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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mostra progress bar e nesconde il contenuto per evitare placeholder flicker
        binding.progressBar.visibility = View.VISIBLE
        binding.viewChallengeContentGroup.visibility = View.GONE

        // Evita blink dei blocchi opzionali prima del caricamento dati
        binding.viewChallengeDescription.visibility = View.GONE
        binding.dividerBeforeForkSection.visibility = View.GONE
        binding.viewChallengeForkedChallengeContainer.visibility = View.GONE
        binding.viewChallengeForkedAuthorContainer.visibility = View.GONE

        // Recupera l'ID passato via Safe Args
        challengeId = arguments?.let {
            ViewChallengeFragmentArgs.fromBundle(it).challengeId
        } ?: -1

        setupViewModel()
        observeChallenge()
        observeAuthorVisibility()

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
            if (challenge != null) {
                // Dati base
                binding.viewChallengeTitle.text = challenge.title
                binding.viewChallengeConcept.text = "Concept: ${challenge.concept}"
                binding.viewChallengeConstraint.text = "Constraint: ${challenge.art_constraint}"

                // Description: nascondi se vuota
                if (challenge.description.isNullOrBlank()) {
                    binding.viewChallengeDescription.visibility = View.GONE
                } else {
                    binding.viewChallengeDescription.visibility = View.VISIBLE
                    binding.viewChallengeDescription.text = challenge.description
                }

                // Autore della challenge corrente
                binding.viewChallengeAuthorUsername.text = challenge.username
                Glide.with(binding.viewChallengeAuthorImage.context)
                    .load(challenge.profile_pic ?: R.drawable.ic_account_circle)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .circleCrop()
                    .into(binding.viewChallengeAuthorImage)

                // Immagine Challenge
                val imageUrl = challenge.result_pic?.takeIf { it.isNotBlank() }
                Glide.with(binding.viewChallengeImage.context)
                    .load(imageUrl ?: R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.viewChallengeImage)

                // Reset preventivo blocchi opzionali
                binding.dividerBeforeForkSection.visibility = View.GONE
                binding.viewChallengeForkedChallengeContainer.visibility = View.GONE
                binding.viewChallengeForkedAuthorContainer.visibility = View.GONE

                // Se la challenge è un fork mostra divider + i due container
                if (challenge.parent_id != null) {
                    binding.dividerBeforeForkSection.visibility = View.VISIBLE

                    // 1) "Forked from: <titolo parent>" click apre la view della challenge parent
                    binding.viewChallengeForkedChallengeContainer.visibility = View.VISIBLE
                    binding.viewChallengeForkedChallengeTitle.text =
                        challenge.parent_title ?: "Untitled"

                    binding.viewChallengeForkedChallengeContainer.setOnClickListener {
                        val parentId = challenge.parent_id
                        val action = ViewChallengeFragmentDirections
                            .actionViewChallengeFragmentSelf(parentId)
                        findNavController().navigate(action)
                    }

                    // 2) "Created by: <username parent>" click apre il profilo dell'autore parent
                    binding.viewChallengeForkedAuthorContainer.visibility = View.VISIBLE
                    binding.viewChallengeForkedAuthorUsername.text =
                        challenge.parent_username ?: "Unknown"

                    Glide.with(binding.viewChallengeForkedAuthorImage.context)
                        .load(challenge.parent_profile_pic ?: R.drawable.ic_account_circle)
                        .placeholder(R.drawable.ic_account_circle)
                        .error(R.drawable.ic_account_circle)
                        .circleCrop()
                        .into(binding.viewChallengeForkedAuthorImage)

                    binding.viewChallengeForkedAuthorContainer.setOnClickListener {
                        val parentUserId = challenge.parent_user_id ?: return@setOnClickListener
                        val action = ViewChallengeFragmentDirections
                            .actionViewChallengeFragmentToOtherUserProfileFragment(parentUserId)
                        findNavController().navigate(action)
                    }
                }

                // Fine caricamento: mostra contenuti principali
                binding.progressBar.visibility = View.GONE
                binding.viewChallengeContentGroup.visibility = View.VISIBLE

            } else {
                binding.progressBar.visibility = View.GONE
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
                // L'autore corrente non deve navigare al proprio profilo da qui
                binding.viewChallengeAuthorContainer.setOnClickListener(null)
                binding.viewChallengeAuthorContainer.isClickable = false
            } else {
                binding.viewChallengeEditButton.visibility = View.GONE
                binding.viewChallengeForkButton.visibility = View.VISIBLE

                // Naviga al profilo dell’autore della challenge corrente
                binding.viewChallengeAuthorContainer.isClickable = true
                binding.viewChallengeAuthorContainer.setOnClickListener {
                    val action = ViewChallengeFragmentDirections
                        .actionViewChallengeFragmentToOtherUserProfileFragment(
                            viewChallengeViewModel.challenge.value?.user_id ?: ""
                        )
                    findNavController().navigate(action)
                }

                // Fork: apre AddChallengeFragment con campi precompilati
                binding.viewChallengeForkButton.setOnClickListener {
                    val c = viewChallengeViewModel.challenge.value ?: return@setOnClickListener
                    val action = ViewChallengeFragmentDirections
                        .actionViewChallengeFragmentToAddChallengeFragment(
                            prefillTitle = c.title,
                            prefillConcept = c.concept,
                            prefillConstraint = c.art_constraint,
                            parentChallengeId = c.id
                        )
                    findNavController().navigate(action)
                }
            }
        }

        // Edit (solo autore)
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

