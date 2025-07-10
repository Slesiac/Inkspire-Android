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
                binding.viewChallengeDescription.text = challenge.description ?: "No description"

                binding.viewChallengeAuthorUsername.text = challenge.username

                // Immagine Challenge
                val imageUrl = challenge.result_pic?.takeIf { it.isNotBlank() }
                Glide.with(binding.viewChallengeImage.context)
                    .load(imageUrl ?: R.drawable.logo)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.viewChallengeImage)

                // Immagine autore
                Glide.with(binding.viewChallengeAuthorImage.context)
                    .load(challenge.profile_pic ?: R.drawable.ic_account_circle)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .circleCrop()
                    .into(binding.viewChallengeAuthorImage)
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
                binding.viewChallengeAuthorContainer.visibility = View.GONE
            } else {
                binding.viewChallengeEditButton.visibility = View.GONE
                binding.viewChallengeAuthorContainer.visibility = View.VISIBLE

                binding.viewChallengeAuthorContainer.setOnClickListener {
                    // Naviga al profilo in sola visualizzazione
                    val action = ViewChallengeFragmentDirections
                        .actionViewChallengeFragmentToOtherUserProfileFragment(
                            viewChallengeViewModel.challenge.value?.user_id ?: ""
                        )
                    findNavController().navigate(action)
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

/*
class ViewChallengeFragment : Fragment() {

    private var _binding: FragmentViewChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewChallengeViewModel
    private var challengeId: Int = -1
    private var currentChallenge: ChallengeVW? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Argomento da nav_graph
        challengeId = arguments?.let {
            ViewChallengeFragmentArgs.fromBundle(it).challengeId
        } ?: -1

        // ViewModel
        val factory = ViewChallengeViewModelFactory(ChallengeRepository(), UserRepository())
        viewModel = ViewModelProvider(this, factory)[ViewChallengeViewModel::class.java]

        // Osserva challenge caricata
        viewModel.challenge.observe(viewLifecycleOwner) { challengeUser ->
            currentChallenge = challengeUser
            bindChallengeToUI(challengeUser)
        }

        // Carica la challenge
        viewModel.loadChallenge(challengeId)

        // Pulsante modifica (solo se l’utente corrente è l’autore)
        viewModel.isCurrentUserAuthor.observe(viewLifecycleOwner) { isAuthor ->
            binding.viewChallengeEditButton.visibility = if (isAuthor) View.VISIBLE else View.GONE
        }

        binding.viewChallengeEditButton.setOnClickListener {
            currentChallenge?.let { challenge ->
                val action = ViewChallengeFragmentDirections
                    .actionViewChallengeFragmentToEditChallengeFragment(challenge.toChallenge())
                findNavController().navigate(action)
            }
        }

        // Pulsante autore → naviga al profilo (se vuoi supportarlo in futuro)
        binding.viewChallengeAuthorContainer.setOnClickListener {
            // Navigazione a UserProfileFragment se vuoi abilitarla
        }
    }

    private fun bindChallengeToUI(challenge: ChallengeVW) {
        binding.viewChallengeTitle.text = challenge.title
        binding.viewChallengeConcept.text = "Concept: ${challenge.concept}"
        binding.viewChallengeConstraint.text = "Constraint: ${challenge.art_constraint}"
        binding.viewChallengeDescription.text =
            if (!challenge.description.isNullOrBlank()) challenge.description else "No description provided"

        binding.viewChallengeAuthorUsername.text = challenge.user_profile.username

        // TODO: se usi Glide o Coil puoi caricare immagini reali
        // Glide.with(this).load(challenge.result_pic).into(binding.viewChallengeImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Conversione ChallengeUser → Challenge
    private fun ChallengeVW.toChallenge(): Challenge {
        return Challenge(
            id = id,
            user_profile_id = user_profile.id,
            title = title,
            concept = concept,
            art_constraint = art_constraint,
            description = description,
            result_pic = result_pic,
            inserted_at = inserted_at,
            updated_at = updated_at
        )
    }
}

 */


/*
class ViewChallengeFragment : BaseFragment<FragmentViewChallengeBinding>(FragmentViewChallengeBinding::inflate) {

    private val args: ViewChallengeFragmentArgs by navArgs()
    private lateinit var challenge: ChallengeUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challenge = args.challengeUser

        populateChallengeDetails()
        configureEditButton()
    }

    private fun populateChallengeDetails() = with(binding) {
        viewChallengeTitle.text = challenge.title
        viewChallengeConcept.text = challenge.concept
        viewChallengeConstraint.text = challenge.art_constraint
        viewChallengeDescription.text = challenge.description ?: "No description"
        //viewChallengeDate.text = challenge.inserted_at?.substring(0, 10) ?: "Unknown"

        Glide.with(requireContext())
            .load(challenge.result_pic ?: R.drawable.logo)
            .into(viewChallengeImage)

        viewChallengeAuthorUsername.text = challenge.user_profile?.username ?: "Unknown"
    }

    private fun configureEditButton() {
        val currentUserId = SupabaseManager.auth.currentUserOrNull()?.id
        val isOwner = challenge.user_profile_id == currentUserId

        binding.viewChallengeEditButton.apply {
            visibility = if (isOwner) View.VISIBLE else View.GONE
            setOnClickListener {
                val action = ViewChallengeFragmentDirections
                    .actionViewChallengeFragmentToEditChallengeFragment(challenge.toChallenge())
                findNavController().navigate(action)
            }
        }
    }

    // Utility: trasforma ChallengeUser in Challenge (senza user_profile)
    private fun ChallengeUser.toChallenge(): Challenge {
        return Challenge(
            id = id,
            user_profile_id = user_profile_id,
            title = title,
            concept = concept,
            art_constraint = art_constraint,
            description = description,
            result_pic = result_pic,
            inserted_at = inserted_at,
            updated_at = updated_at
        )
    }
}

 */

