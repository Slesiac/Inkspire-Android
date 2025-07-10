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

//        binding.addNoteFab.setOnClickListener {
//            val action = HomeFragmentDirections.actionHomeFragmentToAddNoteFragment()
//            it.findNavController().navigate(action)
//        }

/*
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChallengeAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel
        val factory = HomeViewModelFactory(ChallengeRepository())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Adapter + navigazione a ViewChallengeFragment
        adapter = ChallengeAdapter { selectedChallenge ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToViewChallengeFragment(selectedChallenge)
            findNavController().navigate(action)
        }

        binding.challengeRecyclerView.adapter = adapter
        binding.challengeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Osserva dati
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            if (challenges.isNullOrEmpty()) {
                binding.challengeRecyclerView.visibility = View.GONE
                binding.emptyChallengesImage.visibility = View.VISIBLE
            } else {
                adapter.submitList(challenges)
                binding.challengeRecyclerView.visibility = View.VISIBLE
                binding.emptyChallengesImage.visibility = View.GONE
            }
        }

        viewModel.loadAllChallenges()

        // Aggiungi il MenuProvider al ciclo di vita
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)

                // Search
                val searchItem = menu.findItem(R.id.searchMenu)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "Search challenges..."

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        viewModel.searchChallenges(query.orEmpty())
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.searchChallenges(newText.orEmpty())
                        return true
                    }
                })
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
        }, viewLifecycleOwner)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    AuthRepository().logout()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
*/

/*
class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
), SearchView.OnQueryTextListener, MenuProvider {

    private lateinit var challengeViewModel: ChallengeViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupViewModels()
        setupRecyclerView()
        observePublicChallenges()
        observeNavigationResult()
        setupBackPressHandling()
    }

    private fun setupViewModels() {
        challengeViewModel = ViewModelProvider(
            this,
            ChallengeViewModelFactory(requireActivity().application, ChallengeRepository(), AuthRepository())
        )[ChallengeViewModel::class.java]

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository())
        )[AuthViewModel::class.java]
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(onChallengeClick = { selected ->
            val action = HomeFragmentDirections.actionHomeFragmentToViewChallengeFragment(selected)
            findNavController().navigate(action)
        })

        binding.challengeRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = challengeAdapter
            setHasFixedSize(true)
        }
    }

    private fun observePublicChallenges() {
        challengeViewModel.publicChallenges.observe(viewLifecycleOwner) { challenges ->
            challengeAdapter.submitList(challenges)
            updateEmptyState(challenges)
        }
    }

    private fun updateEmptyState(challenges: List<ChallengeUser>) {
        val isEmpty = challenges.isEmpty()
        binding.emptyChallengesImage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.challengeRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun observeNavigationResult() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("challenge_updated")
            ?.observe(viewLifecycleOwner) { updated ->
                if (updated == true) {
                    challengeViewModel.loadPublicChallenges()
                    savedStateHandle.remove<Boolean>("challenge_updated")
                }
            }
    }

    private fun setupBackPressHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes") { _, _ -> requireActivity().finish() }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        )
    }

    // --- Search bar ---
    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            challengeViewModel.searchPublicChallenges(it)
        }
        return true
    }

    // --- Menu ---
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home_menu, menu)

        val searchItem = menu.findItem(R.id.searchMenu)
        val logoutItem = menu.findItem(R.id.logoutMenu)

        val searchView = searchItem.actionView as SearchView
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(this)

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                logoutItem.isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                logoutItem.isVisible = true
                challengeViewModel.loadPublicChallenges()
                return true
            }
        })
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
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                authViewModel.logout {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

 */