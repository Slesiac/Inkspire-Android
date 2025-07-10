package com.example.inkspire

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.inkspire.databinding.ActivitySignUpBinding
import com.example.inkspire.factory.AuthViewModelFactory
import com.example.inkspire.repository.AuthRepository
import com.example.inkspire.viewmodel.AuthViewModel
import androidx.activity.OnBackPressedCallback

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()

        // Callback moderna per intercettare il tasto back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                finish()
            }
        })

        authViewModel.messageEvent.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        binding.signUpButton.setOnClickListener {
            val email = binding.signUpEmail.text.toString()
            val password = binding.signUpPassword.text.toString()
            val username = binding.signUpUsername.text.toString()

            if (email.isNotBlank() && password.isNotBlank() && username.isNotBlank()) {
                authViewModel.signUp(email, password, username) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.toLoginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupViewModel() {
        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository())
        )[AuthViewModel::class.java]
    }
}


/*
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.inkspire.databinding.ActivitySignUpBinding
import com.example.inkspire.supabase.DatabaseHelper

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set the binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        //Set Intent on Sign Up Text View
        binding.signUpButton.setOnClickListener {
            val signUpUsername = binding.signUpUsername.text.toString()
            val signUpPassword = binding.signUpPassword.text.toString()
            signUpDatabase(signUpUsername, signUpPassword)
        }

        //Set Intent on Login Text View
        binding.toLoginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java) //Another way of writing Intents
            startActivity(intent)
            finish()
        }
    }

    //Function that will take username and password from us
    private fun signUpDatabase(username: String, password: String) {
        val insertedRowId = databaseHelper.insertUser(username, password)
        if(insertedRowId != -1L) { // - 1 Long, basically "If task is successful"
            Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() //clears previous activity
        } else {
            Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show()
        }
    }

}
*/


/*
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.inkspire.databinding.ActivitySignUpBinding

/*SignUp Activity code steps
* 1- Set a binding
* 2- Initialize Supabase Auth Client
* 3- Set a Supabase Auth method
* 4- Set Intents on Text Views*/

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set a binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Get Supabase Client Instance
        firebaseAuth = FirebaseAuth.getInstance()

        //Set Supabase Auth Method
        binding.signUpButton.setOnClickListener {
            val email = binding.signUpEmail.text.toString()
            val password = binding.signUpPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish() //clears previous activity
                    } else {
                        Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show()
            }
        }

        //Set Intent on Login Text View
        binding.toLoginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)) //Another way of writing Intents
            finish()
        }

    }
}
*/