package com.example.inkspire

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.inkspire.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera NavController dal NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        // Configura AppBarConfiguration con i fragment di primo livello (BottomNav)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.userListFragment,
                R.id.userProfileFragment
            )
        )

        // Collega l'ActionBar (Toolbar) con il NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Collega il BottomNavigationView con il NavController
        binding.bottomNavView.setupWithNavController(navController)

        // Navigazione tramite FloatingActionButton
        binding.addChallengeFab.setOnClickListener {
            navController.navigate(R.id.addChallengeFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.addChallengeFragment,
                R.id.editChallengeFragment,
                R.id.viewChallengeFragment,
                R.id.editUserProfileFragment,
                R.id.otherUserProfileFragment -> hideBottomUI()
                else -> showBottomUI()
            }
        }

    }

    private fun hideBottomUI() {
        binding.bottomAppBar.visibility = View.GONE
        binding.bottomNavView.visibility = View.GONE
        binding.addChallengeFab.visibility = View.GONE
    }

    private fun showBottomUI() {
        binding.bottomAppBar.visibility = View.VISIBLE
        binding.bottomNavView.visibility = View.VISIBLE
        binding.addChallengeFab.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment)
            .navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}