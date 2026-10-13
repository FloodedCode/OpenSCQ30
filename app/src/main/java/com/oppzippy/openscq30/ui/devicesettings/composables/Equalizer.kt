package com.oppzippy.openscq30.ui.devicesettings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.ui.theme.OpenSCQ30Theme
import com.oppzippy.openscq30.ui.utils.throttledState
import java.math.BigDecimal
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun Equalizer(
    bands: List<UShort>,
    values: List<Short>,
    minValue: Short,
    maxValue: Short,
    fractionDigits: Short,
    onValueChange: (index: Int, value: Short) -> Unit,
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        bands.zip(values).forEachIndexed { index, (hz, value) ->
            EqualizerRow(
                hz = hz,
                value = value,
                minValue = minValue,
                maxValue = maxValue,
                fractionDigits = fractionDigits,
                onValueChange = {
                    onValueChange(index, it)
                },
            )
        }
    }
}

private val removeSecondDecimalRegex = Regex("^([^.]*\\.[^.]*)\\..*$")

@Composable
private fun EqualizerRow(
    hz: UShort,
    value: Short,
    minValue: Short,
    maxValue: Short,
    fractionDigits: Short,
    onValueChange: (Short) -> Unit,
) {
    val (displayedValue, setDisplayedValue) = throttledState(
        value,
        250,
        onValueChange = { onValueChange(it) },
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    if (hz < 1000u) {
                        Text(
                            text = stringResource(R.string.hz, hz.toInt()),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    } else {
                        Text(
                            text = stringResource(
                                R.string.khz,
                                BigDecimal(hz.toInt()).divide(BigDecimal(1000)).toString(),
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                EqualizerTextInput(
                    modifier = Modifier.width(72.dp),
                    hz = hz,
                    value = displayedValue,
                    minValue = minValue,
                    maxValue = maxValue,
                    fractionDigits = fractionDigits,
                    onValueChange = setDisplayedValue,
                )
            }
            Spacer(Modifier.height(8.dp))
            EqualizerSlider(
                modifier = Modifier.fillMaxWidth(),
                value = displayedValue,
                minValue = minValue,
                maxValue = maxValue,
                fractionDigits = fractionDigits,
                onValueChange = setDisplayedValue,
            )
        }
    }
}

@Composable
private fun EqualizerTextInput(
    modifier: Modifier = Modifier,
    hz: UShort,
    value: Short,
    minValue: Short,
    maxValue: Short,
    fractionDigits: Short,
    onValueChange: (Short) -> Unit,
) {
    var text by remember(hz, value) {
        mutableStateOf(BigDecimal(value.toInt()).scaleByPowerOfTen(-fractionDigits).toString())
    }
    val matches = removeSecondDecimalRegex.matchEntire(text)
    val reformattedText = if (matches != null) {
        matches.groupValues[1]
    } else {
        text
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            modifier = Modifier.testTag("equalizerInput$hz"),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            value = reformattedText,
            onValueChange = {
                text = it
                try {
                    val newValue = BigDecimal(it).scaleByPowerOfTen(fractionDigits.toInt()).toShort()
                    if (newValue in minValue..maxValue) {
                        onValueChange(newValue)
                    }
                } catch (_: NumberFormatException) {
                }
            },
        )
    }
}

@Composable
private fun EqualizerSlider(
    modifier: Modifier = Modifier,
    value: Short,
    minValue: Short,
    maxValue: Short,
    fractionDigits: Short,
    onValueChange: (Short) -> Unit,
) {
    val divisor = 10f.pow(fractionDigits.toInt())
    Slider(
        modifier = modifier.testTag("equalizerSlider"),
        value = value.toFloat() / divisor,
        onValueChange = {
            onValueChange((it * divisor).roundToInt().toShort())
        },
        valueRange = (minValue.toFloat() / divisor)..(maxValue.toFloat() / divisor),
        // The min/max values are not included in the steps number
        steps = (maxValue - minValue) - 1,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewEqualizer() {
    val bands = listOf<UShort>(100u, 200u, 400u, 800u, 1600u, 3200u, 6400u, 12000u)
    var values by remember { mutableStateOf(listOf<Short>(0, 0, 0, 0, 0, 0, 0, 0)) }
    OpenSCQ30Theme {
        Equalizer(
            bands = bands,
            values = values,
            minValue = -120,
            maxValue = 135,
            fractionDigits = 1,
            onValueChange = { changedIndex, changedValue ->
                values = values.mapIndexed { index, value ->
                    if (index == changedIndex) {
                        changedValue
                    } else {
                        value
                    }
                }
            },
        )
    }
}
