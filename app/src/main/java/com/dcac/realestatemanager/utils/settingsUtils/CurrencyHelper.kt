package com.dcac.realestatemanager.utils.settingsUtils

import androidx.compose.runtime.compositionLocalOf
import com.dcac.realestatemanager.R
import kotlin.math.roundToInt

object CurrencyHelper {


    val LocalCurrency = compositionLocalOf<String> {
        error("No currency provided")
    }

    private const val EUR = "EUR"


    fun getPropertyCreationStep5Unit(currency: String): Int {
        return if (currency == EUR) {
            R.string.property_creation_step_5_unit_euro
        } else {
            R.string.property_creation_step_5_unit_dollars
        }
    }

    fun getPropertyCreationStep8PriceText(currency: String): Int {
        return if (currency == EUR) {
            R.string.property_creation_step_8_price_text_euro
        } else {
            R.string.property_creation_step_8_price_text_dollars
        }
    }

    fun getPropertyListScreenPropertyPriceText(currency: String): Int {
        return if (currency == EUR) {
            R.string.property_list_screen_property_price_text_euro
        } else {
            R.string.property_list_screen_property_price_text_dollars
        }
    }

    fun getPropertyListScreenPropertyPriceSquareText(currency: String): Int {
        return if (currency == EUR) {
            R.string.property_list_screen_property_price_square_text_euro
        } else {
            R.string.property_list_screen_property_price_square_text_dollars
        }
    }

    fun getGoogleMapScreenMarkerSnippet(currency: String): Int {
        return if (currency == EUR) {
            R.string.google_map_screen_marker_snippet_euro
        } else {
            R.string.google_map_screen_marker_snippet_dollars
        }
    }

    fun getPropertyDetailsPagePropertyPriceText(currency : String) : Int {
        return if (currency == EUR) {
            R.string.property_details_page_property_price_text_euro
        } else {
            R.string.property_details_page_property_price_text_dollars
        }
    }

    fun getPropertyDetailsPagePropertyPriceSquareText(currency: String) : Int {
        return if (currency == EUR) {
            R.string.property_details_page_property_price_square_text_euro
    } else {
            R.string.property_details_page_property_price_square_text_dollars
        }
    }

    fun getUserPropertiesPagePropertyPriceText(currency: String) :Int {
        return if (currency == EUR) {
            R.string.user_properties_page_property_price_text_euro
    } else {
            R.string.user_properties_page_property_price_text_dollars
        }
    }

    fun getUserPropertiesPagePropertyPriceSquareText(currency: String) : Int {
        return if (currency == EUR) {
            R.string.user_properties_page_property_price_square_text_euro
    } else {
            R.string.user_properties_page_property_price_square_text_dollars
        }
    }

    fun getAccountPagePropertyPriceSquareText(currency: String): Int {
        return if (currency == EUR) {
            R.string.account_page_property_price_price_square_text_euro
            } else {
            R.string.account_page_property_price_price_square_text_dollars
        }
    }

    /**
     * 💱 Converts property price from USD to EUR.
     * NOTE: Must be shown during the exam.
     * @param dollars Amount in USD.
     * @return Converted amount in EUR.
     */

    fun convertDollarToEuro(dollars: Int): Int {
        return (dollars * 0.812).roundToInt()
    }

    fun convertEuroToDollar(euros: Int): Int {
        return (euros / 0.812).roundToInt()
    }
}