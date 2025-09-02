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
class AddChallengeInstrumentedTest {

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
    fun createChallenge_displaysInRecyclerView() {
        onView(withId(R.id.addChallengeFab)).perform(click())

        onView(withId(R.id.addChallengeTitle))
            .perform(typeText("Espresso Test Challenge"), closeSoftKeyboard())

        onView(withId(R.id.addChallengeConcept))
            .perform(typeText("Testing Concept"), closeSoftKeyboard())

        onView(withId(R.id.addChallengeConstraint))
            .perform(typeText("Use 3 colors"), closeSoftKeyboard())

        onView(withId(R.id.addChallengeButton)).perform(click())

        Thread.sleep(3000)

        onView(withId(R.id.challengeRecyclerView))
            .check(matches(hasDescendant(withText("Espresso Test Challenge"))))
    }
}