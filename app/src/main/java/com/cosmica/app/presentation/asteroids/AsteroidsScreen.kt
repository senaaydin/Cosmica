package com.cosmica.app.presentation.asteroids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Rocket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmica.app.R
import com.cosmica.app.domain.model.NearEarthObject
import com.cosmica.app.presentation.common.EmptyState
import com.cosmica.app.presentation.common.ErrorState
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.theme.CardSurface
import com.cosmica.app.presentation.theme.MeteorRed
import com.cosmica.app.presentation.theme.SafeGreen

@Composable
fun AsteroidsScreen(
    onAsteroidClick: (String) -> Unit,
    viewModel: AsteroidsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text     = stringResource(R.string.asteroids_title),
            style    = MaterialTheme.typography.headlineMedium,
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Text(
            text     = stringResource(R.string.asteroids_this_week),
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        )

        when (val state = uiState) {
            is ScreenUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is ScreenUiState.Error -> ErrorState(
                message = state.message,
                onRetry = viewModel::loadAsteroids,
            )
            is ScreenUiState.Empty -> EmptyState(
                message = stringResource(R.string.asteroids_empty),
                icon    = Icons.Rounded.Rocket,
            )
            is ScreenUiState.Success -> AsteroidList(
                asteroids       = state.data,
                onAsteroidClick = onAsteroidClick,
            )
        }
    }
}

@Composable
private fun AsteroidList(
    asteroids: List<NearEarthObject>,
    onAsteroidClick: (String) -> Unit,
) {
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(asteroids, key = { it.id }) { neo ->
            AsteroidCard(neo = neo, onClick = { onAsteroidClick(neo.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsteroidCard(neo: NearEarthObject, onClick: () -> Unit) {
    val hazardColor = if (neo.isPotentiallyHazardous) MeteorRed else SafeGreen

    Card(
        onClick  = onClick,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Rounded.Circle,
                    contentDescription = stringResource(R.string.cd_asteroid_hazard_indicator),
                    tint               = hazardColor,
                    modifier           = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = neo.name,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f),
                )
                Text(
                    text  = if (neo.isPotentiallyHazardous)
                        stringResource(R.string.asteroids_hazardous)
                    else
                        stringResource(R.string.asteroids_safe),
                    style = MaterialTheme.typography.labelSmall,
                    color = hazardColor,
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text  = stringResource(R.string.asteroids_approach_date, neo.closeApproachDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text  = stringResource(
                    R.string.asteroids_diameter,
                    neo.estimatedDiameterMinKm,
                    neo.estimatedDiameterMaxKm,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text  = stringResource(R.string.asteroids_velocity, neo.formattedVelocity),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
