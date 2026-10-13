package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.util.DateFormatter
import com.example.core.util.GsmSegmentCounter
import com.example.core.util.OtpDetector
import com.example.core.util.SpamDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim", appName)
    }

    @Test
    fun testOtpDetection() {
        val message1 = "Your verification code is 482910. Do not share it with anyone."
        val otp1 = OtpDetector.extractOtp(message1)
        assertEquals("482910", otp1)

        val message2 = "G-938210 is your Google verification code."
        val otp2 = OtpDetector.extractOtp(message2)
        assertEquals("938210", otp2)

        val normalMessage = "Hey, are we still meeting today at 5pm?"
        val otp3 = OtpDetector.extractOtp(normalMessage)
        assertNull(otp3)
    }

    @Test
    fun testSpamDetection() {
        val spam = "Congratulations! You have won cash prize. Claim your prize immediately."
        assertTrue(SpamDetector.isSuspectedSpam("+1234567890", spam))

        val regular = "Can you pick up milk on the way home?"
        assertTrue(!SpamDetector.isSuspectedSpam("+1234567890", regular))
    }

    @Test
    fun testGsmSegmentCounter() {
        val shortSms = "Hello world"
        val result1 = GsmSegmentCounter.calculate(shortSms)
        assertEquals(1, result1.segmentCount)
        assertEquals(false, result1.isUcs2)

        val unicodeSms = "Hello 🚀 world"
        val result2 = GsmSegmentCounter.calculate(unicodeSms)
        assertEquals(true, result2.isUcs2)
    }

    @Test
    fun testDateFormatter() {
        val now = System.currentTimeMillis()
        assertEquals("Now", DateFormatter.formatRelativeTime(now))
    }
}
