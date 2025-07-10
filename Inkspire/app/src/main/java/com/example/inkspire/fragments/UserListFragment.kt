package com.example.inkspire.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inkspire.R
import com.example.inkspire.adapter.UserAdapter
import com.example.inkspire.databinding.FragmentUserListBinding
import com.example.inkspire.factory.AuthViewModelFactory
import com.example.inkspire.factory.UserListViewModelFactory
import com.example.inkspire.model.UserProfileVW
import com.example.inkspire.repository.AuthRepository
import com.example.inkspire.repository.UserRepository
import com.example.inkspire.viewmodel.AuthViewModel
import com.example.inkspire.viewmodel.UserListViewModel

class UserListFragment : Fragment(R.layout.fragment_user_list),
    SearchView.OnQueryTextListener, MenuProvider {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var userListViewModel: UserListViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupViewModels()
        setupRecyclerView()
        observeUsers()
        observeLoading()
        userListViewModel.getAllUsers()
    }

    private fun setupViewModels() {
        userListViewModel = ViewModelProvider(
            this,
            UserListViewModelFactory(UserRepository())
        )[UserListViewModel::class.java]

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository())
        )[AuthViewModel::class.java]
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { selectedUser ->
            val action = UserListFragmentDirections
                .actionUserListFragmentToOtherUserProfileFragment(selectedUser.id)
            view?.findNavController()?.navigate(action)
        }

        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeUsers() {
        userListViewModel.users.observe(viewLifecycleOwner) { allUsers ->
            // Filtra via l'utente corrente
            val currentUserId = UserRepository().getCurrentUserId()
            val filteredUsers = allUsers.filter { it.id.toString() != currentUserId }
            userAdapter.submitList(filteredUsers)

            updateUI(filteredUsers)
        }
    }

    private fun updateUI(users: List<UserProfileVW>) {
        binding.emptyUsersImage.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
        binding.userRecyclerView.visibility = if (users.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun observeLoading() {
        userListViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.userRecyclerView.visibility = View.GONE
                binding.emptyUsersImage.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let { userListViewModel.searchUsers(it) }
        return true
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.user_list_menu, menu)

        val searchMenuItem = menu.findItem(R.id.searchMenu)
        val searchView = searchMenuItem.actionView as SearchView

        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(this)

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                userListViewModel.getAllUsers()
                return true
            }
        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}