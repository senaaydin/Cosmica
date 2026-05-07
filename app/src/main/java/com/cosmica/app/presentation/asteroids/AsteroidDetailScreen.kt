package com.cosmica.app.presentation.asteroids

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosmica.app.R
import com.cosmica.app.domain.model.NearEarthObject
import com.cosmica.app.presentation.theme.MeteorRed
import com.cosmica.app.presentation.theme.SafeGreen

@Composable
fun AsteroidDetailScreen(
    neo: NearEarthObject,
    onBack: () -> Unit,
) {
    val hazardColor = if (neo.isPotentiallyHazardous) MeteorRed else SafeGreen

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Spacer(Modifier.statusBarsPadding())
            Spacer(Modifier.height(48.dp)) // room for back button

            // Hazard badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(
                        color = hazardColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Circle,
                    contentDescription = null,
                    tint               = hazardColor,
                    modifier           = Modifier.size(10.dp),
                )
                Text(
                    text  = if (neo.isPotentiallyHazardous)
                        stringResource(R.string.asteroids_hazardous)
                    else
                        stringResource(R.string.asteroids_safe),
                    style = MaterialTheme.typography.labelMedium,
                    color = hazardColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text  = neo.name,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text  = "ID: ${neo.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))
            SectionTitle("Close Approach Data")
            DataRow(label = "Date",      value = neo.closeApproachDate)
            DataRow(label = "Speed",     value = "${neo.formattedVelocity} km/h")
            DataRow(label = "Miss dist", value = "${neo.formattedMissDistanceKm} km")
            DataRow(label = "",          value = stringResource(R.string.asteroids_miss_lunar, neo.missDistanceLunar))
            DataRow(label = "Orbiting",  value = neo.orbitingBody)

            Spacer(Modifier.height(20.dp))
            SectionTitle("Physical Characteristics")
            DataRow(label = "Diameter (min)", value = "%.4f km".format(neo.estimatedDiameterMinKm))
            DataRow(label = "Diameter (max)", value = "%.4f km".format(neo.estimatedDiameterMaxKm))
            DataRow(label = "Magnitude",      value = "%.1f H".format(neo.absoluteMagnitude))
            Spacer(Modifier.height(80.dp))
        }

        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp),
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back_button),
                tint               = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text  = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 8.dp),
        color     = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
        )
    }
}
