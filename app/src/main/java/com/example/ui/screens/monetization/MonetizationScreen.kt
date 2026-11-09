package com.example.ui.screens.monetization

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AnkitVerseRepository
import com.example.ui.components.IosGlassCard
import com.example.ui.components.RainbowSmokeBox
import com.example.ui.theme.CosmicCardBg
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.CosmicGlassBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MonetizationScreen(
    repository: AnkitVerseRepository,
    currentUser: UserEntity?,
    onBack: () -> Unit
) {
    val totalViews = currentUser?.totalViews ?: 0L
    val isMonetized = currentUser?.isMonetized ?: false
    val earnings = currentUser?.earnings ?: 0.0
    val progress = (totalViews.toFloat() / AnkitVerseRepository.MONETIZATION_VIEW_THRESHOLD.toFloat()).coerceIn(0f, 1f)
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("monetization_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Creator Monetization",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Eligibility Card
            IosGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = CosmicDarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(if (isMonetized) SunsetGold.copy(alpha = 0.2f) else CosmicCardBg)
                            .border(1.5.dp, if (isMonetized) SunsetGold else ElectricCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMonetized) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isMonetized) SunsetGold else ElectricCyan,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isMonetized) "Creator Partner Status: Active ✨" else "Monetization Eligibility in Progress",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = if (isMonetized)
                            "Congratulations! You unlocked the AnkitVerse Creator Program at 500,000 views."
                        else
                            "Monetization automatically unlocks once your posts and reels reach 500,000 total views.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // Big Progress Bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (isMonetized) SunsetGold else ElectricCyan,
                        trackColor = CosmicCardBg
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${numberFormatter.format(totalViews)} Views",
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Target: 500,000 Views",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transparent Payout Tracking
            Text(
                text = "Transparent Payout Tracking",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            IosGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = CosmicDarkSurface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimated Balance",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "$${"%.2f".format(earnings)} USD",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SunsetGold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = SunsetGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Breakdown lines
                    PayoutLine(label = "CPM View Rate", value = "$0.08 / 1k views", icon = Icons.Default.Visibility)
                    PayoutLine(label = "Reels & Feed Ad Share", value = "$${"%.2f".format(earnings * 0.75)}", icon = Icons.Default.AttachMoney)
                    PayoutLine(label = "Sound Reuse Royalties", value = "$${"%.2f".format(earnings * 0.25)}", icon = Icons.Default.GraphicEq)

                    Spacer(modifier = Modifier.height(18.dp))

                    RainbowSmokeBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isMonetized && earnings > 0.0)
                                    Brush.horizontalGradient(listOf(SunsetGold, NeonPink))
                                else
                                    Brush.horizontalGradient(listOf(CosmicCardBg, CosmicCardBg))
                            )
                            .testTag("button_request_payout"),
                        enabled = isMonetized && earnings > 0.0,
                        onClick = {
                            Toast.makeText(context, "Payout request submitted to Creator Wallet! 💳", Toast.LENGTH_LONG).show()
                        }
                    ) {
                        Text(
                            text = if (isMonetized) "Request Payout 💸" else "Unlock at 500k Views 🔒",
                            color = if (isMonetized && earnings > 0.0) Color.Black else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Demo Milestone Testing Boost:
            // Allows testing the 500,000 views threshold instantly!
            IosGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = CosmicCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Milestone Simulator (+100k views)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Test automatic eligibility at 500,000 views",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    RainbowSmokeBox(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(NeonViolet, ElectricCyan)))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("button_boost_views"),
                        onClick = {
                            if (currentUser != null) {
                                scope.launch {
                                    repository.recordView("demo_boost", "POST", currentUser.id)
                                    // Add 100k views directly to test
                                    val db = com.example.data.local.AppDatabase.getDatabase(context)
                                    db.userDao().incrementViews(currentUser.id, 100_000L)
                                    val updated = db.userDao().getUserByIdSync(currentUser.id)
                                    if (updated != null) {
                                        val nowMonetized = updated.totalViews >= AnkitVerseRepository.MONETIZATION_VIEW_THRESHOLD
                                        val newEarn = (updated.totalViews / 1000.0) * AnkitVerseRepository.CPM_PAYOUT_RATE
                                        db.userDao().updateMonetization(currentUser.id, nowMonetized, newEarn)
                                    }
                                    Toast.makeText(context, "+100,000 views added! 🚀", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "+100k 🚀",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PayoutLine(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
