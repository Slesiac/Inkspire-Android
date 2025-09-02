package com.example.inkspire

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewChallengeInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun openChallengeView_displaysContent() {

        Thread.sleep(2000)

        // Clicca sul primo elemento della RecyclerView
        onView(withId(R.id.challengeRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0, click()
                )
            )

        Thread.sleep(2000)

        // Verifica che il titolo della challenge sia visibile
        onView(withId(R.id.viewChallengeTitle))
            .check(matches(isDisplayed()))

        // Verifica anche che il concept sia mostrato
        onView(withId(R.id.viewChallengeConcept))
            .check(matches(isDisplayed()))
    }
}