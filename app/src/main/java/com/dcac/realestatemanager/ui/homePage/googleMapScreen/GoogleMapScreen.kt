package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.utils.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GoogleMapScreen(
    viewModel: GoogleMapViewModel = hiltViewModel(),
    onPropertyClick: (PropertyWithLocation) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isConnected = Utils.isInternetAvailable(context)
        if (isConnected) {
            viewModel.loadMapData()
        }
    }

    if (!isConnected) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.no_internet_connexion_text),
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    when (val state = uiState) {
        is GoogleMapUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is GoogleMapUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }

        is GoogleMapUiState.Partial -> {
            MapContent(
                userLocation = state.userLocation,
                properties = emptyList(),
                poiS = emptyList(),
                onPropertyClick = onPropertyClick
            )
        }

        is GoogleMapUiState.Success -> {
            MapContent(
                userLocation = state.userLocation,
                properties = state.properties,
                poiS = state.poiS,
                onPropertyClick = onPropertyClick
            )
        }
    }
}

@Composable
fun MapContent(
    userLocation: Location?,
    properties: List<PropertyWithLocation>,
    poiS: List<PoiWithLocation>,
    onPropertyClick: (PropertyWithLocation) -> Unit
) {
    val context = LocalContext.current

    if (userLocation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.location_authorization_request_text),
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(userLocation) {
        val latLng = LatLng(userLocation.latitude, userLocation.longitude)
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(latLng, 14f)
        )
        println("Lat: ${userLocation.latitude}, Lng: ${userLocation.longitude}")
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    ) {
        Marker(
            state = MarkerState(position = LatLng(userLocation.latitude, userLocation.longitude)),
            title = stringResource(R.string.user_marker_title),
            snippet = stringResource(R.string.user_marker_snippet),
            icon = getCachedUserMarker(context)
        )

        properties.forEach { item ->
            Marker(
                state = MarkerState(position = item.latLng),
                title = item.property.address,
                snippet = "${item.property.price} €",
                icon = getCachedPropertyMarker(
                    context = context,
                    type = item.property.type
                ),
                onClick = {
                    onPropertyClick(item)
                    false
                }
            )
        }

        poiS.forEach { poi ->
            Marker(
                state = MarkerState(position = poi.latLng),
                title = poi.poi.name,
                snippet = poi.poi.type,
                icon = getCachedPoiMarker(
                    context = context,
                    type = poi.poi.type
                )
            )
        }
    }
}


