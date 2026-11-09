package com.example.ui.screens.reels

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.ReelEntity
import com.example.data.repository.AnkitVerseRepository
import com.example.ui.components.AnkitVideoPlayer
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.IosGlassCard
import com.example.ui.components.RainbowSmokeBox
import com.example.ui.theme.CosmicCardBg
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.CosmicGlassBorder
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ReelsScreen(
    repository: AnkitVerseRepository,
    onUseThisSound: (songTitle: String, songArtist: String, songAudioUri: String?) -> Unit,
    onNavigateToCreateReel: () -> Unit
) {
    val reels by repository.observeAllReels().collectAsState(initial = emptyList())
    var activeCommentReelId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    if (reels.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("reels_empty_screen"),
            contentAlignment = Alignment.Center
        ) {
            IosGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Shorts & Reels Feed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Real-time live video feed with YouTube-style open audio reuse.\nNo fake preloaded videos exist. Start the trend now!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    RainbowSmokeBox(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(NeonViolet, ElectricCyan))
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .testTag("button_create_first_reel"),
                        onClick = onNavigateToCreateReel
                    ) {
                        Text(
                            text = "Create First Reel 🎬",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { reels.size })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("reels_pager_root")
        ) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = reels[page]
                ReelItemView(
                    reel = reel,
                    repository = repository,
                    isCurrentPage = pagerState.currentPage == page,
                    onCommentClick = { activeCommentReelId = reel.id },
                    onShareClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Watch this Reel by @${reel.authorUsername} on AnkitVerse! 🎵 ${reel.songTitle}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Reel"))
                    },
                    onUseThisSound = {
                        onUseThisSound(reel.songTitle, reel.songArtist, reel.songAudioUri)
                    }
                )
            }

            // Top overlay brand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reels",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // Active Comments Sheet
            if (activeCommentReelId != null) {
                CommentsBottomSheet(
                    targetId = activeCommentReelId!!,
                    targetType = "REEL",
                    repository = repository,
                    onDismiss = { activeCommentReelId = null }
                )
            }
        }
    }
}

@Composable
fun ReelItemView(
    reel: ReelEntity,
    repository: AnkitVerseRepository,
    isCurrentPage: Boolean,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUseThisSound: () -> Unit
) {
    val isLiked by repository.observeIsLiked(reel.id).collectAsState(initial = false)
    val isFollowing by repository.observeIsFollowing(reel.authorId).collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    // Rotating Audio Disc Animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            repository.recordView(reel.id, "REEL", reel.authorId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reel_item_${reel.id}")
    ) {
        // Video Surface
        AnkitVideoPlayer(
            videoUri = reel.videoUri,
            autoPlay = isCurrentPage,
            modifier = Modifier.fillMaxSize()
        )

        // Right Action Rail
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        scope.launch { repository.toggleLike(reel.id, "REEL") }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x33000000), CircleShape)
                        .testTag("reel_like_${reel.id}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) DangerRed else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "${reel.likesCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x33000000), CircleShape)
                        .testTag("reel_comment_${reel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "${reel.commentsCount}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Share
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x33000000), CircleShape)
                        .testTag("reel_share_${reel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "Share",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Rotating Vinyl Sound Disc
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .rotate(rotation)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2230))
                    .border(2.dp, ElectricCyan, CircleShape)
                    .clickable { onUseThisSound() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Sound Disc",
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bottom Creator Info & YouTube-Style Open Audio Reuse Card
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 96.dp)
        ) {
            // Author Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                AsyncImage(
                    model = reel.authorAvatarUri.ifBlank { null },
                    contentDescription = reel.authorUsername,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "@${reel.authorUsername}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Follow button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFollowing) Color(0x55FFFFFF) else ElectricCyan)
                        .clickable {
                            scope.launch { repository.toggleFollow(reel.authorId) }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFollowing) Color.White else Color.Black
                    )
                }
            }

            // Caption
            if (reel.caption.isNotBlank()) {
                Text(
                    text = reel.caption,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // YouTube-Style Open Audio Track Card
            IosGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUseThisSound() }
                    .testTag("open_audio_card_${reel.id}"),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = Color(0x88121626)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Sound Wave",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reel.songTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = reel.songArtist,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // "Use This Sound" button
                    RainbowSmokeBox(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(listOf(NeonViolet, ElectricCyan))
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("button_use_this_sound_${reel.id}"),
                        onClick = onUseThisSound
                    ) {
                        Text(
                            text = "Use Sound 🎵",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
