@file:OptIn(
    org.jetbrains.compose.resources.ExperimentalResourceApi::class,
    kotlin.ExperimentalMultiplatform::class
)
package com.mahmoud.azkarapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import coil3.compose.AsyncImage
import azkarapp.shared.generated.resources.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

// Exact colors from the screenshots
val HeaderGreen = Color(0xFF00894F)
val ButtonGreen = Color(0xFF00965E)
val TextBrown = Color(0xFF4E342E)
val InfoBarColor = Color(0xFFF7E6C4)
val InfoBarBorder = Color(0xFFD7B58E)
val AccentGold = Color(0xFFC5A358)
val BackgroundLight = Color(0xFFF9FBF9) // Softer off-white

sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    object Tasbih : Screen()
    object DuaCategories : Screen()
    object AzkarCategories : Screen()
    object HadithList : Screen()
    data class AzkarList(val category: String, val fromDua: Boolean = false) : Screen()
}

@Composable
fun AppBackground(
    isNightMode: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Slow rotation for the central pattern
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Subtle breathing/pulse effect for corners
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isNightMode) Color(0xFF0F171A) else Color(0xFFFDFBF7))
    ) {
        // Rotating Central Pattern
        Icon(
            painter = painterResource(Res.drawable.ic_islamic_pattern),
            contentDescription = null,
            tint = (if (isNightMode) Color.White else HeaderGreen).copy(alpha = 0.03f),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.9f)
                .graphicsLayer(rotationZ = rotation)
        )
        
        // Animated Corner Ornaments
        Icon(
            painter = painterResource(Res.drawable.ic_islamic_pattern),
            contentDescription = null,
            tint = (if (isNightMode) Color.White else AccentGold).copy(alpha = pulseAlpha),
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(200.dp)
                .graphicsLayer(
                    translationX = -250f, 
                    translationY = -250f,
                    rotationZ = rotation * 0.5f // Rotating slower in corners
                )
        )

        Icon(
            painter = painterResource(Res.drawable.ic_islamic_pattern),
            contentDescription = null,
            tint = (if (isNightMode) Color.White else AccentGold).copy(alpha = pulseAlpha),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(200.dp)
                .graphicsLayer(
                    translationX = 250f, 
                    translationY = 250f,
                    rotationZ = -rotation * 0.5f
                )
        )
        
        // Dynamic Gradient Overlay (Slightly shifts with animation)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isNightMode) {
                            listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                        } else {
                            listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                        },
                        center = androidx.compose.ui.geometry.Offset(500f, 500f),
                        radius = 1200f
                    )
                )
        )
        
        content()
    }
}

@OptIn(ExperimentalMultiplatform::class)
@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    val repository = remember { AzkarRepository() }
    var allAzkar by remember { mutableStateOf<List<Zekr>>(emptyList()) }
    var allHadiths by remember { mutableStateOf<List<Hadith>>(emptyList()) }
    var shouldShowUpdateDialog by remember { mutableStateOf(false) }

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LaunchedEffect(Unit) {
        // Simulate checking for mandatory update
        val platform = getPlatform()
        val currentVersion = platform.versionCode
        val requiredVersion = if (platform.isAndroid) 5 else 1 // Adjust iOS required version to 1 to match current app version
        
        if (currentVersion < requiredVersion) {
            shouldShowUpdateDialog = true
        }

        allAzkar = repository.loadAzkar()
        allHadiths = repository.loadHadiths()
        kotlinx.coroutines.delay(1500)
        currentScreen = Screen.Home
    }

    if (shouldShowUpdateDialog) {
        val platform = getPlatform()
        ForcedUpdateDialog(
            isAndroid = platform.isAndroid,
            onUpdateClick = {
                if (platform.isAndroid) {
                    uriHandler.openUri("https://play.google.com/store/apps/details?id=com.mahmoud.azkarapp")
                } else {
                    uriHandler.openUri("https://apps.apple.com/us/app/%D8%A3%D8%B0%D9%83%D8%A7%D8%B1-%D8%AD%D8%B5%D9%86-%D8%A7%D9%84%D9%85%D8%B3%D9%84%D9%85/id6822073243")
                }
            }
        )
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = HeaderGreen,
            secondary = AccentGold,
            background = BackgroundLight,
            surface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { screen ->
                when (screen) {
                    is Screen.Splash -> SplashScreen()
                    else -> MainContent(
                        screen = screen,
                        allAzkar = allAzkar,
                        allHadiths = allHadiths,
                        onNavigate = { currentScreen = it },
                        onBack = { 
                            if (currentScreen is Screen.DuaCategories) {
                                currentScreen = Screen.Home
                            } else if (currentScreen is Screen.HadithList) {
                                currentScreen = Screen.Home
                            } else if (currentScreen is Screen.AzkarList) {
                                currentScreen = if ((currentScreen as Screen.AzkarList).fromDua) {
                                    Screen.DuaCategories
                                } else {
                                    Screen.AzkarCategories
                                }
                            } else if (currentScreen is Screen.AzkarCategories) {
                                currentScreen = Screen.Home
                            } else if (currentScreen is Screen.Tasbih) {
                                currentScreen = Screen.Home
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeaderGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(180.dp),
                shadowElevation = 12.dp
            ) {
                Image(
                    painter = painterResource(Res.drawable.img_azkar),
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp).fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "حصن المسلم",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMultiplatform::class)
@Composable
fun MainContent(
    screen: Screen,
    allAzkar: List<Zekr>,
    allHadiths: List<Hadith>,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var isTasbihNightMode by remember { mutableStateOf(false) }
    val effectiveNightMode = screen is Screen.Tasbih && isTasbihNightMode

    if (showExitDialog) {
        // ... (remaining of showExitDialog logic)
        Dialog(onDismissRequest = { showExitDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Green Top Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HeaderGreen)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(130.dp).padding(bottom = 16.dp),
                            shadowElevation = 6.dp,
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.img_azkar),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(10.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        Text(
                            "هل تريد الخروج من التطبيق؟",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { getPlatform().exitApp() },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text("نعم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Button(
                                onClick = { showExitDialog = false },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text("لا", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Stars
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            "المرجو إضافة تقييمك للتطبيق",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        Button(
                            onClick = {
                                val platform = getPlatform()
                                if (platform.isAndroid) {
                                    uriHandler.openUri("https://play.google.com/store/apps/details?id=com.mahmoud.azkarapp")
                                } else {
                                    uriHandler.openUri("https://apps.apple.com/us/app/%D8%A3%D8%B0%D9%83%D8%A7%D8%B1-%D8%AD%D8%B5%D9%86-%D8%A7%D9%84%D9%85%D8%B3%D9%84%D9%85/id6822073243")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.height(50.dp).padding(horizontal = 16.dp)
                        ) {
                            Text("أضف تعليقك", color = TextBrown, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    }

                    // White Bottom Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🙌", fontSize = 32.sp)
                        Text(
                            "لا تنسونا من صالح دعائكم لي ولوالديَّ",
                            color = HeaderGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
    
    var isMuted by remember { mutableStateOf(false) }

    val headerBgColor = if (effectiveNightMode) Color(0xFF0F171A) else HeaderGreen

    AppBackground(isNightMode = effectiveNightMode) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (screen !is Screen.AzkarList) {
                Header(
                    title = when (screen) {
                        is Screen.Home -> "حصن المسلم"
                        is Screen.AzkarCategories -> "أذكار المسلم"
                        is Screen.DuaCategories -> "أدعية المسلم"
                        is Screen.Tasbih -> "المسبحة الإلكترونية"
                        is Screen.HadithList -> "الأحاديث النبوية"
                        else -> ""
                    },
                    onBack = {
                        onBack()
                    },
                    showBack = screen !is Screen.Home,
                    onExitClick = { showExitDialog = true },
                    backgroundColor = headerBgColor,
                    trailingIcon = if (screen is Screen.Tasbih || screen is Screen.HadithList) {
                        {
                            IconButton(onClick = { isMuted = !isMuted }) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    } else null
                )
            }

            when (screen) {
                is Screen.Home -> HomeScreen(onCategoryClick = { 
                    when (it) {
                        "المسبحة الإلكترونية" -> onNavigate(Screen.Tasbih)
                        "أدعية المسلم" -> onNavigate(Screen.DuaCategories)
                        "أذكار المسلم" -> onNavigate(Screen.AzkarCategories)
                        "الأحاديث النبوية" -> onNavigate(Screen.HadithList)
                        else -> onNavigate(Screen.AzkarList(it))
                    }
                })
                is Screen.AzkarCategories -> AzkarCategoriesScreen(onCategoryClick = {
                    onNavigate(Screen.AzkarList(it))
                })
                is Screen.DuaCategories -> DuaCategoriesScreen(onDuaClick = {
                    onNavigate(Screen.AzkarList(it, fromDua = true))
                })
                is Screen.AzkarList -> AzkarReaderScreen(
                    category = screen.category,
                    azkar = allAzkar.filter { 
                        it.category == screen.category || (screen.category == "أذكار الصباح" && (it.category == "أذكار الصباح" || it.category == "Morning")) 
                    }.sortedBy { it.id },
                    onBack = onBack,
                    headerBgColor = headerBgColor
                )
                is Screen.HadithList -> HadithReaderScreen(hadiths = allHadiths)
                is Screen.Tasbih -> TasbihScreen(
                    isMuted = isMuted,
                    isNightMode = isTasbihNightMode,
                    onNightModeChange = { isTasbihNightMode = !isTasbihNightMode }
                )
                else -> {}
            }
        }
    }
}

@Composable
fun Header(
    title: String, 
    onBack: () -> Unit, 
    showBack: Boolean,
    onExitClick: () -> Unit = {},
    backgroundColor: Color = HeaderGreen,
    trailingIcon: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp) // Increased size
                )
            }
        } else {
            // Exit button on Home screen
            IconButton(onClick = onExitClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit", 
                    tint = Color.White, // Changed to White
                    modifier = Modifier.size(36.dp) // Increased size
                )
            }
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )

        if (trailingIcon != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                trailingIcon()
            }
        }
    }
}

@Composable
fun HomeScreen(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        CategoryItem("أذكار المسلم", Res.drawable.img_azkar, "", Color(0xFFFFE082)),
        CategoryItem("أدعية المسلم", Res.drawable.img_dua, "", Color(0xFFFFF59D)),
        CategoryItem("الأحاديث النبوية", Res.drawable.hadith_icon, "", Color(0xFFB3E5FC)),
        CategoryItem("المسبحة الإلكترونية", Res.drawable.img_tasbih, "", Color(0xFFA5D6A7))
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp), // Increased for gestures bar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        lazyItems(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .clickable { onCategoryClick(category.title) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .fillMaxHeight()
                    ) {
                        Image(
                            painter = painterResource(category.icon),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(16.dp).fillMaxSize()
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 24.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGold,
                                fontSize = 30.sp
                            ),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AzkarCategoriesScreen(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        CategoryItem("أذكار الصباح", Res.drawable.img_morning, "", Color(0xFFFFE082)),
        CategoryItem("أذكار المساء", Res.drawable.img_evening, "", Color(0xFF9FA8DA)),
        CategoryItem("أذكار قبل النوم", Res.drawable.img_sleep, "", Color(0xFFB39DDB)),
        CategoryItem("أذكار الاستيقاظ", Res.drawable.img_wake_up, "", Color(0xFFFFCC80)),
        CategoryItem("أذكار سماع الأذان", Res.drawable.img_adhan, "", Color(0xFF81C784)),
        CategoryItem("أذكار بعد الوضوء", Res.drawable.img_wudu, "", Color(0xFF4FC3F7)),
        CategoryItem("أذكار بعد الصلاة", Res.drawable.img_after_prayer, "", Color(0xFFE57373))
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp), // Increased for gestures bar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        lazyItems(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .clickable { onCategoryClick(category.title) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .fillMaxHeight()
                    ) {
                        Image(
                            painter = painterResource(category.icon),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(16.dp).fillMaxSize()
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 24.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGold,
                                fontSize = 30.sp
                            ),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DuaCategoriesScreen(onDuaClick: (String) -> Unit) {
    val categories = listOf(
        CategoryItem("دعاء صلاة الاستخارة", Res.drawable.img_istikhara, "", Color(0xFFC5CAE9)),
        CategoryItem("دعاء قضاء الدين", Res.drawable.img_debt, "", Color(0xFFFFCDD2)),
        CategoryItem("دعاء من أصابه هم أو حزن", Res.drawable.img_sadness, "", Color(0xFFB2EBF2)),
        CategoryItem("دعاء السفر", Res.drawable.img_travel, "", Color(0xFFDCEDC8)),
        CategoryItem("دعاء الركوب", Res.drawable.img_riding, "", Color(0xFFF8BBD0)),
        CategoryItem("دعاء دخول المسجد", Res.drawable.img_mosque_enter, "", Color(0xFFE1BEE7)),
        CategoryItem("دعاء الخروج من المسجد", Res.drawable.img_mosque_exit, "", Color(0xFFFFE0B2)),
        CategoryItem("دعاء دخول المنزل", Res.drawable.img_home_enter, "", Color(0xFFD1C4E9)),
        CategoryItem("دعاء الخروج من المنزل", Res.drawable.img_home_exit, "", Color(0xFFCFD8DC)),
        CategoryItem("دعاء الدخول والخروج من الخلاء", Res.drawable.img_toilet, "", Color(0xFFF0F4C3)),
        CategoryItem("دعاء الذهاب إلى المسجد", Res.drawable.img_mosque, "", Color(0xFFFFE082)),
        CategoryItem("دعاء للميت في صلاة الجنازة", Res.drawable.img_funeral, "", Color(0xFFCFD8DC))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp), // Increased for gestures bar
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        lazyItems(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .clickable { onDuaClick(category.title) },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .fillMaxHeight()
                    ) {
                        Image(
                            painter = painterResource(category.icon),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(16.dp).fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 24.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGold,
                                fontSize = 30.sp
                            ),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@OptIn(kotlin.ExperimentalMultiplatform::class)
@Composable
fun AzkarReaderScreen(
    category: String,
    azkar: List<Zekr>,
    onBack: () -> Unit,
    headerBgColor: Color
) {
    if (azkar.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                title = category,
                onBack = onBack,
                showBack = true,
                backgroundColor = headerBgColor
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("لا توجد أذكار في هذا القسم", color = TextBrown)
            }
        }
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    val currentZekr = azkar[currentIndex]
    var remainingRepetitions by remember(currentIndex) { mutableStateOf(currentZekr.count) }

    val playerState = io.github.rufenkhokhar.rememberKMPPlayerState()
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            isSpeaking = false
            playerState.stop()
            playerState.destroy()
        }
    }

    LaunchedEffect(currentIndex, isSpeaking) {
        if (isSpeaking && currentZekr.audio.isNotBlank()) {
            // إعادة تعيين العداد للذكر الحالي
            remainingRepetitions = currentZekr.count
            
            while (remainingRepetitions > 0 && isSpeaking) {
                val audioUri = Res.getUri("files/audio/${currentZekr.audio.trim()}")
                playerState.stop()
                kotlinx.coroutines.delay(100)
                playerState.setFileUrl(audioUri)
                playerState.play()
                
                // انتظار اكتمال تشغيل المقطع الحالي حتى حدث Ended
                playerState.observePlayerEvent().firstOrNull { event ->
                    event is io.github.rufenkhokhar.KMPPlayerEvent.Ended || event is io.github.rufenkhokhar.KMPPlayerEvent.Error
                }
                
                // إنقاص العداد بعد انتهاء التكرار الفعلي
                remainingRepetitions--
                kotlinx.coroutines.delay(150)
            }
            
            // بعد انتهاء جميع التكرارات بنجاح:
            if (isSpeaking) {
                if (currentIndex < azkar.size - 1) {
                    currentIndex++
                } else {
                    isSpeaking = false
                    playerState.stop()
                }
            }
        } else {
            playerState.stop()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Header(
            title = category,
            onBack = onBack,
            showBack = true,
            backgroundColor = headerBgColor
        )

        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, HeaderGreen.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            val scrollState = rememberScrollState()
            
            // Format text with line breaks after : and . for better structure
            val formattedText = currentZekr.text
                .replace(": ", ":\n")
                .replace(". ", ".\n\n")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .clickable {
                        if (remainingRepetitions > 1) {
                            remainingRepetitions--
                        } else if (currentIndex < azkar.size - 1) {
                            currentIndex++
                        }
                    }
                    .padding(24.dp), // Adjust padding since audio button is moved
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        lineHeight = 38.sp,
                        fontSize = 24.sp,
                        color = TextBrown,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Info Bar
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(60.dp)
                .background(InfoBarColor, RoundedCornerShape(30.dp))
                .border(1.dp, InfoBarBorder, RoundedCornerShape(30.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("عدد التكرار", color = TextBrown, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(remainingRepetitions.toString(), color = TextBrown, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    
                    if (remainingRepetitions > 1) {
                        IconButton(
                            onClick = { remainingRepetitions = 1 },
                            modifier = Modifier.size(32.dp).padding(start = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "تجاوز التكرار",
                                tint = AccentGold.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(InfoBarBorder))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("عدد الأذكار", color = TextBrown, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("${currentIndex + 1} / ${azkar.size}", color = TextBrown, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Controls
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth()
                .height(64.dp)
                .background(ButtonGreen, RoundedCornerShape(32.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            IconButton(
                onClick = { 
                    if (currentIndex > 0) {
                        currentIndex--
                    }
                },
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous",
                    tint = if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play/Pause Button
            IconButton(
                onClick = { 
                    if (currentZekr.audio.isNotBlank()) {
                        if (isSpeaking) {
                            isSpeaking = false
                            playerState.stop()
                        } else {
                            isSpeaking = true
                            val audioFile = currentZekr.audio.trim()
                            playerState.setFileUrl(Res.getUri("files/audio/$audioFile"))
                            playerState.play()
                        }
                    }
                },
                enabled = currentZekr.audio.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isSpeaking) "إيقاف القراءة" else "استماع للذكر",
                    tint = if (currentZekr.audio.isNotBlank()) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next Button
            IconButton(
                onClick = { 
                    if (currentIndex < azkar.size - 1) {
                        currentIndex++
                    } else if (remainingRepetitions > 1) {
                        remainingRepetitions = 1
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun TasbihScreen(
    isMuted: Boolean,
    isNightMode: Boolean,
    onNightModeChange: () -> Unit
) {
    var count by remember { mutableStateOf(0) }
    var alertCount by remember { mutableStateOf(0) }
    var isAlertEnabled by remember { mutableStateOf(true) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val backgroundColor = if (isNightMode) Color(0xFF0F171A) else Color.White
    // Fixed: Transparent night color for the box, or pure white for light mode
    val containerColor = if (isNightMode) Color.White.copy(alpha = 0.05f) else Color.White
    
    val incrementCount = {
        count++
        if (!isMuted) {
            getPlatform().playClickSound()
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }

        if (isAlertEnabled && count > 0 && count % 100 == 0) {
            alertCount++
            getPlatform().vibrate(500)
            if (!isMuted) getPlatform().playAlertSound()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = incrementCount
            )
    ) {
        // Patterned background
        Icon(
            painter = painterResource(Res.drawable.ic_islamic_pattern),
            contentDescription = null,
            tint = (if (isNightMode) Color.White else HeaderGreen).copy(alpha = 0.05f),
            modifier = Modifier.fillMaxSize().scale(2.5f)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            
            // Top Control Panel - Completely Transparent/Subtle in Night Mode
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .height(75.dp)
                    .then(
                        if (isNightMode) {
                            Modifier.border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        } else {
                            Modifier.shadow(6.dp, RoundedCornerShape(20.dp))
                        }
                    )
                    .background(containerColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            count = 0 
                            alertCount = 0
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isNightMode) Color.White.copy(alpha = 0.1f) else Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = if (isNightMode) Color.White else Color(0xFF444444),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { 
                            onNightModeChange()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isNightMode) Color.White.copy(alpha = 0.1f) else Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                            contentDescription = "Mode",
                            tint = if (isNightMode) Color.White else Color(0xFF444444),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Alert Badge
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .width(160.dp)
                    .height(38.dp)
                    .background(if (isNightMode) Color.White.copy(alpha = 0.08f) else Color(0xFF333333), RoundedCornerShape(19.dp))
                    .then(if (isNightMode) Modifier.border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(19.dp)) else Modifier)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$alertCount × 100 تنبيه",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { isAlertEnabled = !isAlertEnabled },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = if (isAlertEnabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = "Toggle Alert",
                        tint = if (isAlertEnabled) Color(0xFF81C784) else Color(0xFFE57373),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // The Main Tasbih Device - Realistic Compose Implementation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f),
                contentAlignment = Alignment.Center
            ) {
                // Realistic Device Body
                Box(
                    modifier = Modifier
                        .size(320.dp, 420.dp)
                        .shadow(
                            elevation = if (isNightMode) 0.dp else 15.dp,
                            shape = RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp, bottomStart = 160.dp, bottomEnd = 160.dp)
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isNightMode) {
                                    listOf(Color(0xFF1B2B32).copy(alpha = 0.5f), Color(0xFF0F171A).copy(alpha = 0.8f))
                                } else {
                                    listOf(Color(0xFF333333), Color(0xFF111111))
                                }
                            ),
                            shape = RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp, bottomStart = 160.dp, bottomEnd = 160.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = if (isNightMode) {
                                    listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                                } else {
                                    listOf(Color.White.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.3f))
                                }
                            ),
                            shape = RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp, bottomStart = 160.dp, bottomEnd = 160.dp)
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 60.dp)
                    ) {
                        // Digital Display Screen
                        Surface(
                            modifier = Modifier
                                .width(160.dp)
                                .height(85.dp)
                                .border(3.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            color = if (isNightMode) Color(0xFF1B3D1B).copy(alpha = 0.4f) else Color(0xFFAAB8A0),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "8888",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 52.sp,
                                        color = Color.Black.copy(alpha = 0.05f),
                                        textAlign = TextAlign.Center,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = count.toString().padStart(4, '0'),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 52.sp,
                                        color = if (isNightMode) Color(0xFF00FF00) else Color(0xFF111111),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Large Main Click Button
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .shadow(8.dp, CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E)),
                                        center = androidx.compose.ui.geometry.Offset(100f, 100f)
                                    ),
                                    shape = CircleShape
                                )
                                .border(4.dp, Color(0xFF757575), CircleShape)
                                .clickable { incrementCount() },
                            contentAlignment = Alignment.Center
                        ) {
                            // Circular texture/shading on button
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.9f)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(30.dp))
                        
                        // Decorative elements (Beads simulation)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.DarkGray, CircleShape)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }
}

data class CategoryItem(val title: String, val icon: DrawableResource, val imageUrl: String, val bgColor: Color)
