package com.example.inkspire

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginInstrumentedTest {

    @Test
    fun loginWithValidCredentials_opensHome() {

        // Avvia LoginActivity
        ActivityScenario.launch(LoginActivity::class.java)

        onView(withId(R.id.loginEmail))
            .perform(typeText("ale.cap@test.com"), closeSoftKeyboard())

        onView(withId(R.id.loginPassword))
            .perform(typeText("alecap"), closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())

        // Attesa cambio Activity + caricamento dati
        Thread.sleep(5000)

        // Verifica che la RecyclerView della Home sia visibile
        onView(withId(R.id.challengeRecyclerView))
            .check(matches(isDisplayed()))
    }
}