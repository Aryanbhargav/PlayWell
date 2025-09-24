package com.video.playwell

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView

@ExperimentalMaterial3Api
@SuppressLint("Range")
@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun PlayerScreen(
    manifestUrl: String,
    licenseUrl: String,
    onReleasePlayer: () -> Unit
) {
    val context = LocalContext.current
    var showBottomsheet by remember { mutableStateOf(false) }

    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            parameters = buildUponParameters()
                .setPreferredAudioLanguage("en")
                .build()
        }
    }
    val player = remember {
        ExoPlayer.Builder(context).setTrackSelector(trackSelector).build().apply {
            val mediaItem = MediaItem.Builder()
                .setUri(manifestUrl)
                .setDrmConfiguration(
                    MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                        .setLicenseUri(licenseUrl)
                        .build()
                )
                .setMimeType("application/dash+xml")
                .build()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }


    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(256, 344, 344))) {
        val sheetState = rememberModalBottomSheetState()
        var selectedIndex by remember { mutableIntStateOf(-1) }
        val gradientBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFEAE19A), Color(0xFF9EA83C))
        )

            Column {
                Text(
                    modifier = Modifier.padding(16.dp, top = 40.dp, bottom = 20.dp),
                    text = "Playwell App",
                    textAlign = TextAlign.Start,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        brush = gradientBrush,
                        shadow = Shadow(
                            color = Color(0xFFF6ECAB), offset = Offset(2f, 2f), blurRadius = 4f
                        )
                    )
                )
                AndroidView(factory = { ctx ->

                    val pv = PlayerView(ctx)
                    pv.useController = true
                    pv.player = player
                    pv.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    pv
                }, modifier = Modifier.wrapContentHeight())
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showBottomsheet = true },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(212, 224, 98, 255),
                                contentColor = Color(6, 35, 35, 255),
                            )
                        ) {
                            Text("Change Resolution")
                        }
                    }

        }
       
        if (showBottomsheet) {
            val player = player
            val trackSelector = trackSelector

            var items by remember { mutableStateOf<List<String>>(emptyList()) }
            var mapping by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) } // (groupIndex, trackIndex)
            var rendererIndex by remember { mutableStateOf(-1) }

            LaunchedEffect(player) {
                if (player == null) return@LaunchedEffect
                val mapped = trackSelector.currentMappedTrackInfo ?: return@LaunchedEffect
                rendererIndex = (0 until mapped.rendererCount).firstOrNull { i ->
                    mapped.getRendererType(i) == C.TRACK_TYPE_VIDEO
                } ?: -1

                if (rendererIndex == -1) return@LaunchedEffect

                val groups = mapped.getTrackGroups(rendererIndex)
                val list = mutableListOf<String>()
                val map = mutableListOf<Pair<Int, Int>>()
                for (g in 0 until groups.length) {
                    val group = groups[g]
                    for (t in 0 until group.length) {
                        val f = group.getFormat(t)
                        val label = when {
                            f.width > 0 && f.height > 0 -> "${f.height}p (${f.width}x${f.height})"
                            else -> "bitrate ${f.bitrate}bps"
                        }
                        list.add(label)
                        map.add(Pair(g, t))
                    }
                }
                items = list
                mapping = map
            }
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {showBottomsheet=false},
                containerColor = Color(31, 30, 30, 255),

            ) {
                Text(
                    "Choose resolution",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                    color = Color(212, 224, 98, 255)
                )
                LazyColumn(
                    modifier = Modifier.padding(16.dp)
                ) {
                    itemsIndexed(items) { index, s ->
                        Text(
                            text = s,
                            color = Color(255,255,255),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (selectedIndex == index) Color(
                                        86,
                                        143,
                                        136,
                                        255
                                    ) else Color(31, 30, 30, 255),
                                )
                                .clickable {
                                    val pair = mapping[index]
                                    val groupIndex = pair.first
                                    val trackIndex = pair.second
                                    val mapped = trackSelector.currentMappedTrackInfo
                                    val groups = mapped?.getTrackGroups(rendererIndex)

                                    if (groups != null) {
                                        val group = groups[groupIndex]
                                        val override = DefaultTrackSelector.SelectionOverride(
                                            groupIndex,
                                            trackIndex
                                        )
                                        val newParams = trackSelector.buildUponParameters()
                                            .clearSelectionOverrides()
                                            .setSelectionOverride(rendererIndex, groups, override)
                                            .build()
                                        trackSelector.parameters = newParams
                                    }
                                    selectedIndex = index
                                    showBottomsheet = false
                                }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onReleasePlayer()
        }
    }
}
