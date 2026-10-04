package com.smartcampus.client

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Confirms that the instrumented test is connected to this application.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.smartcampus.client", appContext.packageName)
    }
}