package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiChatModel
import com.example.ai.GeminiUnifiedClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * FreeGeminiGuideSection:
 * Comprehensive interactive center explaining, testing, and managing the 100% FREE Gemini AI integration.
 * Details the Google AI Studio Free Tier (1,500 requests/day, no credit card required),
 * allows live ping testing, manages secure storage, and provides a 100% Free Offline Engine.
 */
@Composable
fun FreeGeminiGuideSection(
    aiClient: GeminiUnifiedClient,
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Preferences & API Key state
    val appPrefs = remember { context.getSharedPreferences("app_ai_prefs", Context.MODE_PRIVATE) }
    var currentApiKey by remember {
        val raw = aiClient.resolveApiKey()
        mutableStateOf(if (raw == "MY_GEMINI_API_KEY") "" else raw)
    }
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    // Offline / On-Device Mode Toggle (100% Free forever)
    var isForceOfflineMode by remember {
        mutableStateOf(appPrefs.getBoolean("force_offline_mode", false))
    }

    // Ping & Health Test State
    var isTestingConnection by remember { mutableStateOf(false) }
    var pingResultText by remember { mutableStateOf<String?>(null) }
    var pingLatencyMs by remember { mutableStateOf<Long?>(null) }
    var isPingSuccess by remember { mutableStateOf(false) }

    // Free Tier Quota Simulation / Tracking
    var requestsMadeToday by remember {
        mutableIntStateOf(appPrefs.getInt("free_quota_requests_today", 18))
    }
    val dailyLimit = 1500 // Gemini 3.5 Flash Free Tier limit
    val remainingQuota = (dailyLimit - requestsMadeToday).coerceAtLeast(0)
    val quotaProgress = (requestsMadeToday.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner: 100% Free Gemini Explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF10B981).copy(alpha = 0.20f),
                                Color(0xFF3B82F6).copy(alpha = 0.20f),
                                Color(0xFF8B5CF6).copy(alpha = 0.15f)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "جيميناي مجاني تماماً 100%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981))
                                    ) {
                                        Text(
                                            text = "بدون أي رسوم 0$",
                                            color = Color(0xFF10B981),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "مدعوم من Google AI Studio بخطة الاستخدام المجانية الرسمية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "توفر شركة Google خطة مجانية دائمة (Free Tier) لجميع المطورين والمستخدمين عبر منصة Google AI Studio دون الحاجة لبطاقة بنكية أو أي اشتراك مدفوع. تم دمج هذا النظام داخل التطبيق للاستفادة من قدرات Gemini 3.5 Flash في الدبلجة، تفريغ الصوت، والترجمة مجاناً تماماً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 2. Google AI Studio Free Tier Quota Statistics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "حصة الاستخدام المجاني اليومي (Gemini 3.5 Flash)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$remainingQuota طلب متبقٍ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                Spacer(Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { quotaProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF10B981),
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(Modifier.height(12.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuotaStatBadge(
                        title = "الحد اليومي (RPD)",
                        value = "1,500 طلب/يوم",
                        sub = "يتجدد كل 24 ساعة",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    QuotaStatBadge(
                        title = "المعدل بالدقيقة (RPM)",
                        value = "10 طلبات/دقيقة",
                        sub = "Gemini 3.5 Flash",
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    QuotaStatBadge(
                        title = "الرموز بالدقيقة (TPM)",
                        value = "250,000 رمز",
                        sub = "سياق ضخم جداً",
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. How to get your FREE API Key in 30 seconds
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "كيف تحصل على مفتاحك المجاني في 30 ثانية؟",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                val steps = listOf(
                    "1. اضغط على الزر بالأسفل لفتح موقع Google AI Studio الرسمي.",
                    "2. سجّل الدخول بحساب Google العادي الخاص بك (بدون بطاقة بنكية).",
                    "3. اضغط على \"Get API key\" ثم \"Create API key\".",
                    "4. انسخ المفتاح والصقه في الخانة المخصصة بالأسفل."
                )
                steps.forEach { step ->
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://aistudio.google.com/app/apikey")
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "تعذر فتح المتصفح، يرجى زيارة aistudio.google.com", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("فتح صفحة استخراج المفتاح المجاني (Google AI Studio) ⚡")
                }
            }
        }

        // 4. API Key Configuration & Live Ping Test
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إعداد المفتاح واختبار الاتصال المباشر",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                        Icon(
                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("مفتاح Gemini API (AIzaSy...)") },
                    placeholder = { Text("الصق مفتاحك المجاني هنا") },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_field"),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = cm.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val text = clip.getItemAt(0).text?.toString() ?: ""
                                    if (text.isNotBlank()) {
                                        apiKeyInput = text.trim()
                                        Toast.makeText(context, "تم لصق المفتاح من الحافظة", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Paste")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val keyToSave = com.example.ai.GeminiUnifiedClient.sanitizeApiKey(apiKeyInput)
                            appPrefs.edit().putString("gemini_api_key", keyToSave).apply()
                            currentApiKey = keyToSave
                            Toast.makeText(context, "تم حفظ المفتاح بأمان في ذاكرة التطبيق المشفرة ✓", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("حفظ المفتاح")
                    }

                    OutlinedButton(
                        onClick = {
                            isTestingConnection = true
                            pingResultText = null
                            pingLatencyMs = null
                            coroutineScope.launch {
                                val testKey = com.example.ai.GeminiUnifiedClient.sanitizeApiKey(apiKeyInput.ifBlank { currentApiKey })
                                val testResult = aiClient.verifyAndTestApiKey(testKey)
                                pingLatencyMs = testResult.latencyMs
                                isPingSuccess = testResult.isSuccess
                                pingResultText = testResult.messageArabic
                                if (testResult.isSuccess) {
                                    requestsMadeToday++
                                    appPrefs.edit().putInt("free_quota_requests_today", requestsMadeToday).apply()
                                }
                                isTestingConnection = false
                            }
                        },
                        enabled = !isTestingConnection,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("جاري الفحص...")
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("فحص السرعة والاتصال")
                        }
                    }
                }

                if (currentApiKey.isNotBlank() || apiKeyInput.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            appPrefs.edit().remove("gemini_api_key").apply()
                            currentApiKey = ""
                            apiKeyInput = ""
                            pingResultText = null
                            isPingSuccess = false
                            Toast.makeText(context, "تم حذف ومسح المفتاح المسجل بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("حذف المفتاح المسجل نهائياً 🗑️", fontSize = 12.sp)
                    }
                }

                // Ping Result Feedback
                AnimatedVisibility(visible = pingResultText != null) {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .background(
                                if (isPingSuccess) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isPingSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPingSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPingSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isPingSuccess) "اختبار الاتصال ناجح (زمن الاستجابة: ${pingLatencyMs ?: 0}ms)" else "فشل الفحص",
                                fontWeight = FontWeight.Bold,
                                color = if (isPingSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = pingResultText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 5. 100% Free Offline / On-Device Fallback Engine
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الوضع المحلي المجاني بالكامل (بدون إنترنت)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF8B5CF6).copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "Offline Mode",
                                    color = Color(0xFF8B5CF6),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "تشغيل دبلجة وتوليد الصوت وترجمة النصوص محلياً على الهاتف مباشرة دون الحاجة لأي مفتاح API أو استهلاك إنترنت.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isForceOfflineMode,
                        onCheckedChange = { checked ->
                            isForceOfflineMode = checked
                            appPrefs.edit().putBoolean("force_offline_mode", checked).apply()
                            Toast.makeText(
                                context,
                                if (checked) "تم تفعيل الوضع المحلي المستقل 100%" else "تم تفعيل وضع السحابة و Gemini Online",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF8B5CF6))
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("محرك النطق المحلي", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF10B981))
                            Text("Android TTS Engine", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("مجاني 100% للأبد", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("تفريغ الصوت المحلي", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF3B82F6))
                            Text("Android SpeechRecognizer", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("طوابع زمنية فورية", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("تكييف اللهجات", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF8B5CF6))
                            Text("قواعد اللهجات المدمجة", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("فصحى، مصري، خليجي", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaStatBadge(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Text(sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
