package com.example.ui.screens.editor

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.SoundEntity
import com.example.data.repository.AnkitVerseRepository
import com.example.ui.components.AnkitVideoPlayer
import com.example.ui.components.IosGlassCard
import com.example.ui.components.RainbowSmokeBox
import com.example.ui.theme.CosmicCardBg
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.CosmicGlassBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class EditorTool {
    TIMELINE, FILTERS, TRANSITIONS, TEXT, AUDIO
}

data class VideoFilter(
    val name: String,
    val overlayColor: Color,
    val alpha: Float
)

val AvailableFilters = listOf(
    VideoFilter("Normal", Color.Transparent, 0f),
    VideoFilter("Neon Cyber", NeonViolet, 0.28f),
    VideoFilter("Sunset", SunsetGold, 0.25f),
    VideoFilter("Electric", ElectricCyan, 0.25f),
    VideoFilter("Noir", Color.Black, 0.35f),
    VideoFilter("Vivid Pink", NeonPink, 0.22f)
)

val TrendingSounds = listOf(
    SoundEntity("snd_1", "Cosmic Resonance", "Ankit Beats", null, null, 24, 1840),
    SoundEntity("snd_2", "Midnight City Drift", "Neon Pulse", null, null, 18, 920),
    SoundEntity("snd_3", "Cyberpunk Horizon", "Future Core", null, null, 30, 2410),
    SoundEntity("snd_4", "Lo-Fi Coffee Sunset", "Chill Verse", null, null, 22, 650)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    repository: AnkitVerseRepository,
    initialSongTitle: String? = null,
    initialSongArtist: String? = null,
    initialSongAudioUri: String? = null,
    onPublishSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedVideoUri by remember { mutableStateOf<String?>(null) }
    var selectedTool by remember { mutableStateOf(EditorTool.TIMELINE) }

    // Audio sound reuse state
    var activeSongTitle by remember { mutableStateOf(initialSongTitle ?: "Original Audio - AnkitVerse") }
    var activeSongArtist by remember { mutableStateOf(initialSongArtist ?: "Creator Sound") }
    var activeSongUri by remember { mutableStateOf(initialSongAudioUri) }

    // Filter state
    var selectedFilter by remember { mutableStateOf(AvailableFilters[0]) }

    // Transition state
    var selectedTransition by remember { mutableStateOf("Crossfade") }

    // Animated Text Overlay state
    var overlayText by remember { mutableStateOf("") }
    var textColor by remember { mutableStateOf(Color.White) }
    var textAnimation by remember { mutableStateOf("Neon Glow") }

    // Timeline state
    var trimStart by remember { mutableFloatStateOf(0f) }
    var trimEnd by remember { mutableFloatStateOf(15f) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

    // Export & Publish State
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var captionText by remember { mutableStateOf("") }
    var showPublishSheet by remember { mutableStateOf(false) }

    // Device Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri.toString()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("video_editor_screen_root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "CapCut Studio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )

                // Export / Next Button
                RainbowSmokeBox(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selectedVideoUri != null)
                                Brush.horizontalGradient(listOf(NeonViolet, ElectricCyan))
                            else
                                Brush.horizontalGradient(listOf(CosmicDarkSurface, CosmicDarkSurface))
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("button_studio_export"),
                    enabled = selectedVideoUri != null && !isExporting,
                    onClick = { showPublishSheet = true }
                ) {
                    Text(
                        text = "Export 🚀",
                        color = if (selectedVideoUri != null) Color.White else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Video Preview Surface / Monitor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .background(Color.Black)
                    .clickable {
                        if (selectedVideoUri == null) {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedVideoUri != null) {
                    AnkitVideoPlayer(
                        videoUri = selectedVideoUri!!,
                        autoPlay = true,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Creative Filter Color Tint Overlay
                    if (selectedFilter.alpha > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(selectedFilter.overlayColor.copy(alpha = selectedFilter.alpha))
                        )
                    }

                    // Animated Text Overlay on Video
                    if (overlayText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                                .background(Color(0x66000000), RoundedCornerShape(8.dp))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = overlayText,
                                color = textColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Active Sound badge on monitor
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x88000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeSongTitle,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                } else {
                    // Empty clip picker prompt
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap to pick video from device",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Or record a clip from your camera",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Timeline Scrubber & Clip Info Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDarkSurface)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trim: ${"%.1f".format(trimStart)}s — ${"%.1f".format(trimEnd)}s",
                        fontSize = 12.sp,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Speed: ${playbackSpeed}x",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Scrubber Slider
                Slider(
                    value = trimEnd,
                    onValueChange = { trimEnd = it },
                    valueRange = 1f..30f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = NeonViolet,
                        inactiveTrackColor = CosmicCardBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tool Switcher Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EditorTool.entries.forEach { tool ->
                    val isSelected = tool == selectedTool
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonViolet else CosmicDarkSurface)
                            .clickable { selectedTool = tool }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tool.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            // Tool Options Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                when (selectedTool) {
                    EditorTool.TIMELINE -> {
                        Column {
                            Text(
                                text = "Timeline Editing Tools",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Split Clip
                                IosGlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            Toast.makeText(context, "Clip split at current playhead!", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.ContentCut, contentDescription = null, tint = ElectricCyan)
                                        Text("Split Clip", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }

                                // Speed Control
                                IosGlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            playbackSpeed = when (playbackSpeed) {
                                                0.5f -> 1.0f
                                                1.0f -> 1.5f
                                                1.5f -> 2.0f
                                                else -> 0.5f
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = SunsetGold)
                                        Text("${playbackSpeed}x Speed", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }

                                // Replace Video
                                IosGlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            videoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                            )
                                        },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = NeonPink)
                                        Text("Change", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    EditorTool.FILTERS -> {
                        Column {
                            Text(
                                text = "CapCut Creative Filters",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AvailableFilters.forEach { filter ->
                                    val isFilterSelected = filter == selectedFilter
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable { selectedFilter = filter }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(filter.overlayColor.ifZeroAlpha(CosmicCardBg))
                                                .border(
                                                    width = if (isFilterSelected) 2.dp else 1.dp,
                                                    color = if (isFilterSelected) ElectricCyan else CosmicGlassBorder,
                                                    shape = RoundedCornerShape(14.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isFilterSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                            }
                                        }
                                        Text(
                                            text = filter.name,
                                            fontSize = 11.sp,
                                            color = if (isFilterSelected) ElectricCyan else TextSecondary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    EditorTool.TRANSITIONS -> {
                        Column {
                            Text(
                                text = "Visual Transitions",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("Crossfade", "Zoom In", "Glitch Flash", "Slide Left", "Wipe Up").forEach { trans ->
                                    val isTransSelected = trans == selectedTransition
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isTransSelected) RoyalBlue else CosmicDarkSurface)
                                            .clickable { selectedTransition = trans }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = trans,
                                            color = if (isTransSelected) Color.White else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    EditorTool.TEXT -> {
                        Column {
                            Text(
                                text = "Animated Overlay Text",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = overlayText,
                                onValueChange = { overlayText = it },
                                placeholder = { Text("Enter viral caption overlay...") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = CosmicGlassBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(Color.White, ElectricCyan, SunsetGold, NeonPink, NeonViolet).forEach { col ->
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (textColor == col) 2.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable { textColor = col }
                                    )
                                }
                            }
                        }
                    }

                    EditorTool.AUDIO -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current Audio Track",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text(
                                text = "🎵 $activeSongTitle • $activeSongArtist",
                                color = ElectricCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Trending AnkitVerse Sounds:",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            TrendingSounds.forEach { snd ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CosmicDarkSurface)
                                        .clickable {
                                            activeSongTitle = snd.title
                                            activeSongArtist = snd.artist
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Audiotrack, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(snd.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${snd.artist} • ${snd.usageCount} reels", color = TextSecondary, fontSize = 10.sp)
                                    }
                                    Text("Select", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }

        // Publish & Export Bottom Sheet
        if (showPublishSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPublishSheet = false },
                containerColor = CosmicDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Publish Edited Video",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        label = { Text("Caption & Hashtags") },
                        placeholder = { Text("Created with CapCut Studio in AnkitVerse #viral") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CosmicGlassBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isExporting) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = ElectricCyan, strokeWidth = 3.dp)
                            Text(
                                text = "Encoding HD Video & Syncing Live...",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    } else {
                        // Publish to Reels button
                        RainbowSmokeBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.horizontalGradient(listOf(NeonViolet, ElectricCyan)))
                                .testTag("publish_to_reels_button"),
                            onClick = {
                                if (selectedVideoUri != null) {
                                    isExporting = true
                                    scope.launch {
                                        delay(800) // Realistic rendering progress
                                        repository.createReel(
                                            videoUri = selectedVideoUri!!,
                                            caption = captionText.ifBlank { "Live Reel from Studio ✨" },
                                            songTitle = activeSongTitle,
                                            songArtist = activeSongArtist,
                                            songAudioUri = activeSongUri
                                        )
                                        isExporting = false
                                        showPublishSheet = false
                                        Toast.makeText(context, "Reel broadcast live to AnkitVerse! 🎬", Toast.LENGTH_SHORT).show()
                                        onPublishSuccess()
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "Post to Reels Feed 🎬",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Publish to Home Feed button
                        RainbowSmokeBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CosmicCardBg)
                                .border(1.dp, CosmicGlassBorder, RoundedCornerShape(14.dp))
                                .testTag("publish_to_feed_button"),
                            onClick = {
                                if (selectedVideoUri != null) {
                                    isExporting = true
                                    scope.launch {
                                        delay(800)
                                        repository.createPost(
                                            mediaUri = selectedVideoUri!!,
                                            mediaType = "VIDEO",
                                            caption = captionText.ifBlank { "New studio video! 🎬" },
                                            songTitle = activeSongTitle,
                                            songArtist = activeSongArtist,
                                            songAudioUri = activeSongUri
                                        )
                                        isExporting = false
                                        showPublishSheet = false
                                        Toast.makeText(context, "Post shared live to Feed! ✨", Toast.LENGTH_SHORT).show()
                                        onPublishSuccess()
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "Post to Home Feed 📱",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Color.ifZeroAlpha(fallback: Color): Color = if (alpha == 0f) fallback else this
