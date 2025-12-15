package com.dcac.realestatemanager.ui.settingsPage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.R

@Composable
fun SettingsPage(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Scaffold(
        topBar = {
            SettingsTopBar(onBack = onBack)
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when (val state = uiState) {
                is SettingsUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is SettingsUiState.Success -> {
                    SettingsContent(
                        currentLanguage = state.currentLanguage,
                        currentCurrency = state.currentCurrency,
                        appVersion = state.appVersion,
                        onLanguageChange = { viewModel.changeLanguage(it) },
                        onCurrencyChange = { viewModel.changeCurrency(it) }
                    )
                }

                is SettingsUiState.Error -> {
                    Text(
                        text = stringResource(R.string.settings_page_ui_state_error, state.message),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.settings_page_top_bar_back_button_content_description)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings_24px),
                        contentDescription = stringResource(R.string.settings_page_top_bar_icon_content_description),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_page_top_bar_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    )
}

@Composable
fun SettingsContent(
    currentLanguage: String,
    currentCurrency: String,
    appVersion: String,
    onLanguageChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(stringResource(R.string.settings_page_language_title), style = MaterialTheme.typography.titleMedium)
        LanguageSelector(currentLanguage, onLanguageChange)

        HorizontalDivider()

        Text(stringResource(R.string.settings_page_currency_title), style = MaterialTheme.typography.titleMedium)
        CurrencySelector(currentCurrency, onCurrencyChange)

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()
        Text(stringResource(R.string.settings_page_app_version, appVersion), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(stringResource(R.string.settings_page_en) to stringResource(R.string.settings_page_english),
        stringResource(
            R.string.settings_page_fr
        ) to stringResource(R.string.settings_page_fran_ais))

    Column {
        languages.forEach { (code, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(code) }
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = code == selectedLanguage,
                    onClick = { onLanguageSelected(code) }
                )
                Text(text = label)
            }
        }
    }
}

@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    val currencies = listOf(stringResource(R.string.settings_page_usd) to stringResource(R.string.settings_page_us_dollar),
        stringResource(
            R.string.settings_page_eur
        ) to stringResource(R.string.settings_page_euro))

    Column {
        currencies.forEach { (code, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCurrencySelected(code) }
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = code == selectedCurrency,
                    onClick = { onCurrencySelected(code) }
                )
                Text(text = label)
            }
        }
    }
}