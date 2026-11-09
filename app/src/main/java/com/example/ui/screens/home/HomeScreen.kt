package com.example.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AnkitVerseRepository
import com.example.ui.components.AnkitMediaImage
import com.example.ui.components.AnkitVideoPlayer
import com.example.ui.components.CommentsBottomSheet
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: AnkitVerseRepository,
    currentUser: UserEntity?,
    onOpenStory: (StoryEntity) -> Unit,
    onNavigateToCreatePost: () -> Unit
) {
    val posts by repository.observeAllPosts().collectAsState(initial = emptyList())
    val stories by repository.observeActiveStories().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeCommentPostId by remember { mutableStateOf<String?>(null) }

    // Story media picker for quick story upload
    val storyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val isVideo = context.contentResolver.getType(uri)?.startsWith("video") == true
            scope.launch {
                repository.createStory(
                    mediaUri = uri.toString(),
                    mediaType = if (isVideo) "VIDEO" else "IMAGE",
                    caption = "Live from AnkitVerse ✨"
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
            .statusBarsPadding()
            .testTag("home_screen_root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AnkitVerse",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                }

                // Quick Add Post / Story button
                RainbowSmokeBox(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CosmicDarkSurface)
                        .border(1.dp, CosmicGlassBorder, CircleShape)
                        .testTag("button_header_create"),
                    onClick = onNavigateToCreatePost
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 76.dp) // accommodate floating bottom bar
            ) {
                // Horizontal Stories Tray
                item(key = "stories_tray") {
                    StoriesTray(
                        currentUser = currentUser,
                        stories = stories,
                        onAddStoryClick = {
                            storyPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        onStoryClick = onOpenStory
                    )
                }

                // Empty State if no posts
                if (posts.isEmpty()) {
                    item(key = "empty_feed") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IosGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Your Live Universe is Fresh",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Zero pre-made dummy data! You are in the creator seat.\nTap '+' above or Studio to upload your first real video/photo post.",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    RainbowSmokeBox(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(NeonViolet, ElectricCyan)
                                                )
                                            )
                                            .padding(horizontal = 20.dp, vertical = 10.dp)
                                            .testTag("button_first_post"),
                                        onClick = onNavigateToCreatePost
                                    ) {
                                        Text(
                                            text = "Create First Post 🎬",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            repository = repository,
                            onCommentClick = { activeCommentPostId = post.id },
                            onShareClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out this post on AnkitVerse: ${post.caption}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Post"))
                            }
                        )
                    }
                }
            }
        }

        // Active Comments Sheet
        if (activeCommentPostId != null) {
            CommentsBottomSheet(
                targetId = activeCommentPostId!!,
                targetType = "POST",
                repository = repository,
                onDismiss = { activeCommentPostId = null }
            )
        }
    }
}

@Composable
fun StoriesTray(
    currentUser: UserEntity?,
    stories: List<StoryEntity>,
    onAddStoryClick: () -> Unit,
    onStoryClick: (StoryEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Current user add story
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onAddStoryClick() }
                    .testTag("add_story_item")
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = currentUser?.avatarUri?.ifBlank { null },
                        contentDescription = "My Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(CosmicCardBg)
                            .border(1.5.dp, CosmicGlassBorder, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .border(2.dp, CosmicDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = CosmicDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "Your Story",
                    fontSize = 11.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        items(stories, key = { it.id }) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onStoryClick(story) }
                    .testTag("story_${story.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(listOf(NeonViolet, ElectricCyan, NeonPink, NeonViolet)),
                            shape = CircleShape
                        )
                        .padding(3.dp)
                ) {
                    AsyncImage(
                        model = story.authorAvatarUri.ifBlank { null },
                        contentDescription = story.authorUsername,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                Text(
                    text = "@${story.authorUsername}",
                    fontSize = 11.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PostCard(
    post: PostEntity,
    repository: AnkitVerseRepository,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val isLiked by repository.observeIsLiked(post.id).collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    var showHeartBurst by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0.2f) }

    LaunchedEffect(post.id) {
        repository.recordView(post.id, "POST", post.authorId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("post_card_${post.id}")
    ) {
        // Author Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.authorAvatarUri.ifBlank { null },
                contentDescription = post.authorUsername,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CosmicCardBg)
                    .border(1.dp, CosmicGlassBorder, CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.authorDisplayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "@${post.authorUsername}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (post.songTitle != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CosmicDarkSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Song",
                        tint = ElectricCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.songTitle,
                        fontSize = 11.sp,
                        color = ElectricCyan,
                        maxLines = 1
                    )
                }
            }
        }

        // Media Surface with Double Tap to Like
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(Color.Black)
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            scope.launch {
                                if (!isLiked) {
                                    repository.toggleLike(post.id, "POST")
                                }
                                showHeartBurst = true
                                heartScale.snapTo(0.3f)
                                heartScale.animateTo(
                                    targetValue = 1.3f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                delay(350)
                                showHeartBurst = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (post.mediaType == "VIDEO") {
                AnkitVideoPlayer(
                    videoUri = post.mediaUri,
                    autoPlay = false
                )
            } else {
                AnkitMediaImage(
                    mediaUri = post.mediaUri,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bursting Heart Animation
            if (showHeartBurst) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Liked",
                    tint = DangerRed,
                    modifier = Modifier
                        .size(110.dp)
                        .scale(heartScale.value)
                )
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    scope.launch { repository.toggleLike(post.id, "POST") }
                },
                modifier = Modifier.testTag("button_like_${post.id}")
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) DangerRed else TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onCommentClick,
                modifier = Modifier.testTag("button_comment_${post.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier.testTag("button_share_${post.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Likes Count & Caption
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(
                text = "${post.likesCount} likes • ${post.viewsCount} views",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )

            if (post.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = "@${post.authorUsername} ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = post.caption,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            if (post.commentsCount > 0) {
                Text(
                    text = "View all ${post.commentsCount} comments",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}
