package com.vaudibert.canidrive

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vaudibert.canidrive.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HappyPathUITest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testHappyPathAddDrink() {
        // App starts. Disclaimer shows up on first launch.
        onView(withText("I understand")).perform(click())

        // We land on DrinkerFragment because it's first launch
        onView(withId(R.id.editTextWeight)).perform(clearText(), typeText("80"), closeSoftKeyboard())
        onView(withId(R.id.radioMale)).perform(click())
        onView(withId(R.id.buttonValidateDrinker)).perform(click())

        // We should be on DriveFragment
        onView(withId(R.id.buttonAddDrink)).perform(click())

        // AddDrinkFragment
        onView(withId(R.id.buttonAddPreset)).perform(click())

        // Added a default preset, we should be back on DriveFragment
        // The alcohol rate view should be visible indicating > 0 BAC.
        onView(withId(R.id.textViewAlcoholRate)).check(matches(isDisplayed()))

        // Ensure that label explicit text is also present
        onView(withId(R.id.textViewDriveStatusLabel)).check(matches(isDisplayed()))
    }
}
