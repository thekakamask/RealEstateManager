package com.dcac.realestatemanager.utilsTest

import com.dcac.realestatemanager.utils.settingsUtils.LocaleHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.Locale

@RunWith(JUnit4::class)
class LocaleHelperTest {
    private val context = androidx.test.platform.app.InstrumentationRegistry
        .getInstrumentation()
        .targetContext

    @Test
    fun updateLocale_changesLocaleCorrectly() {
        val updatedContext = LocaleHelper.updateLocale(context, "fr")

        val locale = updatedContext.resources.configuration.locales[0]

        assertEquals("fr", locale.language)
    }

    @Test
    fun updateLocale_withEnglish_setsEnglishLocale() {
        val updatedContext = LocaleHelper.updateLocale(context, "en")

        val locale = updatedContext.resources.configuration.locales[0]

        assertEquals("en", locale.language)
    }

    @After
    fun resetLocale() {
        Locale.setDefault(Locale.getDefault())
    }
}