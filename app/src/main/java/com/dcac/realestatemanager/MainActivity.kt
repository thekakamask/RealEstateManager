package com.dcac.realestatemanager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.dcac.realestatemanager.dI.SettingsEntryPoint
import com.dcac.realestatemanager.ui.RealEstateManagerApp
import com.dcac.realestatemanager.ui.settingsPage.SettingsStateHolder
import com.dcac.realestatemanager.ui.theme.RealEstateManagerTheme
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper.LocalCurrency
import com.dcac.realestatemanager.utils.settingsUtils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsStateHolder: SettingsStateHolder

    override fun attachBaseContext(newBase: Context) {

        // Récupération Hilt via EntryPoint (pas via @Inject)
        val entryPoint = EntryPointAccessors.fromApplication(
            newBase.applicationContext,
            SettingsEntryPoint::class.java
        )

        val lang = runBlocking {
            entryPoint.settingsStateHolder().settingsState.first().language
        }

        val updated = LocaleHelper.updateLocale(newBase, lang)

        super.attachBaseContext(updated)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            println(getString(R.string.location_permission_accepted))
        } else {
            Toast.makeText(
                this,
                getString(R.string.location_required_to_display_your_position_on_the_map),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        enableEdgeToEdge()

        setContent {
            val settingsState by settingsStateHolder.settingsState.collectAsState()

            CompositionLocalProvider(
                LocalCurrency provides settingsState.currency
            ) {
                RealEstateManagerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        RealEstateManagerApp()
                    }
                }
            }
        }
    }
}



