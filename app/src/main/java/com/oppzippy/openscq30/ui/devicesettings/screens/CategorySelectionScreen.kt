package com.oppzippy.openscq30.ui.devicesettings.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.bindings.translateValue
import com.oppzippy.openscq30.lib.wrapper.Setting
import com.oppzippy.openscq30.lib.wrapper.Value
import com.oppzippy.openscq30.lib.wrapper.toValue
import com.oppzippy.openscq30.ui.devicesettings.Screen
import com.oppzippy.openscq30.ui.devicesettings.ScreenInfo
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme

@Composable
fun CategorySelectionScreen(
    deviceModel: String = "Soundcore Liberty 4 NC",
    settings: List<Pair<String, Setting>> = emptyList(),
    setSettings: (List<Pair<String, Value>>) -> Unit = {},
    screens: List<ScreenInfo>,
    onNavigation: (Screen) -> Unit,
) {
    val ambientSoundSetting = settings.find { it.first == "ambientSoundMode" }?.second as? Setting.SelectSetting
    val leftBattery = (settings.find { it.first == "leftBatteryLevel" }?.second as? Setting.InformationSetting)?.translatedValue ?: "88%"
    val rightBattery = (settings.find { it.first == "rightBatteryLevel" }?.second as? Setting.InformationSetting)?.translatedValue ?: "92%"
    val caseBattery = (settings.find { it.first == "caseBatteryLevel" }?.second as? Setting.InformationSetting)?.translatedValue ?: "95%"

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val isWideScreen = maxWidth >= 700.dp
        val horizontalPadding = if (isWideScreen) 32.dp else 16.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Nothing App / Galaxy Buds Inspired Premium Hero Dashboard Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 800.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(22.dp),
                        ) {
                            // Bluetooth Connection Status Bar at the Top ("Bluetooth at the top")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                painter = painterResource(R.drawable.bluetooth_24px),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "BLUETOOTH AUDIO",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp,
                                        )
                                        Text(
                                            text = "Connected via LE Audio",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
                                        )
                                        Text(
                                            text = "Active",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }

                            // Device Name Header
                            Column {
                                Text(
                                    text = "CONNECTED DEVICE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.4.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = deviceModel,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            // Earbud & Case Battery Status Pods with Expressive Circular Gradients
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                BatteryPod(
                                    modifier = Modifier.weight(1f),
                                    title = "Left",
                                    level = leftBattery,
                                    icon = R.drawable.earbuds_2_24px,
                                )
                                BatteryPod(
                                    modifier = Modifier.weight(1f),
                                    title = "Right",
                                    level = rightBattery,
                                    icon = R.drawable.earbuds_2_24px,
                                )
                                BatteryPod(
                                    modifier = Modifier.weight(1f),
                                    title = "Case",
                                    level = caseBattery,
                                    icon = R.drawable.earbud_case_24px,
                                )
                            }
                        }
                    }
                }
            }

            // Comprehensive Noise Control Center Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 800.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            // Header with Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(R.drawable.headphones_24px),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Noise Control Center",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "ANC, Transparency & Sound Modes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    )
                                }
                            }

                            // 1. Ambient Sound Mode (Noise Canceling / Transparency / Normal)
                            if (ambientSoundSetting != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "LISTENING MODE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp,
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        ambientSoundSetting.setting.options.forEach { option ->
                                            val isSelected = ambientSoundSetting.value == option
                                            val optionLabel = translateValue(Value.StringValue(option))
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    setSettings(listOf(Pair("ambientSoundMode", option.toValue())))
                                                },
                                                label = {
                                                    Text(
                                                        text = optionLabel,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    )
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(16.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Conditional Sub-Modes & Sliders
                            when (ambientSoundSetting?.value) {
                                "noiseCanceling" -> {
                                    val noiseCancelingModeSetting = settings.find { it.first == "noiseCancelingMode" }?.second as? Setting.SelectSetting
                                    val customNcSetting = settings.find { it.first == "customNoiseCanceling" }?.second as? Setting.I32RangeSetting

                                    if (noiseCancelingModeSetting != null) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "ANC ENVIRONMENT PROFILE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.1.sp,
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                noiseCancelingModeSetting.setting.options.forEach { option ->
                                                    val isSelected = noiseCancelingModeSetting.value == option
                                                    val optionLabel = translateValue(Value.StringValue(option))
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            setSettings(listOf(Pair("noiseCancelingMode", option.toValue())))
                                                        },
                                                        label = {
                                                            Text(
                                                                text = optionLabel,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            )
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(14.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (customNcSetting != null) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = "Custom ANC Intensity",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                )
                                                Text(
                                                    text = "Level ${customNcSetting.value}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                            Slider(
                                                value = customNcSetting.value.toFloat(),
                                                onValueChange = { newValue ->
                                                    setSettings(listOf(Pair("customNoiseCanceling", newValue.toInt().toValue())))
                                                },
                                                valueRange = 1f..5f,
                                                steps = 3,
                                            )
                                        }
                                    }
                                }
                                "transparency" -> {
                                    val transparencyModeSetting = settings.find { it.first == "transparencyMode" }?.second as? Setting.SelectSetting
                                    if (transparencyModeSetting != null) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "TRANSPARENCY PROFILE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.1.sp,
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                transparencyModeSetting.setting.options.forEach { option ->
                                                    val isSelected = transparencyModeSetting.value == option
                                                    val optionLabel = translateValue(Value.StringValue(option))
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = {
                                                            setSettings(listOf(Pair("transparencyMode", option.toValue())))
                                                        },
                                                        label = {
                                                            Text(
                                                                text = optionLabel,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            )
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(16.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Wind Noise Suppression Toggle Switch
                            val windNoiseSetting = settings.find { it.first == "windNoiseSuppression" }?.second as? Setting.ToggleSetting
                            if (windNoiseSetting != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(R.drawable.light_mode_24px),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "Wind Noise Suppression",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                text = "Reduces wind interference outdoors",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = windNoiseSetting.value,
                                        onCheckedChange = { checked ->
                                            setSettings(listOf(Pair("windNoiseSuppression", checked.toValue())))
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Title for Categories
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 800.dp),
                ) {
                    Text(
                        text = "Device Controls & Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )
                }
            }

            // Responsive Category Cards (1 column on phones, 2-column grid on tablets/wide screens)
            if (isWideScreen) {
                val chunks = screens.chunked(2)
                items(chunks) { rowScreens ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 800.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            rowScreens.forEach { screenInfo ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CategoryCard(screenInfo = screenInfo, onNavigation = onNavigation)
                                }
                            }
                            if (rowScreens.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                items(screens) { screenInfo ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 800.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CategoryCard(screenInfo = screenInfo, onNavigation = onNavigation)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    screenInfo: ScreenInfo,
    onNavigation: (Screen) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigation(screenInfo.baseRoute) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(52.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(screenInfo.icon),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = screenInfo.name.translated(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Configure preferences and options",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
        }
    }
}

@Composable
fun BatteryPod(
    modifier: Modifier = Modifier,
    title: String,
    level: String,
    icon: Int,
) {
    val percentage = level.replace(Regex("[^0-9]"), "").toFloatOrNull()?.div(100f)?.coerceIn(0f, 1f) ?: 0.85f
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = primaryColor,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }

            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                val strokeWidth = 6.dp
                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(primaryColor, tertiaryColor, primaryColor)
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = strokeWidth.toPx()
                    drawArc(
                        color = surfaceVariant,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = gradientBrush,
                        startAngle = -90f,
                        sweepAngle = 360f * percentage,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCategorySelectionScreen() {
    OpenSCQ30Theme(dynamicColor = false) {
        CategorySelectionScreen(
            screens = listOf(
                Screen.SettingsCategory("general").screenInfo(),
                Screen.SettingsCategory("equalizer").screenInfo(),
                Screen.SettingsCategory("buttonConfiguration").screenInfo(),
                Screen.QuickPresets.screenInfo,
            ),
            onNavigation = {},
        )
    }
}
