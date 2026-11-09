package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IosGlassCard
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinishSplash: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.85f) }
    val rotateAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Pulse animation for avatar
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        rotateAnim.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    LaunchedEffect(Unit) {
        // Hold for 2.6 seconds per Section 1 requirements
        delay(2600)
        onFinishSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        // Background cosmic glow
        Box(
            modifier = Modifier
                .size(360.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonViolet.copy(alpha = 0.25f),
                            ElectricCyan.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            // Rotating cosmic border around creator photo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(186.dp)
                        .rotate(rotateAnim.value)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    ElectricCyan,
                                    NeonViolet,
                                    NeonPink,
                                    ElectricCyan
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Designated Creator Photo
                Image(
                    painter = painterResource(id = R.drawable.creator_ankit_splash),
                    contentDescription = "Ankit - Founder & Creator",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(164.dp)
                        .scale(scaleAnim.value)
                        .clip(CircleShape)
                        .border(2.dp, Color(0x66FFFFFF), CircleShape)
                        .testTag("creator_splash_photo")
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Name
            Text(
                text = "AnkitVerse",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Designated personal creator caption
            IosGlassCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "\"Welcome to AnkitVerse – Founded by Ankit.\nWhere video, viral sounds & creators thrive.\"",
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Cosmic loader pill
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(NeonViolet, ElectricCyan, NeonPink)
                        )
                    )
            )
        }
    }
}
