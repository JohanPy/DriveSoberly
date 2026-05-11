package com.johanpy.drivesoberly

import androidx.test.espresso.Espresso.onData
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
import com.johanpy.drivesoberly.ui.MainActivity
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HappyPathUITest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testHappyPathAddDrink() {
        dismissDisclaimerIfShown()

        // We land on DrinkerFragment because it's first launch
        onView(withId(R.id.editTextWeight)).perform(clearText(), typeText("80"), closeSoftKeyboard())
        onView(withId(R.id.radioMale)).perform(click())
        onView(withId(R.id.buttonValidateDrinker)).perform(click())

        // We should be on DriveFragment
        onView(withId(R.id.buttonAddDrink)).perform(click())

        // AddDrinkFragment: select an existing preset, then validate ingestion.
        onView(withId(R.id.imageViewPresetDrinkIcon)).perform(click())
        onView(withId(R.id.buttonValidateNewDrink)).perform(click())

        // Added a default preset, we should be back on DriveFragment
        // The alcohol rate view should be visible indicating > 0 BAC.
        onView(withId(R.id.textViewAlcoholRate)).check(matches(isDisplayed()))

        // Ensure that label explicit text is also present
        onView(withId(R.id.textViewDriveStatusLabel)).check(matches(isDisplayed()))
    }

    @Test
    fun testCustomCountryLimitAndDriveStatusFlow() {
        dismissDisclaimerIfShown()

        // Initialize profile.
        onView(withId(R.id.editTextWeight)).perform(clearText(), typeText("82"), closeSoftKeyboard())
        onView(withId(R.id.radioMale)).perform(click())
        onView(withId(R.id.buttonValidateDrinker)).perform(click())

        // Open profile/drinker screen from drive screen.
        onView(withId(R.id.buttonToDrinker)).perform(click())

        // Select OTHER country to enable custom limit editing.
        onView(withId(R.id.spinnerCountry)).perform(click())
        onData(
            allOf(
                `is`(instanceOf(String::class.java)),
                containsString("OTHER"),
            ),
        ).perform(click())

        // Update custom legal limit and validate profile.
        onView(withId(R.id.editTextCurrentLimit)).perform(clearText(), typeText("0.10"), closeSoftKeyboard())
        onView(withId(R.id.buttonValidateDrinker)).perform(click())

        // Add a drink and verify status panel updates are visible.
        onView(withId(R.id.buttonAddDrink)).perform(click())
        onView(withId(R.id.imageViewPresetDrinkIcon)).perform(click())
        onView(withId(R.id.buttonValidateNewDrink)).perform(click())

        onView(withId(R.id.textViewAlcoholRate)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDriveStatusLabel)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewProjectionSober)).check(matches(isDisplayed()))
    }

    private fun dismissDisclaimerIfShown() {
        runCatching {
            onView(withText(R.string.disclaimer_accept)).perform(click())
        }
    }
}
