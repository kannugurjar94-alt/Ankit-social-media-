package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AnkitVerseRepository
import com.example.data.session.SessionManager
import com.example.ui.components.AnkitBottomBar
import com.example.ui.components.NavTab
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.create.CreatePostScreen
import com.example.ui.screens.direct.ChatDetailScreen
import com.example.ui.screens.direct.DirectMessagesScreen
import com.example.ui.screens.editor.VideoEditorScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.monetization.MonetizationScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reels.ReelsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.stories.StoryViewerScreen
import com.example.ui.theme.AnkitVerseTheme
import com.example.ui.theme.CosmicDark

sealed class AppScreen {
    data object Splash : AppScreen()
    data object Auth : AppScreen()
    data object Main : AppScreen()
    data object CreatePost : AppScreen()
    data class StoryViewer(val story: StoryEntity) : AppScreen()
    data class ChatDetail(val user: UserEntity) : AppScreen()
    data object Monetization : AppScreen()
    data class EditorWithSound(
        val songTitle: String,
        val songArtist: String,
        val songAudioUri: String?
    ) : AppScreen()
}

class MainActivity : ComponentActivity() {
    private lateinit var repository: AnkitVerseRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        repository = AnkitVerseRepository(db, sessionManager)

        setContent {
            AnkitVerseTheme {
                AnkitVerseApp(repository = repository, sessionManager = sessionManager)
            }
        }
    }
}

@Composable
fun AnkitVerseApp(
    repository: AnkitVerseRepository,
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    val currentUser by repository.observeCurrentUser().collectAsState(initial = null)

    // Device Permissions Launcher (Section 4 requirement)
    val permissionsToRequest = remember {
        val list = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.READ_MEDIA_IMAGES)
            list.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        list.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    // Auto-request permissions on first launch
    LaunchedEffect(Unit) {
        val hasPermissions = permissionsToRequest.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        if (!hasPermissions) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                is AppScreen.Splash -> {
                    SplashScreen(
                        onFinishSplash = {
                            if (sessionManager.isLoggedIn) {
                                currentScreen = AppScreen.Main
                            } else {
                                currentScreen = AppScreen.Auth
                            }
                        }
                    )
                }

                is AppScreen.Auth -> {
                    AuthScreen(
                        repository = repository,
                        onAuthSuccess = {
                            currentScreen = AppScreen.Main
                        }
                    )
                }

                is AppScreen.Main -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = CosmicDark,
                        bottomBar = {
                            AnkitBottomBar(
                                currentTab = currentTab,
                                onTabSelected = { tab ->
                                    currentTab = tab
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (currentTab) {
                                NavTab.HOME -> {
                                    HomeScreen(
                                        repository = repository,
                                        currentUser = currentUser,
                                        onOpenStory = { story ->
                                            currentScreen = AppScreen.StoryViewer(story)
                                        },
                                        onNavigateToCreatePost = {
                                            currentScreen = AppScreen.CreatePost
                                        }
                                    )
                                }

                                NavTab.REELS -> {
                                    ReelsScreen(
                                        repository = repository,
                                        onUseThisSound = { songTitle, songArtist, songAudioUri ->
                                            currentScreen = AppScreen.EditorWithSound(
                                                songTitle = songTitle,
                                                songArtist = songArtist,
                                                songAudioUri = songAudioUri
                                            )
                                        },
                                        onNavigateToCreateReel = {
                                            currentTab = NavTab.EDITOR
                                        }
                                    )
                                }

                                NavTab.EDITOR -> {
                                    VideoEditorScreen(
                                        repository = repository,
                                        onPublishSuccess = {
                                            currentTab = NavTab.REELS
                                        },
                                        onBack = {
                                            currentTab = NavTab.HOME
                                        }
                                    )
                                }

                                NavTab.MESSAGES -> {
                                    DirectMessagesScreen(
                                        repository = repository,
                                        currentUserId = currentUser?.id,
                                        onOpenChat = { targetUser ->
                                            currentScreen = AppScreen.ChatDetail(targetUser)
                                        }
                                    )
                                }

                                NavTab.PROFILE -> {
                                    ProfileScreen(
                                        repository = repository,
                                        currentUser = currentUser,
                                        onOpenMonetization = {
                                            currentScreen = AppScreen.Monetization
                                        },
                                        onSignOut = {
                                            repository.signOut()
                                            currentScreen = AppScreen.Auth
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                is AppScreen.CreatePost -> {
                    BackHandler { currentScreen = AppScreen.Main }
                    CreatePostScreen(
                        repository = repository,
                        onPublishSuccess = {
                            currentScreen = AppScreen.Main
                            currentTab = NavTab.HOME
                        },
                        onBack = { currentScreen = AppScreen.Main }
                    )
                }

                is AppScreen.StoryViewer -> {
                    BackHandler { currentScreen = AppScreen.Main }
                    StoryViewerScreen(
                        story = screen.story,
                        onClose = { currentScreen = AppScreen.Main }
                    )
                }

                is AppScreen.ChatDetail -> {
                    BackHandler { currentScreen = AppScreen.Main }
                    ChatDetailScreen(
                        repository = repository,
                        otherUser = screen.user,
                        currentUserId = currentUser?.id,
                        onBack = { currentScreen = AppScreen.Main }
                    )
                }

                is AppScreen.Monetization -> {
                    BackHandler { currentScreen = AppScreen.Main }
                    MonetizationScreen(
                        repository = repository,
                        currentUser = currentUser,
                        onBack = { currentScreen = AppScreen.Main }
                    )
                }

                is AppScreen.EditorWithSound -> {
                    BackHandler { currentScreen = AppScreen.Main }
                    VideoEditorScreen(
                        repository = repository,
                        initialSongTitle = screen.songTitle,
                        initialSongArtist = screen.songArtist,
                        initialSongAudioUri = screen.songAudioUri,
                        onPublishSuccess = {
                            currentScreen = AppScreen.Main
                            currentTab = NavTab.REELS
                        },
                        onBack = { currentScreen = AppScreen.Main }
                    )
                }
            }
        }
    }
}
