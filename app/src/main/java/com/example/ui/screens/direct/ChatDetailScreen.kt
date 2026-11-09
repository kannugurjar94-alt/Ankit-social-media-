package com.example.ui.screens.direct

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AnkitVerseRepository
import com.example.ui.components.AnkitMediaImage
import com.example.ui.components.IosGlassCard
import com.example.ui.components.RainbowSmokeBox
import com.example.ui.theme.CosmicCardBg
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.CosmicGlassBorder
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
    repository: AnkitVerseRepository,
    otherUser: UserEntity,
    currentUserId: String?,
    onBack: () -> Unit
) {
    val messages by repository.observeConversation(otherUser.id).collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Voice record pulse animation
    val recordScale = remember { Animatable(1f) }
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDuration = 0
            recordScale.animateTo(
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            recordScale.snapTo(1f)
        }
    }

    // Timer when recording
    LaunchedEffect(isRecordingVoice) {
        while (isRecordingVoice) {
            delay(1000)
            recordingDuration++
        }
    }

    // Media Picker for chat image / video attachments
    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val isVideo = context.contentResolver.getType(uri)?.startsWith("video") == true
            scope.launch {
                repository.sendMessage(
                    receiverId = otherUser.id,
                    text = if (isVideo) "📹 Video" else "📷 Photo",
                    mediaUri = uri.toString(),
                    mediaType = if (isVideo) "VIDEO" else "IMAGE"
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("chat_detail_screen_root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDarkSurface)
                    .border(0.5.dp, CosmicGlassBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                AsyncImage(
                    model = otherUser.avatarUri.ifBlank { null },
                    contentDescription = otherUser.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.dp, ElectricCyan, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = otherUser.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "@${otherUser.username} • Live in AnkitVerse",
                        fontSize = 11.sp,
                        color = ElectricCyan
                    )
                }
            }

            // Messages List (Instant sub-second live sync via Room Flow)
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No messages yet.\nSay hello to @${otherUser.username} with text, photos, or voice notes! 🎙️",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMine = msg.senderId == currentUserId
                        MessageBubble(msg = msg, isMine = isMine)
                    }
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDarkSurface)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media attachment button
                IconButton(
                    onClick = {
                        mediaPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach Media",
                        tint = ElectricCyan
                    )
                }

                if (isRecordingVoice) {
                    // Recording live state
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DangerRed.copy(alpha = 0.2f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(recordScale.value)
                                .clip(CircleShape)
                                .background(DangerRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Recording Voice Note... 00:${if (recordingDuration < 10) "0$recordingDuration" else recordingDuration}",
                            color = DangerRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Stop and Send Voice Note button
                    RainbowSmokeBox(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DangerRed),
                        onClick = {
                            isRecordingVoice = false
                            val duration = recordingDuration.coerceAtLeast(1)
                            scope.launch {
                                repository.sendMessage(
                                    receiverId = otherUser.id,
                                    text = "Voice Note (${duration}s)",
                                    isVoiceNote = true,
                                    voiceDurationSec = duration
                                )
                                Toast.makeText(context, "Voice note sent! 🎙️", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Send Voice", tint = Color.White)
                    }
                } else {
                    // Regular Text & Voice Mic bar
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message @${otherUser.username}...", color = TextSecondary, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CosmicGlassBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (inputText.isNotBlank()) {
                        RainbowSmokeBox(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(NeonViolet, ElectricCyan)))
                                .testTag("button_send_chat_msg"),
                            onClick = {
                                val text = inputText.trim()
                                if (text.isNotBlank()) {
                                    inputText = ""
                                    scope.launch {
                                        repository.sendMessage(
                                            receiverId = otherUser.id,
                                            text = text
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // Voice Note Record Toggle button
                        RainbowSmokeBox(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(CosmicCardBg)
                                .border(1.dp, CosmicGlassBorder, CircleShape)
                                .testTag("button_voice_record"),
                            onClick = { isRecordingVoice = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record Voice Note",
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    msg: MessageEntity,
    isMine: Boolean
) {
    val timeFormatted = remember(msg.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(msg.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isMine)
                        Brush.horizontalGradient(listOf(NeonViolet, RoyalBlue))
                    else
                        Brush.horizontalGradient(listOf(CosmicDarkSurface, CosmicDarkSurface))
                )
                .border(
                    width = 1.dp,
                    color = if (isMine) ElectricCyan.copy(alpha = 0.3f) else CosmicGlassBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (msg.mediaUri != null) {
                    AnkitMediaImage(
                        mediaUri = msg.mediaUri,
                        contentDescription = "Attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (msg.isVoiceNote) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isMine) Color.White.copy(alpha = 0.3f) else ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Voice Note",
                                tint = if (isMine) Color.White else ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Voice Note (${msg.voiceDurationSec}s)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "▶ ılıılıllıılıl",
                                fontSize = 12.sp,
                                color = if (isMine) Color.White else ElectricCyan
                            )
                        }
                    }
                } else {
                    Text(
                        text = msg.text,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = if (isMine) Color.White.copy(alpha = 0.7f) else TextSecondary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp)
                )
            }
        }
    }
}
