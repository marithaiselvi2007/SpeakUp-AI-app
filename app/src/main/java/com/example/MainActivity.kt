package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.EnglishLevel
import com.example.data.model.PracticeTopic
import com.example.ui.components.AchievementsDialog
import com.example.ui.components.CelebrationRewardDialog
import com.example.ui.screens.auth.ProfileSetupScreen
import com.example.ui.screens.auth.WelcomeAuthScreen
import com.example.ui.screens.coach.CoachScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileSettingsScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.topics.SpeakingPracticeScreen
import com.example.ui.screens.topics.TopicsScreen
import com.example.ui.theme.SpeakUpTheme
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.CoachViewModel
import com.example.viewmodel.SpeakUpViewModel
import com.example.viewmodel.SpeakingPracticeViewModel
import kotlinx.coroutines.launch

sealed class AppNavRoutes(val route: String) {
    data object WelcomeAuth : AppNavRoutes("welcome_auth")
    data object ProfileSetup : AppNavRoutes("profile_setup")
    data object MainApp : AppNavRoutes("main_app")
    data object Coach : AppNavRoutes("coach_screen")
    data object SpeakingPractice : AppNavRoutes("speaking_practice_screen")
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val speakUpViewModel: SpeakUpViewModel by viewModels()
    private val coachViewModel: CoachViewModel by viewModels()
    private val speakingPracticeViewModel: SpeakingPracticeViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("MainActivity", "POST_NOTIFICATIONS granted: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure notification channels are set up
        com.example.notification.ScheduleNotificationManager.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS on Android 13+ if not already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val themeColor by speakUpViewModel.themeColor.collectAsState()
            val themeMode by speakUpViewModel.themeMode.collectAsState()
            val authState by authViewModel.uiState.collectAsState()

            val navController = rememberNavController()

            // Keep user session updated in speakUpViewModel, coachViewModel and speakingPracticeViewModel
            LaunchedEffect(authState) {
                if (authState is AuthUiState.Authenticated) {
                    val uid = (authState as AuthUiState.Authenticated).user.uid
                    speakUpViewModel.updateCurrentUserId(uid)
                    coachViewModel.updateCurrentUserId(uid)
                    speakingPracticeViewModel.updateCurrentUserId(uid)
                } else {
                    speakUpViewModel.clearUserData()
                    coachViewModel.clearMessages()
                    speakingPracticeViewModel.resetState()
                }
            }

            SpeakUpTheme(
                themeColor = themeColor,
                themeMode = themeMode
            ) {
                AppNavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    speakUpViewModel = speakUpViewModel,
                    coachViewModel = coachViewModel,
                    speakingPracticeViewModel = speakingPracticeViewModel
                )
            }
        }
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    speakUpViewModel: SpeakUpViewModel,
    coachViewModel: CoachViewModel,
    speakingPracticeViewModel: SpeakingPracticeViewModel
) {
    val authState by authViewModel.uiState.collectAsState()

    val startDestination = when (val state = authState) {
        is AuthUiState.Authenticated -> {
            if (state.isProfileCompleted) AppNavRoutes.MainApp.route else AppNavRoutes.ProfileSetup.route
        }
        else -> AppNavRoutes.WelcomeAuth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        // Welcome Auth Screen
        composable(AppNavRoutes.WelcomeAuth.route) {
            WelcomeAuthScreen(
                authViewModel = authViewModel,
                onNavigateToDashboard = {
                    navController.navigate(AppNavRoutes.MainApp.route) {
                        popUpTo(AppNavRoutes.WelcomeAuth.route) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(AppNavRoutes.ProfileSetup.route) {
                        popUpTo(AppNavRoutes.WelcomeAuth.route) { inclusive = true }
                    }
                }
            )
        }

        // Profile Setup Onboarding
        composable(AppNavRoutes.ProfileSetup.route) {
            ProfileSetupScreen(
                authViewModel = authViewModel,
                onProfileCompleted = {
                    speakUpViewModel.refreshData()
                    navController.navigate(AppNavRoutes.MainApp.route) {
                        popUpTo(AppNavRoutes.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Main App with Bottom Navigation
        composable(AppNavRoutes.MainApp.route) {
            MainAppScaffold(
                speakUpViewModel = speakUpViewModel,
                authViewModel = authViewModel,
                onStartSpeakingPractice = { topic ->
                    val userLevel = speakUpViewModel.userProfile.value?.englishLevel ?: EnglishLevel.BEGINNER
                    val authUserUid = (authState as? AuthUiState.Authenticated)?.user?.uid
                    val profileUid = speakUpViewModel.userProfile.value?.uid
                    val vmUid = speakUpViewModel.currentUserId.value
                    val uid = if (!authUserUid.isNullOrBlank()) authUserUid
                        else if (!profileUid.isNullOrBlank()) profileUid
                        else if (vmUid.isNotBlank()) vmUid
                        else "guest_user"
                    speakingPracticeViewModel.updateCurrentUserId(uid)
                    speakingPracticeViewModel.startSession(topic = topic, level = userLevel, userId = uid)
                    navController.navigate(AppNavRoutes.SpeakingPractice.route)
                },
                onStartDayChallenge = { dayNumber ->
                    val userLevel = speakUpViewModel.userProfile.value?.englishLevel ?: EnglishLevel.BEGINNER
                    val authUserUid = (authState as? AuthUiState.Authenticated)?.user?.uid
                    val profileUid = speakUpViewModel.userProfile.value?.uid
                    val vmUid = speakUpViewModel.currentUserId.value
                    val uid = if (!authUserUid.isNullOrBlank()) authUserUid
                        else if (!profileUid.isNullOrBlank()) profileUid
                        else if (vmUid.isNotBlank()) vmUid
                        else "guest_user"
                    speakingPracticeViewModel.updateCurrentUserId(uid)
                    speakingPracticeViewModel.startDayChallengeSession(dayNumber = dayNumber, level = userLevel, userId = uid)
                    navController.navigate(AppNavRoutes.SpeakingPractice.route)
                },
                onNavigateToAiCoach = {
                    val userLevel = speakUpViewModel.userProfile.value?.englishLevel ?: EnglishLevel.BEGINNER
                    val authUserUid = (authState as? AuthUiState.Authenticated)?.user?.uid
                    val profileUid = speakUpViewModel.userProfile.value?.uid
                    val authVmUid = authViewModel.currentUserProfile.value?.uid
                    val uid = if (!authUserUid.isNullOrBlank()) authUserUid
                        else if (!profileUid.isNullOrBlank()) profileUid
                        else if (!authVmUid.isNullOrBlank()) authVmUid
                        else "guest_user"
                    coachViewModel.updateCurrentUserId(uid, userLevel)
                    coachViewModel.startFreeConversation(level = userLevel)
                    navController.navigate(AppNavRoutes.Coach.route)
                },
                onSignOut = {
                    authViewModel.signOut()
                    speakUpViewModel.clearUserData()
                    speakingPracticeViewModel.resetState()
                    coachViewModel.clearMessages()
                    navController.navigate(AppNavRoutes.WelcomeAuth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Structured Speaking Practice Screen (15 Topics, Live Timer, Verbatim Transcript & Result)
        composable(
            route = AppNavRoutes.SpeakingPractice.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300))
            }
        ) {
            val profile by speakUpViewModel.userProfile.collectAsState()
            SpeakingPracticeScreen(
                practiceViewModel = speakingPracticeViewModel,
                dailyGoalMinutes = profile?.dailyGoalMinutes ?: 30,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPrepareNextDayChallenge = {
                    speakUpViewModel.prepareNextDayChallenge()
                },
                onSessionFinished = {
                    val authUserUid = (authState as? AuthUiState.Authenticated)?.user?.uid
                    val vmUid = speakUpViewModel.currentUserId.value
                    val uid = if (!authUserUid.isNullOrBlank()) authUserUid else if (vmUid.isNotBlank()) vmUid else "guest_user"
                    speakUpViewModel.recordPracticeSessionCompleted(uid)
                    speakUpViewModel.refreshData()
                    navController.popBackStack()
                }
            )
        }

        // Natural AI Speaking Coach Screen (Free Conversation, Intent Analysis, Inline Corrections)
        composable(
            route = AppNavRoutes.Coach.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300))
            }
        ) {
            CoachScreen(
                coachViewModel = coachViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun MainAppScaffold(
    speakUpViewModel: SpeakUpViewModel,
    authViewModel: AuthViewModel,
    onStartSpeakingPractice: (PracticeTopic) -> Unit,
    onStartDayChallenge: (Int) -> Unit = {},
    onNavigateToAiCoach: () -> Unit,
    onSignOut: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val profile by speakUpViewModel.userProfile.collectAsState()
    val scores by speakUpViewModel.dashboardScores.collectAsState()
    val activeCelebration by speakUpViewModel.activeCelebrationMilestone.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(profile?.uid) {
        val uid = profile?.uid?.trim().orEmpty()
        if (uid.isNotBlank() && uid != "default_user") {
            speakUpViewModel.checkForCelebrationMilestones(uid)
        }
    }

    // Automatically sync schedule notification when profile preferences change
    LaunchedEffect(profile?.preferredPracticeTime, profile?.practiceDays, profile?.isPracticeAlarmEnabled, profile?.uid) {
        profile?.let { p ->
            com.example.notification.ScheduleNotificationManager.schedulePracticeReminder(
                context = context,
                preferredTime = p.preferredPracticeTime,
                practiceDays = p.practiceDays,
                isEnabled = p.isPracticeAlarmEnabled,
                userId = p.uid
            )
        }
    }

    val userName = profile?.name?.takeIf { it.isNotBlank() } ?: "Learner"
    val userEmail = profile?.email?.takeIf { it.isNotBlank() } ?: ""
    val avatar = profile?.photoUrl?.takeIf { it.isNotBlank() } ?: "👨‍🎓"
    val userLevel = profile?.englishLevel ?: EnglishLevel.BEGINNER
    val dailyGoal = profile?.dailyGoalMinutes ?: 15

    if (showAchievementsDialog) {
        AchievementsDialog(
            scores = scores,
            onDismiss = { showAchievementsDialog = false }
        )
    }

    activeCelebration?.let { celebration ->
        CelebrationRewardDialog(
            milestone = celebration,
            onDismiss = { speakUpViewModel.dismissCelebration() }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout of SpeakUp AI?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        authViewModel.signOut()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("drawer_dialog_confirm_logout")
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(310.dp)
                    .testTag("dashboard_navigation_drawer"),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    // Decorative Drawer Header with User Profile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                com.example.ui.components.UserProfileAvatar(
                                    photoUrl = profile?.photoUrl,
                                    size = 54.dp,
                                    fallbackTextSize = 28.sp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = userName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (userEmail.isNotBlank()) {
                                        Text(
                                            text = userEmail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Badges strip
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = userLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Text(
                                        text = "🎯 ${dailyGoal}m/day",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MENU",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )

                    // 1. 🏠 Home
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = {
                            Column {
                                Text(
                                    text = "Home",
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Dashboard & quick practice",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = selectedTabIndex == 0,
                        onClick = {
                            selectedTabIndex = 0
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("dashboard_menu_home"),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    // 2. 🎤 Speaking Practice
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                        label = {
                            Column {
                                Text(
                                    text = "Speaking Practice",
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "15 Structured practice topics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("dashboard_menu_speaking_practice"),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    // 3. 🤖 AI Coach
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                        label = {
                            Column {
                                Text(text = "AI Coach", fontWeight = FontWeight.Normal)
                                Text(
                                    text = "Free conversation & grammar guidance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onNavigateToAiCoach()
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("dashboard_menu_ai_coach"),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    // 4. 📊 Progress
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = {
                            Column {
                                Text(
                                    text = "Progress",
                                    fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Learning analytics & trends",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = selectedTabIndex == 3,
                        onClick = {
                            selectedTabIndex = 3
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("dashboard_menu_progress"),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    // 5. 🏆 Achievements
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                        label = {
                            Column {
                                Text(text = "Achievements", fontWeight = FontWeight.Normal)
                                Text(
                                    text = "Badges & speaking milestones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                showAchievementsDialog = true
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("dashboard_menu_achievements")
                    )

                    // 6. 👤 Profile
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = {
                            Column {
                                Text(
                                    text = "Profile",
                                    fontWeight = if (selectedTabIndex == 4) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Personal goals & learning stats",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = selectedTabIndex == 4,
                        onClick = {
                            selectedTabIndex = 4
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("dashboard_menu_profile"),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    // 7. ⚙️ Settings
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = {
                            Column {
                                Text(
                                    text = "Settings",
                                    fontWeight = if (selectedTabIndex == 5) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Practice schedule & preferences",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = selectedTabIndex == 5,
                        onClick = {
                            selectedTabIndex = 5
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("dashboard_menu_settings"),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(12.dp))

                    // 8. 🚪 Logout
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        label = {
                            Column {
                                Text(
                                    text = "Logout",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Sign out of your account",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                showLogoutConfirmDialog = true
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("dashboard_menu_logout")
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "bottom_nav_home"),
                        BottomNavItem("Practice", Icons.Filled.Mic, Icons.Outlined.Mic, "bottom_nav_practice"),
                        BottomNavItem("AI Coach", Icons.Filled.Psychology, Icons.Outlined.Psychology, "bottom_nav_coach"),
                        BottomNavItem("Progress", Icons.Filled.BarChart, Icons.Outlined.BarChart, "bottom_nav_progress"),
                        BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, "bottom_nav_profile")
                    )

                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTabIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            alwaysShowLabel = true,
                            onClick = {
                                if (index == 2) {
                                    onNavigateToAiCoach()
                                } else {
                                    selectedTabIndex = index
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTabIndex) {
                    0 -> DashboardScreen(
                        speakUpViewModel = speakUpViewModel,
                        onStartPracticeTopic = onStartSpeakingPractice,
                        onNavigateToAiCoach = onNavigateToAiCoach,
                        onNavigateToProfile = { selectedTabIndex = 4 },
                        onNavigateToTopics = { selectedTabIndex = 1 },
                        onNavigateToProgress = { selectedTabIndex = 3 },
                        onNavigateToSettings = { selectedTabIndex = 5 },
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        },
                        onOpenAchievements = {
                            showAchievementsDialog = true
                        },
                        onSignOut = {
                            authViewModel.signOut()
                            onSignOut()
                        }
                    )
                    1 -> TopicsScreen(
                        onSelectTopic = onStartSpeakingPractice,
                        speakUpViewModel = speakUpViewModel,
                        onStartDayChallenge = onStartDayChallenge,
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                    2 -> {
                        TopicsScreen(
                            onSelectTopic = onStartSpeakingPractice,
                            speakUpViewModel = speakUpViewModel,
                            onStartDayChallenge = onStartDayChallenge,
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                    3 -> ProgressScreen(
                        speakUpViewModel = speakUpViewModel,
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                    4 -> ProfileScreen(
                        authViewModel = authViewModel,
                        speakUpViewModel = speakUpViewModel,
                        onNavigateToSpeakingPractice = { selectedTabIndex = 1 },
                        onNavigateToSettings = { selectedTabIndex = 5 },
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                    5 -> SettingsScreen(
                        authViewModel = authViewModel,
                        speakUpViewModel = speakUpViewModel,
                        onSignOutClicked = {
                            authViewModel.signOut()
                            onSignOut()
                        },
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        },
                        onDone = {
                            selectedTabIndex = 0
                        }
                    )
                }
            }
        }
    }
}
