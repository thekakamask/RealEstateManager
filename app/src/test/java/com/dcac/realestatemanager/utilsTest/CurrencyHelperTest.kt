package com.dcac.realestatemanager.utilsTest

import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyHelperTest {

    @Test
    fun getPropertyCreationStep5Unit_returnsCorrectValues() {
        val euro = CurrencyHelper.getPropertyCreationStep5Unit("EUR")
        val dollar = CurrencyHelper.getPropertyCreationStep5Unit("USD")

        assertEquals(R.string.property_creation_step_5_unit_euro, euro)
        assertEquals(R.string.property_creation_step_5_unit_dollars, dollar)
    }

    @Test
    fun getPropertyListScreenPropertyPriceText_returnsCorrectValues() {
        val euro = CurrencyHelper.getPropertyListScreenPropertyPriceText("EUR")
        val dollar = CurrencyHelper.getPropertyListScreenPropertyPriceText("USD")

        assertEquals(R.string.property_list_screen_property_price_text_euro, euro)
        assertEquals(R.string.property_list_screen_property_price_text_dollars, dollar)
    }

    @Test
    fun getGoogleMapScreenMarkerSnippet_returnsCorrectValues() {
        val euro = CurrencyHelper.getGoogleMapScreenMarkerSnippet("EUR")
        val dollar = CurrencyHelper.getGoogleMapScreenMarkerSnippet("USD")

        assertEquals(R.string.google_map_screen_marker_snippet_euro, euro)
        assertEquals(R.string.google_map_screen_marker_snippet_dollars, dollar)
    }

    @Test
    fun convertDollarToEuro_returnsCorrectValue() {
        val result = CurrencyHelper.convertDollarToEuro(100)

        assertEquals(81, result)
    }

    @Test
    fun convertEuroToDollar_returnsCorrectValue() {
        val result = CurrencyHelper.convertEuroToDollar(100)

        assertEquals(123, result)
    }


    @Test
    fun convertDollarToEuro_withZero_returnsZero() {
        val result = CurrencyHelper.convertDollarToEuro(0)

        assertEquals(0, result)
    }

    @Test
    fun convertEuroToDollar_withZero_returnsZero() {
        val result = CurrencyHelper.convertEuroToDollar(0)

        assertEquals(0, result)
    }

}