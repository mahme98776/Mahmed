package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alad.AladFloatingWidgetService
import com.example.alad.AladLanguage
import com.example.alad.AladLanguageCatalog
import com.example.alad.AladLiveDubbingEngine
import com.example.ui.DubbingViewModel

/**
 * ALAD Mobile - دبلجة التطبيقات الحية بالذكاء الاصطناعي (AI Live Audio Dubbing)
 * مستوحى ومبني على معمارية ALAD Mobile مفتوحة المصدر لتقديم دبلجة صوتية حية
 * لأي تطبيق أندرويد (YouTube, Netflix, Spotify, TikTok) عبر Gemini 3.5 Live Translate.
 *
 * جميع حقوق النشر والملكية الفكرية والتصميم محفوظة للأستاذ محمد سليمة © 2026
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AladLiveDubbingScreen(
    viewModel: DubbingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aladEngine = remember { AladLiveDubbingEngine(context, viewModel.geminiUnifiedClient) }

    DisposableEffect(Unit) {
        onDispose {
            aladEngine.release()
        }
    }

    val isLiveDubbing by aladEngine.isLiveDubbing.collectAsStateWithLifecycle()
    val targetLanguage by aladEngine.targetLanguage.collectAsStateWithLifecycle()
    val selectedApp by aladEngine.selectedTargetApp.collectAsStateWithLifecycle()
    val isSpeakingDub by aladEngine.liveClient.isSpeakingDub.collectAsStateWithLifecycle()
    val liveOriginalText by aladEngine.liveClient.liveOriginalText.collectAsStateWithLifecycle()
    val liveDubbedText by aladEngine.liveClient.liveDubbedText.collectAsStateWithLifecycle()
    val liveLatencyMs by aladEngine.liveClient.liveLatencyMs.collectAsStateWithLifecycle()
    val audioLevelRms by aladEngine.captureEngine.audioLevelRms.collectAsStateWithLifecycle()
    val isDuckingActive by aladEngine.duckingManager.isDuckingActive.collectAsStateWithLifecycle()
    val duckingPercent by aladEngine.duckingManager.duckingPercentage.collectAsStateWithLifecycle()
    val isFloatingServiceRunning by AladFloatingWidgetService.isServiceRunning.collectAsStateWithLifecycle()
    val liveStatusMessage by aladEngine.liveStatusMessage.collectAsStateWithLifecycle()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var languageSearchQuery by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141218))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Hero Header & Copyright Badge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alad_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF211F26)
                ),
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFFD0BCFF), Color(0xFFE8DEF8), Color(0xFFFFB4AB))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF6750A4), Color(0xFF7D5260))),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RecordVoiceOver,
                                    contentDescription = "ALAD Live",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "ALAD Mobile",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFFE6E1E5)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFB3261E), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "LIVE 78L",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "دبلجة التطبيقات الحية بالذكاء الاصطناعي",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCAC4D0)
                                )
                            }
                        }

                        // Live status badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLiveDubbing) Color(0xFF601410) else Color(0xFF2B2930),
                            border = BorderStroke(
                                1.dp,
                                if (isLiveDubbing) Color(0xFFF2B8B5) else Color(0xFF49454F)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .scale(if (isLiveDubbing) pulseScale else 1f)
                                        .background(
                                            if (isLiveDubbing) Color(0xFFF2B8B5) else Color(0xFF938F99),
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = if (isLiveDubbing) "بث مباشر" else "جاهز للبدء",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLiveDubbing) Color(0xFFF2B8B5) else Color(0xFFCAC4D0)
                                )
                            }
                        }
                    }

                    Text(
                        text = "تقنية ALAD (AI Live Audio Dubbing) تتيح دبلجة فورية لأي تطبيق خارجي (مثل يوتيوب، نتفليكس، سبوتيفاي) بصوت طبيعي منخفض الكمون عبر Google Gemini 3.5 Live Translate عبر 78 لغة مع التهدئة التلقائية لصوت الخلفية.",
                        fontSize = 13.sp,
                        color = Color(0xFFE6E1E5),
                        lineHeight = 19.sp
                    )

                    // Copyright guarantee
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1D1B20),
                        border = BorderStroke(1.dp, Color(0xFF49454F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = "Copyright",
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "جميع حقوق النشر والابتكار محفوظة للمهندس والمبتكر محمد سليمة © 2026",
                                fontSize = 11.sp,
                                color = Color(0xFFD0BCFF),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Floating Control Widget (Overlay Window) Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Layers,
                                contentDescription = "Floating Widget",
                                tint = Color(0xFFD0BCFF)
                            )
                            Text(
                                text = "الزر العائم فوق التطبيقات (Floating Overlay)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFFE6E1E5)
                            )
                        }

                        // Floating toggle button
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    Toast.makeText(
                                        context,
                                        "يرجى منح إذن الظهور فوق التطبيقات لتفعيل الزر العائم 🪟",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } else {
                                    if (isFloatingServiceRunning) {
                                        AladFloatingWidgetService.stop(context)
                                        Toast.makeText(context, "تم إغلاق الزر العائم", Toast.LENGTH_SHORT).show()
                                    } else {
                                        AladFloatingWidgetService.start(context)
                                        Toast.makeText(context, "تم تشغيل الزر العائم فوق التطبيقات بنجاح 🔴", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFloatingServiceRunning) Color(0xFFB3261E) else Color(0xFF6750A4)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("alad_toggle_floating_widget_btn")
                        ) {
                            Text(
                                text = if (isFloatingServiceRunning) "إخفاء العائم" else "إظهار العائم",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "يسمح لك بالتحكم المباشر في بدء وإيقاف الدبلجة واختيار اللغة دون مغادرة يوتيوب أو نتفليكس أو سبوتيفاي!",
                        fontSize = 12.sp,
                        color = Color(0xFFCAC4D0)
                    )
                }
            }
        }

        // Target External Apps Quick Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "اختر التطبيق المراد دبلجته تلقائياً:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFE6E1E5)
                    )

                    val apps = listOf(
                        "YouTube" to "🔴",
                        "Netflix" to "🎬",
                        "Spotify" to "🎧",
                        "TikTok" to "📱",
                        "Twitch" to "🎮",
                        "Podcasts" to "🎙️",
                        "أي تطبيق" to "🌐"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        apps.forEach { (app, icon) ->
                            val isSelected = selectedApp.equals(app, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { aladEngine.setTargetApp(app) },
                                label = {
                                    Text(
                                        text = "$icon $app",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6750A4),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1D1B20),
                                    labelColor = Color(0xFFE6E1E5)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Target Language Selector Card (78 Languages)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "لغة الدوبلاج المستهدفة (78 لغة):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFE6E1E5)
                            )
                            Text(
                                text = "Gemini 3.5 Live Translate Engine",
                                fontSize = 11.sp,
                                color = Color(0xFFD0BCFF)
                            )
                        }

                        Button(
                            onClick = { showLanguageDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F378B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${targetLanguage.flagEmoji} ${targetLanguage.nameArabic}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Popular Language Quick Pills
                    val popularLangs = listOf(
                        AladLanguageCatalog.findByCode("ar-EG"),
                        AladLanguageCatalog.findByCode("ar"),
                        AladLanguageCatalog.findByCode("en"),
                        AladLanguageCatalog.findByCode("fr"),
                        AladLanguageCatalog.findByCode("es"),
                        AladLanguageCatalog.findByCode("de"),
                        AladLanguageCatalog.findByCode("tr"),
                        AladLanguageCatalog.findByCode("ja")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularLangs.forEach { lang ->
                            val isSelected = targetLanguage.code == lang.code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF6750A4) else Color(0xFF1D1B20),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFD0BCFF) else Color(0xFF49454F)
                                ),
                                modifier = Modifier.clickable { aladEngine.setTargetLanguage(lang) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = lang.flagEmoji, fontSize = 14.sp)
                                    Text(
                                        text = lang.nameArabic,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFFE6E1E5)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Intelligent Audio Ducking Slider Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isDuckingActive) Icons.Filled.VolumeDown else Icons.Filled.VolumeUp,
                                contentDescription = "Audio Ducking",
                                tint = if (isDuckingActive) Color(0xFFFFB4AB) else Color(0xFFD0BCFF)
                            )
                            Text(
                                text = "التهدئة التلقائية لصوت الخلفية (Audio Ducking):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFE6E1E5)
                            )
                        }
                        Text(
                            text = "$duckingPercent%",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF),
                            fontSize = 14.sp
                        )
                    }

                    Slider(
                        value = duckingPercent.toFloat(),
                        onValueChange = { aladEngine.duckingManager.setDuckingPercentage(it.toInt()) },
                        valueRange = 0f..100f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFD0BCFF),
                            activeTrackColor = Color(0xFF6750A4),
                            inactiveTrackColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.testTag("alad_ducking_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "صوت خفيف في الخلفية (25%)",
                            fontSize = 11.sp,
                            color = Color(0xFFCAC4D0)
                        )
                        Text(
                            text = if (isDuckingActive) "⚡ حالة التهدئة: نشطة الآن" else "حالة التهدئة: في وضع الاستعداد",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDuckingActive) Color(0xFFFFB4AB) else Color(0xFFCAC4D0)
                        )
                    }
                }
            }
        }

        // Live Action Controls (Start / Stop / Live Simulation)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF211F26)),
                border = BorderStroke(1.dp, Color(0xFF49454F))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "التحكم في جلسة الدوبلاج المباشر:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFE6E1E5)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isLiveDubbing) {
                                    aladEngine.stopLiveDubbing()
                                    Toast.makeText(context, "تم إيقاف الدبلجة الحية", Toast.LENGTH_SHORT).show()
                                } else {
                                    val success = aladEngine.startLiveDubbing()
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "بدأت الدبلجة الحية لتطبيق $selectedApp إلى ${targetLanguage.nameArabic} 🔴",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "تأكد من منح إذن الميكروفون للبدء",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLiveDubbing) Color(0xFFB3261E) else Color(0xFF6750A4)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("alad_start_stop_live_btn")
                        ) {
                            Icon(
                                imageVector = if (isLiveDubbing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLiveDubbing) "إيقاف الدبلجة الحية ⏹️" else "بدء الدبلجة الحية الحقيقية 🔴",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Audio Level Live Meter
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "مستوى حساسية التقاط الصوت:",
                                fontSize = 12.sp,
                                color = Color(0xFFCAC4D0)
                            )
                            Text(
                                text = "${(audioLevelRms * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (audioLevelRms > 0.05f) Color(0xFFD0BCFF) else Color(0xFF938F99)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { audioLevelRms },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFD0BCFF),
                            trackColor = Color(0xFF49454F)
                        )
                    }
                }
            }
        }

        // Realtime Translation & Dubbing Monitor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
                border = BorderStroke(1.dp, Color(0xFF49454F))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = "Monitor",
                                tint = Color(0xFFD0BCFF)
                            )
                            Text(
                                text = "شاشة المراقبة اللحظية للدبلجة (Live Monitor)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFE6E1E5)
                            )
                        }

                        // Latency badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF332D41)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = "Latency",
                                    tint = Color(0xFFD0BCFF),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${liveLatencyMs} ms",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF)
                                )
                            }
                        }
                    // Real-time Engine Status Message
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isLiveDubbing) Color(0xFF601410).copy(alpha = 0.5f) else Color(0xFF332D41),
                        border = BorderStroke(1.dp, if (isLiveDubbing) Color(0xFFB3261E) else Color(0xFF49454F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiveDubbing) Icons.Filled.RecordVoiceOver else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = if (isLiveDubbing) Color(0xFFF2B8B5) else Color(0xFFD0BCFF),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = liveStatusMessage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiveDubbing) Color(0xFFF2B8B5) else Color(0xFFE6E1E5)
                            )
                        }
                    }

                    // Original Speech Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2B2930),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🗣️ الصوت الأصلي الملتقط من $selectedApp:",
                                fontSize = 11.sp,
                                color = Color(0xFFCAC4D0),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (liveOriginalText.isNotBlank()) liveOriginalText else "في انتظار بدء التحدث أو تشغيل مقطع الفيديو...",
                                fontSize = 13.sp,
                                color = if (liveOriginalText.isNotBlank()) Color(0xFFE6E1E5) else Color(0xFF79747E),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Dubbed Speech Output Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF332D41),
                        border = BorderStroke(
                            1.dp,
                            if (isSpeakingDub) Color(0xFFD0BCFF) else Color(0xFF49454F)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎙️ الصوت المدبلج (${targetLanguage.flagEmoji} ${targetLanguage.nameArabic}):",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD0BCFF),
                                    fontWeight = FontWeight.Bold
                                )
                                if (isSpeakingDub) {
                                    Text(
                                        text = "⚡ جاري النطق بصوت طبيعي",
                                        fontSize = 10.sp,
                                        color = Color(0xFFCCC2DC),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (liveDubbedText.isNotBlank()) liveDubbedText else "جاهز لتوليد الدبلجة الفورية بصوت نقي ومطابق للأصل...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (liveDubbedText.isNotBlank()) Color.White else Color(0xFF79747E),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
    }

    // 78 Languages Search & Selection Dialog
    if (showLanguageDialog) {
        val filteredLanguages = remember(languageSearchQuery) {
            if (languageSearchQuery.isBlank()) {
                AladLanguageCatalog.supportedLanguages
            } else {
                AladLanguageCatalog.supportedLanguages.filter {
                    it.nameArabic.contains(languageSearchQuery, ignoreCase = true) ||
                            it.nameEnglish.contains(languageSearchQuery, ignoreCase = true) ||
                            it.code.contains(languageSearchQuery, ignoreCase = true)
                }
            }
        }

        AlertDialog(
            onDismissRequest = {
                showLanguageDialog = false
                languageSearchQuery = ""
            },
            title = {
                Text(
                    text = "اختر لغة الدبلجة الحية (78 لغة مدعومة)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = languageSearchQuery,
                        onValueChange = { languageSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ابحث عن لغة أو لهجة...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredLanguages) { lang ->
                            val isSelected = targetLanguage.code == lang.code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF4F378B) else Color(0xFF2B2930),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        aladEngine.setTargetLanguage(lang)
                                        showLanguageDialog = false
                                        languageSearchQuery = ""
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = lang.flagEmoji, fontSize = 20.sp)
                                        Column {
                                            Text(
                                                text = lang.nameArabic,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = lang.nameEnglish,
                                                fontSize = 11.sp,
                                                color = Color(0xFFCAC4D0)
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFFD0BCFF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLanguageDialog = false
                        languageSearchQuery = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("إغلاق")
                }
            }
        )
    }
}
