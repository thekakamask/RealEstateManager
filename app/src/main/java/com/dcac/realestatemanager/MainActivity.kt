package com.dcac.realestatemanager

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
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

    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val locationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val notificationGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions[Manifest.permission.POST_NOTIFICATIONS] == true
                } else {
                    true
                }

            if (!locationGranted) {
                Toast.makeText(
                    this,
                    getString(R.string.location_required_to_display_your_position_on_the_map),
                    Toast.LENGTH_LONG
                ).show()
            }

            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !notificationGranted
            ) {
                Toast.makeText(
                    this,
                    "Notifications disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsToRequest = mutableListOf<String>()

        lockOrientation()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(
                permissionsToRequest.toTypedArray()
            )
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

    private fun isTablet(): Boolean {
        return resources.configuration.smallestScreenWidthDp >= 600
    }

    private fun lockOrientation() {
        requestedOrientation = if (isTablet()) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}



