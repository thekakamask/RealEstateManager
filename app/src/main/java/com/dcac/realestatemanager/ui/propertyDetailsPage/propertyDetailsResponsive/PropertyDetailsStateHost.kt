package com.dcac.realestatemanager.ui.propertyDetailsPage.propertyDetailsResponsive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.propertyDetailsPage.EditSection
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsUiState

@Composable
fun PropertyDetailsStateHost(
    uiState: PropertyDetailsUiState,
    onEditSectionSelected: (EditSection, String) -> Unit,
    onDeleteConfirmed: (Property) -> Unit,
    content: @Composable (
        property: Property,
        userName: String,
        isOwnedByCurrentUser: Boolean,
        onEditClick: () -> Unit
    ) -> Unit
) {
    val showEditSheet = rememberSaveable { mutableStateOf(false) }
    val propertyToDelete = rememberSaveable { mutableStateOf<Property?>(null) }

    when (uiState) {

        is PropertyDetailsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PropertyDetailsUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is PropertyDetailsUiState.Success -> {
            val property = uiState.property

            content(
                property,
                uiState.userName,
                uiState.isOwnedByCurrentUser,
                { showEditSheet.value = true }
            )

            if (showEditSheet.value) {
                EditPropertyBottomSheet(
                    onDismiss = { showEditSheet.value = false },
                    onOptionSelected = { section ->
                        showEditSheet.value = false
                        onEditSectionSelected(section, property.universalLocalId)
                    },
                    onDeleteProperty = {
                        propertyToDelete.value = property
                        showEditSheet.value = false
                    }
                )
            }

            propertyToDelete.value?.let {
                AlertDialog(
                    onDismissRequest = { propertyToDelete.value = null },
                    title = {
                        Text(stringResource(R.string.property_details_page_delete_section_title))
                    },
                    text = {
                        Text(
                            stringResource(
                                R.string.property_details_page_delete_property_text,
                                it.title
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeleteConfirmed(it)
                                propertyToDelete.value = null
                            }
                        ) {
                            Text(stringResource(R.string.property_details_page_delete_section_confirm_button_text))
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { propertyToDelete.value = null }
                        ) {
                            Text(stringResource(R.string.property_details_page_delete_property_cancel_button_text))
                        }
                    }
                )
            }
        }
    }
}
