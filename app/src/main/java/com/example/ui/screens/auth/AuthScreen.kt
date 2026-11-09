package com.example.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.repository.AnkitVerseRepository
import com.example.ui.components.IosGlassCard
import com.example.ui.components.RainbowSmokeBox
import com.example.ui.theme.CosmicDark
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
fun AuthScreen(
    repository: AnkitVerseRepository,
    onAuthSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Android Photo Picker for Profile Picture upload
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri.toString()
            errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("auth_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "AnkitVerse",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = if (isSignUp) "Create your creator identity" else "Welcome back to the universe",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Glassmorphism Auth Card
            IosGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Selector: Sign Up / Sign In
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CosmicDarkSurface)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSignUp) NeonViolet else Color.Transparent)
                                .clickable {
                                    isSignUp = true
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp)
                                .testTag("tab_signup"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign Up",
                                fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSignUp) TextPrimary else TextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isSignUp) NeonViolet else Color.Transparent)
                                .clickable {
                                    isSignUp = false
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp)
                                .testTag("tab_signin"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                fontWeight = if (!isSignUp) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isSignUp) TextPrimary else TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isSignUp) {
                        // Profile Picture Picker (Mandatory per Section 3)
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier
                                .size(96.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("profile_pic_picker")
                        ) {
                            if (avatarUri != null) {
                                AsyncImage(
                                    model = avatarUri,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, ElectricCyan, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(CosmicDarkSurface)
                                        .border(1.5.dp, CosmicGlassBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Placeholder",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
                            }

                            // Camera / Plus badge
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan)
                                    .border(2.dp, CosmicDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Pick Photo",
                                    tint = CosmicDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = if (avatarUri == null) "Tap to choose profile photo *" else "Photo selected ✓",
                            fontSize = 12.sp,
                            color = if (avatarUri == null) ElectricCyan else TextSecondary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )

                        // 1. Unique Username Field
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it.trim().lowercase() },
                            label = { Text("Unique Username *") },
                            placeholder = { Text("ankit_creator") },
                            leadingIcon = { Text("@", color = ElectricCyan, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            colors = outlinedColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_username")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Display Name Field
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            placeholder = { Text("Ankit Sharma") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                            singleLine = true,
                            colors = outlinedColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_display_name")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. Age Field
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 3) ageText = it },
                            label = { Text("Age (e.g. 21) *") },
                            placeholder = { Text("Must be 13+") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = outlinedColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_age")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password *") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                            colors = outlinedColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_password")
                        )
                    } else {
                        // Sign In Flow
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it.trim().lowercase() },
                            label = { Text("Username") },
                            placeholder = { Text("ankit_creator") },
                            leadingIcon = { Text("@", color = ElectricCyan, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            colors = outlinedColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_login_username")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                            colors = outlinedColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_login_password")
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = DangerRed,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Rainbow Smoke Touch Effect Action Button!
                    RainbowSmokeBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(NeonViolet, ElectricCyan)
                                )
                            )
                            .testTag("submit_auth_button"),
                        enabled = !isLoading,
                        onClick = {
                            errorMessage = null
                            if (isSignUp) {
                                val age = ageText.toIntOrNull() ?: 0
                                if (username.isBlank()) {
                                    errorMessage = "Please enter a unique username"
                                    return@RainbowSmokeBox
                                }
                                if (age < 13) {
                                    errorMessage = "You must be at least 13 years old"
                                    return@RainbowSmokeBox
                                }
                                if (password.length < 4) {
                                    errorMessage = "Password must be at least 4 characters"
                                    return@RainbowSmokeBox
                                }
                                val pfp = avatarUri ?: "android.resource://com.example/${com.example.R.drawable.creator_ankit_splash}"

                                isLoading = true
                                scope.launch {
                                    val result = repository.registerUser(
                                        username = username,
                                        displayName = displayName.ifBlank { username },
                                        age = age,
                                        password = password,
                                        avatarUri = pfp
                                    )
                                    isLoading = false
                                    result.fold(
                                        onSuccess = { onAuthSuccess() },
                                        onFailure = { errorMessage = it.message ?: "Registration failed" }
                                    )
                                }
                            } else {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter your username and password"
                                    return@RainbowSmokeBox
                                }
                                isLoading = true
                                scope.launch {
                                    val result = repository.loginUser(username, password)
                                    isLoading = false
                                    result.fold(
                                        onSuccess = { onAuthSuccess() },
                                        onFailure = { errorMessage = it.message ?: "Login failed" }
                                    )
                                }
                            }
                        }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUp) "Launch Account 🚀" else "Sign In ✨",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = ElectricCyan,
    unfocusedBorderColor = CosmicGlassBorder,
    focusedLabelColor = ElectricCyan,
    unfocusedLabelColor = TextSecondary,
    cursorColor = ElectricCyan
)
