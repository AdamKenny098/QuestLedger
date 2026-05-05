package ie.setu.questledger.ui.screens.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun CampaignMapScreen(
    vm: CampaignMapViewModel = hiltViewModel()
) {
    val quests = vm.quests.value
    val places = vm.places.value
    val isLoading = vm.isLoading.value
    val isErr = vm.isErr.value
    val error = vm.error.value

    val startPosition = when {
        quests.isNotEmpty() -> LatLng(quests.first().latitude, quests.first().longitude)
        places.isNotEmpty() -> LatLng(places.first().latitude, places.first().longitude)
        else -> vm.defaultCentre
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPosition, 6f)
    }

    val uiSettings = remember {
        MapUiSettings(
            compassEnabled = true,
            mapToolbarEnabled = true
        )
    }

    val properties = remember {
        MapProperties(
            mapType = MapType.NORMAL
        )
    }

    LaunchedEffect(quests, places) {
        when {
            quests.isNotEmpty() -> {
                val first = LatLng(quests.first().latitude, quests.first().longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(first, 6f)
            }

            places.isNotEmpty() -> {
                val first = LatLng(places.first().latitude, places.first().longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(first, 6f)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }

            isErr -> {
                Text(
                    text = error,
                    modifier = Modifier.padding(24.dp)
                )
            }

            quests.isEmpty() && places.isEmpty() -> {
                Text(
                    text = "No quest or place markers yet.",
                    modifier = Modifier.padding(24.dp)
                )
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Quest markers: ${quests.size} • Place markers: ${places.size}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = uiSettings,
                        properties = properties
                    ) {
                        quests.forEach { quest ->
                            Marker(
                                state = MarkerState(
                                    position = LatLng(quest.latitude, quest.longitude)
                                ),
                                title = quest.title,
                                snippet = "Quest • ${quest.status} • ${quest.description}"
                            )
                        }

                        places.forEach { place ->
                            Marker(
                                state = MarkerState(
                                    position = LatLng(place.latitude, place.longitude)
                                ),
                                title = place.title,
                                snippet = "Place • ${place.region} • ${place.description}"
                            )
                        }
                    }
                }
            }
        }
    }
}