package com.cosmica.app.presentation.moonphase

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmica.app.R
import com.cosmica.app.domain.model.MoonPhase
import com.cosmica.app.domain.model.MoonPhaseType
import com.cosmica.app.domain.repository.CitySearchResult
import com.cosmica.app.presentation.common.ErrorState
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.theme.CardSurface
import com.cosmica.app.presentation.theme.MoonGray

@Composable
fun MoonPhaseScreen(viewModel: MoonPhaseViewModel = hiltViewModel()) {
    val uiState         by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val shouldShowRationale = (context as? Activity)?.let { activity ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        } ?: false
        viewModel.onPermissionResult(granted, shouldShowRationale)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.onPermissionResult(granted = true, shouldShowRationale = false)
        } else {
            showRationale = true
        }
    }

    if (showRationale && permissionState == LocationPermissionState.Unknown) {
        LocationRationaleDialog(
            onConfirm = {
                showRationale = false
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            },
            onDismiss = {
                showRationale = false
                viewModel.onPermissionResult(granted = false, shouldShowRationale = true)
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(
            text     = stringResource(R.string.moon_phase_title),
            style    = MaterialTheme.typography.headlineMedium,
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        when (permissionState) {
            LocationPermissionState.Granted -> when (val state = uiState) {
                is ScreenUiState.Loading -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is ScreenUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = viewModel::loadFromCurrentLocation,
                )
                is ScreenUiState.Success -> MoonPhaseContent(state.data)
                is ScreenUiState.Empty   -> Unit
            }

            LocationPermissionState.Denied,
            LocationPermissionState.DeniedPermanently -> CitySearchFallback(
                viewModel        = viewModel,
                uiState          = uiState,
                openSettings     = permissionState == LocationPermissionState.DeniedPermanently,
            )

            LocationPermissionState.Unknown -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun LocationRationaleDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title       = { Text(stringResource(R.string.moon_phase_permission_title)) },
        text        = { Text(stringResource(R.string.moon_phase_permission_rationale)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.moon_phase_permission_allow))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.moon_phase_permission_deny))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySearchFallback(
    viewModel: MoonPhaseViewModel,
    uiState: ScreenUiState<MoonPhase>,
    openSettings: Boolean,
) {
    val cityQuery       by viewModel.cityQuery.collectAsStateWithLifecycle()
    val citySuggestions by viewModel.citySuggestions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text  = stringResource(R.string.moon_phase_search_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MoonGray,
        )

        if (openSettings) {
            Spacer(Modifier.size(8.dp))
            TextButton(onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            }) { Text(stringResource(R.string.moon_phase_open_settings)) }
        }

        Spacer(Modifier.size(12.dp))

        OutlinedTextField(
            value         = cityQuery,
            onValueChange = viewModel::onCityQueryChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text(stringResource(R.string.moon_phase_search_placeholder)) },
            leadingIcon   = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine    = true,
        )

        if (citySuggestions.isNotEmpty()) {
            LazyColumn(
                modifier            = Modifier.fillMaxWidth().heightIn(max = 220.dp).padding(top = 4.dp),
                contentPadding      = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(citySuggestions, key = { it.displayName + it.coordinates.latitude }) { city ->
                    CitySuggestionRow(city = city, onClick = { viewModel.selectCity(city) })
                }
            }
        }

        Spacer(Modifier.size(16.dp))

        when (val state = uiState) {
            is ScreenUiState.Loading -> {
                if (cityQuery.isNotBlank() && citySuggestions.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is ScreenUiState.Success -> MoonPhaseContent(state.data)
            is ScreenUiState.Error   -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is ScreenUiState.Empty   -> Unit
        }
    }
}

@Composable
private fun CitySuggestionRow(city: CitySearchResult, onClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(CardSurface, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = MoonGray, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(city.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MoonPhaseContent(moon: MoonPhase) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // Animated moon visualization
        MoonCanvas(
            phase = moon.phase,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )

        Spacer(Modifier.size(8.dp))

        Text(
            text       = moon.phaseType.toDisplayName(),
            style      = MaterialTheme.typography.headlineSmall,
            color      = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.fillMaxWidth(),
        )

        Text(
            text     = stringResource(R.string.moon_phase_illumination, moon.illuminationPercent),
            style    = MaterialTheme.typography.bodyMedium,
            color    = MoonGray,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (moon.locationName.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = MoonGray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
                Text(moon.locationName, style = MaterialTheme.typography.bodySmall, color = MoonGray)
            }
        }

        Spacer(Modifier.size(20.dp))

        InfoCard(
            label = stringResource(R.string.moon_phase_next_full),
            value = if (moon.daysUntilFullMoon == 0)
                stringResource(R.string.moon_phase_full_today)
            else
                stringResource(R.string.moon_phase_days_until_full, moon.daysUntilFullMoon),
            icon  = Icons.Rounded.NightsStay,
        )

        Spacer(Modifier.size(24.dp))
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MoonGray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(12.dp))
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MoonGray)
                Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun MoonPhaseType.toDisplayName(): String = stringResource(
    when (this) {
        MoonPhaseType.NEW_MOON         -> R.string.moon_phase_new_moon
        MoonPhaseType.WAXING_CRESCENT  -> R.string.moon_phase_waxing_crescent
        MoonPhaseType.FIRST_QUARTER    -> R.string.moon_phase_first_quarter
        MoonPhaseType.WAXING_GIBBOUS   -> R.string.moon_phase_waxing_gibbous
        MoonPhaseType.FULL_MOON        -> R.string.moon_phase_full_moon
        MoonPhaseType.WANING_GIBBOUS   -> R.string.moon_phase_waning_gibbous
        MoonPhaseType.LAST_QUARTER     -> R.string.moon_phase_last_quarter
        MoonPhaseType.WANING_CRESCENT  -> R.string.moon_phase_waning_crescent
    }
)
