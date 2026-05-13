package com.johanpy.drivesoberly

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.johanpy.drivesoberly.ui.MainActivity
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HappyPathUITest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun prepareDeviceForEspresso() {
        ensureDeviceInteractive()
        disableSystemAnimations()
    }

    @Test
    fun testHappyPathAddDrink() {
        dismissDisclaimerIfShown()
        ensureOnDrinkerScreen()

        onView(withId(R.id.editTextWeight)).perform(clearText(), typeText("80"), closeSoftKeyboard())
        onView(withId(R.id.radioMale)).perform(click())
        onView(withId(R.id.buttonValidateDrinker)).perform(click())

        // Wait for transition to DriveFragment
        Thread.sleep(500)
        onView(withId(R.id.buttonAddDrink)).check(matches(isDisplayed()))

        // We should be on DriveFragment
        onView(withId(R.id.buttonAddDrink)).perform(click())

        // Wait for transition to AddDrinkFragment
        Thread.sleep(500)
        onView(withId(R.id.listViewPresetDrinks)).check(matches(isDisplayed()))

        // AddDrinkFragment: select the first preset entry (position 1, because 0 is "add preset" button)
        onView(withId(R.id.listViewPresetDrinks)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()),
        )

        // Wait for potential UI update before validating
        Thread.sleep(300)
        onView(withId(R.id.buttonValidateNewDrink)).perform(click())

        // Wait for transition back to DriveFragment
        Thread.sleep(500)

        // Added a default preset, we should be back on DriveFragment
        // The alcohol rate view should be visible indicating > 0 BAC.
        onView(withId(R.id.textViewAlcoholRate)).check(matches(isDisplayed()))

        // Ensure that label explicit text is also present
        onView(withId(R.id.textViewDriveStatusLabel)).check(matches(isDisplayed()))
    }

    @Test
    fun testCustomCountryLimitAndDriveStatusFlow() {
        dismissDisclaimerIfShown()
        ensureOnDrinkerScreen()

        // Initialize profile.
        onView(withId(R.id.editTextWeight)).perform(clearText(), typeText("82"), closeSoftKeyboard())
        onView(withId(R.id.radioMale)).perform(click())
        onView(withId(R.id.buttonValidateDrinker)).perform(click())

        // Wait for transition to DriveFragment
        Thread.sleep(500)

        // Open profile/drinker screen from drive screen.
        onView(withId(R.id.buttonToDrinker)).perform(click())

        // Wait for transition to DrinkerFragment
        Thread.sleep(500)
        onView(withId(R.id.spinnerCountry)).check(matches(isDisplayed()))

        // Select OTHER country to enable custom limit editing.
        onView(withId(R.id.spinnerCountry)).perform(click())

        Thread.sleep(300)
        // Use the localized string for "OTHER" - from resources, this is "AUTRE" in French locales
        val otherCountryLabel =
            InstrumentationRegistry.getInstrumentation()
                .targetContext.getString(R.string.other)
        onData(
            allOf(
                `is`(instanceOf(String::class.java)),
                containsString(otherCountryLabel),
            ),
        ).perform(click())

        // Update custom legal limit and validate profile.
        Thread.sleep(300)
        onView(withId(R.id.editTextCurrentLimit)).perform(clearText(), typeText("0.10"), closeSoftKeyboard())
        onView(withId(R.id.buttonValidateDrinker)).perform(click())

        // Wait for transition back to DriveFragment
        Thread.sleep(500)

        // Verify drive screen is reachable and status panel is visible.
        onView(withId(R.id.buttonAddDrink)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewDriveStatusLabel)).check(matches(isDisplayed()))
    }

    private fun dismissDisclaimerIfShown() {
        runCatching {
            onView(withText(R.string.disclaimer_accept)).perform(click())
        }
    }

    private fun ensureOnDrinkerScreen() {
        val alreadyOnDrinker =
            runCatching {
                onView(withId(R.id.editTextWeight)).check(matches(isDisplayed()))
            }.isSuccess

        if (!alreadyOnDrinker) {
            runCatching {
                onView(withId(R.id.buttonToDrinker)).perform(click())
            }
            onView(withId(R.id.editTextWeight)).check(matches(isDisplayed()))
        }
    }

    private fun disableSystemAnimations() {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf(
            "settings put global window_animation_scale 0.0",
            "settings put global transition_animation_scale 0.0",
            "settings put global animator_duration_scale 0.0",
        ).forEach { command ->
            uiAutomation.executeShellCommand(command).close()
        }
    }

    private fun ensureDeviceInteractive() {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        listOf(
            "input keyevent KEYCODE_WAKEUP",
            "wm dismiss-keyguard",
            "svc power stayon true",
            "settings put system screen_off_timeout 2147483647",
        ).forEach { command ->
            uiAutomation.executeShellCommand(command).close()
        }
    }
}
