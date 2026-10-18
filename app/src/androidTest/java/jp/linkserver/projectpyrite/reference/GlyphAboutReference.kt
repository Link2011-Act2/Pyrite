// Test-only source extraction; copyright (c) 2026 Azul1nk, MIT; see NOTICE.md.
// Only package/name, resource bindings and app-owned artwork/fonts are substituted.
// Production code never depends on these reference fixtures.
package jp.linkserver.projectpyrite.reference
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

@Composable
internal fun GlyphAboutReference(
    channelName: String,
    channelLabel: String,
    versionName: String,
    simpleVersion: String,
    versionCode: Int,
    buildNumber: String,
    nothingStyleEnabled: Boolean,
    onVersionClick: () -> Unit,
    onChannelClick: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val cardBorder: BorderStroke? = null
    val titleFontFamily = if (nothingStyleEnabled) FontFamily.SansSerif else null
    val gap = 3.dp
    val cardRadius = 20.dp
    val heroHeight = 176.dp
    val bridgeLength = 48.dp
    val bridgeOverlap = 1.dp
    val gutterColor = MaterialTheme.colorScheme.surface

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val squareSize = (maxWidth - gap) * 0.5f

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            Surface(
                onClick = onVersionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight),
                shape = RoundedCornerShape(cardRadius),
                color = cardColor,
                border = cardBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(92.dp),
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        ReferenceLogo()
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Pyrite",
                            fontFamily = titleFontFamily,
                            fontWeight = if (nothingStyleEnabled) FontWeight.Normal else FontWeight.Bold,
                            fontSize = 27.sp,
                            lineHeight = 32.sp
                        )
                        val releaseLine = "v$versionName ($versionCode)".replace(' ', '\u00A0')
                        Text(
                            text = "$channelLabel $releaseLine",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 21.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(squareSize)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(squareSize),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    Surface(
                        onClick = onChannelClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(squareSize),
                        shape = RoundedCornerShape(cardRadius),
                        color = cardColor,
                        border = cardBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(22.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Channel",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = channelName,
                                fontFamily = titleFontFamily,
                                fontWeight = if (nothingStyleEnabled) FontWeight.Normal else FontWeight.Bold,
                                fontSize = 25.sp,
                                lineHeight = 29.sp
                            )
                        }
                    }

                    Surface(
                        onClick = onVersionClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(squareSize),
                        shape = RoundedCornerShape(cardRadius),
                        color = cardColor,
                        border = cardBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(22.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Build",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = buildNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 16.sp
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Version",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "v$simpleVersion",
                                    fontFamily = if (nothingStyleEnabled) {
                                        FontFamily.Monospace
                                    } else {
                                        null
                                    },
                                    fontWeight = if (nothingStyleEnabled) FontWeight.Normal else FontWeight.Bold,
                                    fontSize = 25.sp,
                                    lineHeight = 29.sp
                                )
                            }
                        }
                    }
                }

                val horizontalBridgeStart = (squareSize - bridgeLength) * 0.5f
                val verticalBridgeStart = (squareSize - bridgeLength) * 0.5f

                Box(
                    modifier = Modifier
                        .offset(
                            x = horizontalBridgeStart,
                            y = -gap - bridgeOverlap
                        )
                        .width(bridgeLength)
                        .height(gap + bridgeOverlap * 2f)
                        .background(cardColor)
                )
                Box(
                    modifier = Modifier
                        .offset(
                            x = horizontalBridgeStart - gap * 0.5f,
                            y = -gap
                        )
                        .size(gap)
                        .background(gutterColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(
                            x = horizontalBridgeStart + bridgeLength - gap * 0.5f,
                            y = -gap
                        )
                        .size(gap)
                        .background(gutterColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(
                            x = squareSize - bridgeOverlap,
                            y = verticalBridgeStart
                        )
                        .width(gap + bridgeOverlap * 2f)
                        .height(bridgeLength)
                        .background(cardColor)
                )
                Box(
                    modifier = Modifier
                        .offset(
                            x = squareSize,
                            y = verticalBridgeStart - gap * 0.5f
                        )
                        .size(gap)
                        .background(gutterColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(
                            x = squareSize,
                            y = verticalBridgeStart + bridgeLength - gap * 0.5f
                        )
                        .size(gap)
                        .background(gutterColor, CircleShape)
                )
            }
        }
    }
}

