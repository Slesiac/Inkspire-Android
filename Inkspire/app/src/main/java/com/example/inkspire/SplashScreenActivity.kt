package com.example.inkspire

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.inkspire.factory.AuthViewModelFactory
import com.example.inkspire.repository.AuthRepository
import com.example.inkspire.supabase.SupabaseManager
import com.example.inkspire.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // ViewModel setup
        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository())
        )[AuthViewModel::class.java]

        lifecycleScope.launch {
            SupabaseManager.auth.awaitInitialization()

            // Navigazione in base allo stato utente
            if (authViewModel.isLoggedIn()) {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
            }
            finish()
        }

    }
}