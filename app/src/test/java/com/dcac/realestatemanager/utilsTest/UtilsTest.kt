package com.dcac.realestatemanager.utilsTest

import com.dcac.realestatemanager.utils.Utils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UtilsTest {

    @Test
    fun convertDollarToEuro_returnsCorrectValue() {
        // $100 => €81.2 => 81 (rounded)
        val result = Utils.convertDollarToEuro(100)
        assertEquals(81, result)
    }

    @Test
    fun convertEuroToDollar_returnsCorrectValue() {
        // €100 => $123.15 => 123 (rounded)
        val result = Utils.convertEuroToDollar(100)
        assertEquals(123, result)
    }

    @Test
    fun getTodayDate_returnsFormattedDate_ddMMyyyy() {
        val expected = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val result = Utils.getTodayDate()
        assertEquals(expected, result)
    }

    @Test
    fun getTodayDateLegacy_returnsFormattedDate_yyyyMMdd() {
        val expected = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        val result = Utils.getTodayDateLegacy()
        assertEquals(expected, result)
    }
}