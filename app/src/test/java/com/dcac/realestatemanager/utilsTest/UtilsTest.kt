package com.dcac.realestatemanager.utilsTest

import com.dcac.realestatemanager.utils.Utils
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {

    @Test
    fun getTodayDate_returnsCorrectFormat() {
        val result = Utils.getTodayDate()

        val regex = Regex("\\d{2}/\\d{2}/\\d{4}")
        assert(regex.matches(result))
    }

    @Test
    fun getTodayDateLegacy_returnsCorrectFormat() {
        val result = Utils.getTodayDateLegacy()

        val regex = Regex("\\d{4}/\\d{2}/\\d{2}")
        assert(regex.matches(result))
    }

    @Test
    fun calculatePricePerSquareMeter_returnsCorrectValue() {
        val result = Utils.calculatePricePerSquareMeter(100000, 100)

        assertEquals(1000, result)
    }

    @Test
    fun calculatePricePerSquareMeter_withZeroSurface_returnsZero() {
        val result = Utils.calculatePricePerSquareMeter(100000, 0)

        assertEquals(0, result)
    }

    @Test
    fun getIconForPropertyType_returnsCorrectIcon() {
        val result = Utils.getIconForPropertyType("House")

        assertEquals(com.dcac.realestatemanager.R.drawable.house_24px, result)
    }

    @Test
    fun getIconForPoiType_returnsCorrectIcon() {
        val result = Utils.getIconForPoiType("School")

        assertEquals(com.dcac.realestatemanager.R.drawable.school_24px, result)
    }

    @Test
    fun getColorForPoiType_returnsCorrectColor() {
        val result = Utils.getColorForPoiType("School")

        assertEquals(android.graphics.Color.CYAN, result)
    }

    @Test
    fun normalize_trimsLowercasesAndRemovesExtraSpaces() {
        val input = "  Hello   World  "

        val result = Utils.run { input.normalize() }

        assertEquals("hello world", result)
    }

}
