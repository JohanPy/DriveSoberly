package com.johanpy.drivesoberly

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.johanpy.drivesoberly.ui.MainActivity
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HappyPathUITest {
    @Test
    fun testMainActivityLaunches() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        } finally {
            scenario.close()
        }
    }
}
