package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    repository: AnkitVerseRepository,
    currentUser: UserEntity?,
    onOpenMonetization: () -> Unit,
    onSignOut: () -> Unit
) {
    val uid = currentUser?.id ?: ""
    val userPosts by repository.observeUserPosts(uid).collectAsState(initial = emptyList())
    val userReels by repository.observeUserReels(uid).collectAsState(initial = emptyList())
    var selectedGridTab by remember { mutableIntStateOf(0) } // 0 = Posts, 1 = Reels

    val totalViews = currentUser?.totalViews ?: 0L
    val isMonetized = currentUser?.isMonetized ?: false
    val progress = (totalViews.toFloat() / AnkitVerseRepository.MONETIZATION_VIEW_THRESHOLD.toFloat()).coerceIn(0f, 1f)
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
            .statusBarsPadding()
            .testTag("profile_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${currentUser?.username ?: "creator"}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (isMonetized) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Monetized Partner",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("button_profile_signout")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = DangerRed
                    )
                }
            }

            // Profile Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Photo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(2.dp, ElectricCyan, CircleShape)
                        .padding(2.dp)
                ) {
                    AsyncImage(
                        model = currentUser?.avatarUri?.ifBlank { null },
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Stats Row
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(count = userPosts.size + userReels.size, label = "Uploads")
                    StatItem(count = currentUser?.followersCount ?: 0, label = "Followers")
                    StatItem(count = currentUser?.followingCount ?: 0, label = "Following")
                }
            }

            // Bio & Details
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    text = currentUser?.displayName ?: "Creator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Age: ${currentUser?.age ?: 21} • AnkitVerse Creator",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = currentUser?.bio ?: "Exploring the universe of video & audio ✨",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Creator Monetization Overview Card
            IosGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenMonetization() }
                    .testTag("card_monetization_overview"),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = CosmicDarkSurface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Monetization",
                                tint = if (isMonetized) SunsetGold else ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Creator Monetization",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isMonetized) SuccessGreen.copy(alpha = 0.2f) else CosmicCardBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isMonetized) "Active Partner ✨" else "${(progress * 100).toInt()}% towards unlock",
                                color = if (isMonetized) SuccessGreen else ElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isMonetized) SunsetGold else ElectricCyan,
                        trackColor = CosmicCardBg
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${numberFormatter.format(totalViews)} / 500,000 views",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = if (isMonetized) "$${"%.2f".format(currentUser?.earnings ?: 0.0)} Earned" else "Unlocks at 500k views",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMonetized) SunsetGold else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid Tab Selector (Posts vs Reels)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, CosmicGlassBorder, RoundedCornerShape(0.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedGridTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Posts",
                        tint = if (selectedGridTab == 0) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedGridTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Reels",
                        tint = if (selectedGridTab == 1) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Grid Content
            if (selectedGridTab == 0) {
                if (userPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No posts uploaded yet.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(userPosts, key = { it.id }) { post ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(1.dp)
                                    .background(CosmicDarkSurface)
                            ) {
                                AnkitMediaImage(
                                    mediaUri = post.mediaUri,
                                    contentDescription = post.caption,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            } else {
                if (userReels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No reels published yet.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(userReels, key = { it.id }) { reel ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(0.65f)
                                    .padding(1.dp)
                                    .background(CosmicDarkSurface)
                            ) {
                                AnkitMediaImage(
                                    mediaUri = reel.videoUri,
                                    contentDescription = reel.caption,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "▶ ${reel.viewsCount}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}
