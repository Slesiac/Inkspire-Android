package com.example.inkspire.fragments

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.inkspire.R
import com.example.inkspire.adapter.ChallengeAdapter
import com.example.inkspire.databinding.FragmentHomeBinding
import com.example.inkspire.factory.AuthViewModelFactory
import com.example.inkspire.factory.HomeViewModelFactory
import com.example.inkspire.model.ChallengeVW
import com.example.inkspire.repository.AuthRepository
import com.example.inkspire.repository.ChallengeRepository
import com.example.inkspire.viewmodel.AuthViewModel
import com.example.inkspire.viewmodel.HomeViewModel

class HomeFragment : Fragment(R.layout.fragment_home),
    SearchView.OnQueryTextListener, MenuProvider {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupViewModels()
        setupRecyclerView()
        observeChallenges()
        observeLoading()
        homeViewModel.getAllChallenges()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmationDialog()
                }
            }
        )
    }

    private fun setupViewModels() {
        homeViewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(ChallengeRepository())
        )[HomeViewModel::class.java]

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository())
        )[AuthViewModel::class.java]
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter { selectedChallenge ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToViewChallengeFragment(selectedChallenge.id)
            view?.findNavController()?.navigate(action)
        }

        binding.challengeRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = challengeAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeChallenges() {
        homeViewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.submitList(challenges)
            updateUI(challenges)
        }
    }

    private fun updateUI(challenges: List<ChallengeVW>) {
        binding.emptyChallengesImage.visibility = if (challenges.isEmpty()) View.VISIBLE else View.GONE
        binding.challengeRecyclerView.visibility = if (challenges.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun observeLoading() {
        homeViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.challengeRecyclerView.visibility = View.GONE
                binding.emptyChallengesImage.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { homeViewModel.searchChallenges(it) }
        return true
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home_menu, menu)

        val searchMenuItem = menu.findItem(R.id.searchMenu)
        val searchView = searchMenuItem.actionView as SearchView

        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(this)

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Nessun altro item da nascondere
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Ricarica TUTTE le challenge quando chiudi la search
                homeViewModel.getAllChallenges()
                return true
            }
        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Exit App")
            setMessage("Are you sure you want to exit?")
            setPositiveButton("Yes") { _, _ -> requireActivity().finish() }
            setNegativeButton("Cancel", null)
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}