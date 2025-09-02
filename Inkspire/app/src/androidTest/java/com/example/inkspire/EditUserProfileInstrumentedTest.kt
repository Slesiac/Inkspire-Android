package com.example.inkspire

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.inkspire.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditUserProfileInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun ensureLoggedIn() {
        runBlocking {
            val repo = AuthRepository()
            repo.login("ale.cap@test.com", "alecap")
        }
    }

    @Test
    fun editUserProfile_updatesBio() {
        // Vai al profilo utente tramite bottom navigation
        onView(withId(R.id.userProfileFragment)).perform(click())

        Thread.sleep(2000)

        onView(withId(R.id.editProfileButton)).perform(click())

        Thread.sleep(2000)

        val newBio = "Espresso updated bio"
        onView(withId(R.id.editProfileBio))
            .perform(clearText(), typeText(newBio), closeSoftKeyboard())

        onView(withId(R.id.saveProfileButton)).perform(click())

        Thread.sleep(2000)

        // Verifica che la nuova bio sia visibile nel fragment del profilo
        onView(withId(R.id.profileBio)).check(matches(withText(newBio)))
    }
}