package com.dcac.realestatemanager.ui.propertyDetailsPage.propertyDetailsResponsive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.propertyDetailsPage.EditSection
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsUiState

@Composable
fun PropertyDetailsTablet(
    uiState: PropertyDetailsUiState,
    onEditSectionSelected: (EditSection, String) -> Unit,
    onDeleteConfirmed: (Property) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        PropertyDetailsStateHost(
            uiState = uiState,
            onEditSectionSelected = onEditSectionSelected,
            onDeleteConfirmed = onDeleteConfirmed
        ) { property, userName, isOwnedByCurrentUser, onEditClick ->

            PropertyDetailsSuccessContent(
                property = property,
                userName = userName
            )

            if (isOwnedByCurrentUser) {
                FloatingActionButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.modify_24px),
                        contentDescription = stringResource(
                            R.string.details_page_modify_button_description
                        )
                    )
                }
            }
        }
    }
}
