package com.dcac.realestatemanager.ui.accountPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.Utils.calculatePricePerSquareMeter
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper
import androidx.compose.material3.AlertDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPage(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
){

    val uiState by viewModel.uiState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showBottomSheet = (uiState as? AccountUiState.Success)?.isEditing == true
    val propertyToDelete = remember { mutableStateOf<Property?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is AccountUiState.Idle) {
            viewModel.checkAndLoadUser()
        }
    }

    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            bottomSheetState.show()
        } else {
            bottomSheetState.hide()
        }
    }

    Scaffold(
        topBar = {
            AccountTopBar(onBack = onBack)
        },
        floatingActionButton = {
            if (uiState is AccountUiState.Success) {
                FloatingActionButton(
                    onClick = {
                        viewModel.enterEditMode()
                    },
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 32.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.modify_24px),
                        contentDescription = stringResource(R.string.details_page_modify_button_description),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is AccountUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator()
                    }
                }

                is AccountUiState.Success -> {
                    Column(Modifier.fillMaxSize()) {
                        UserInfoSection(user = state.user)
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            items(state.properties.size) { index ->
                                UserPropertyItem(
                                    property = state.properties[index],
                                    onDeleteClick = { property ->
                                        propertyToDelete.value = property
                                    }

                                )
                            }
                        }
                    }
                }

                is AccountUiState.Error -> {
                    val message = state.message
                    Text(stringResource(R.string.account_page_ui_state_error, message), color = MaterialTheme.colorScheme.error)
                }

                else -> {}
            }
        }
    }

    if (showBottomSheet) {
       val user = (uiState as AccountUiState.Success).user
        ModalBottomSheet(
            onDismissRequest = { viewModel.resetState() },
            sheetState = bottomSheetState
        ) {
            EditUserBottomSheetContent(
                currentName = user.agentName,
                onConfirm = { newName ->
                    viewModel.updateUser(newName)
                },
                onCancel = {
                    viewModel.resetState()
                }
            )
        }
    }

    propertyToDelete.value?.let { property ->
        AlertDialog(
            onDismissRequest = {
                propertyToDelete.value = null
            },
            title = {
                Text(stringResource(R.string.account_page_delete_section_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.account_page_delete_property_text,
                        property.title
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProperty(property)
                        propertyToDelete.value = null
                    }
                ) {
                    Text(stringResource(R.string.account_page_delete_section_confirm_button_text))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        propertyToDelete.value = null
                    }
                ) {
                    Text(stringResource(R.string.account_page_delete_property_cancel_button_text))
                }
            }
        )
    }
}

@Composable
fun UserInfoSection(user: User) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.user_icon),
            contentDescription = stringResource(R.string.account_page_user_icon_content_description),
            tint = Color.Unspecified,
            modifier = Modifier
                .size(96.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = user.agentName,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
fun UserPropertyItem(
    property: Property,
    modifier: Modifier = Modifier,
    onDeleteClick: (Property) -> Unit
) {
    val currency = CurrencyHelper.LocalCurrency.current

    val displayPrice = if (currency == "EUR") {
        CurrencyHelper.convertDollarToEuro(property.price)
    } else {
        property.price
    }

    val pricePerSquareMeter = calculatePricePerSquareMeter(displayPrice, property.surface)

    val priceTextRes = CurrencyHelper.getAccountPagePropertyPriceSquareText(currency)

    val formattedPrice = stringResource(
        id = priceTextRes,
        displayPrice,
        pricePerSquareMeter
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ){
        Box {
            IconButton(
                onClick = {
                    onDeleteClick(property)
                          },
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.delete_24px),
                    contentDescription = stringResource(R.string.delete_section_button_text),
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(property.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    property.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Text(
                        stringResource(
                            R.string.account_page_property_photo_size_text,
                            property.photos.size
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(
                            R.string.account_page_property_poi_size_text,
                            property.poiS.size
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(formattedPrice, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.account_page_top_bar_back_button_content_description)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.account_circle_24px),
                        contentDescription = stringResource(R.string.account_page_top_bar_icon_content_description),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.account_page_top_bar_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    )
}

@Composable
fun EditUserBottomSheetContent(
    currentName: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    val nameState = remember { mutableStateOf(currentName) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(R.string.account_page_edit_account_sheet_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text(stringResource(R.string.account_page_edit_account_sheet_agent_name_text_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(onClick = onCancel) {
                Text(stringResource(R.string.account_page_edit_account_sheet_cancel_text_button))
            }
            Button(onClick = {
                onConfirm(nameState.value)
            }) {
                Text(stringResource(R.string.account_page_edit_account_sheet_apply_button_text))
            }
        }
    }
}
