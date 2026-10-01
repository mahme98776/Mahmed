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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.GeminiChatModel
import com.example.ai.GeminiUnifiedClient
import kotlinx.coroutines.launch

/**
 * ExtractGeminiApiKeyDialog:
 * Allows user to delete previously registered API key, direct-open Google AI Studio to extract a new one,
 * paste from clipboard, live test, and save securely.
 */
@Composable
fun ExtractGeminiApiKeyDialog(
    currentSavedKey: String,
    onSaveKey: (String) -> Unit,
    onClearKey: () -> Unit,
    onDismiss: () -> Unit,
    aiClient: GeminiUnifiedClient? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var keyInput by remember(currentSavedKey) { mutableStateOf(if (currentSavedKey.startsWith("AQ.Ab8") || currentSavedKey == "MY_GEMINI_API_KEY") "" else currentSavedKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    // Ping / Test state
    var isTestingConnection by remember { mutableStateOf(false) }
    var pingResult by remember { mutableStateOf<String?>(null) }
    var isPingSuccess by remember { mutableStateOf<Boolean?>(null) }
    var pingLatencyMs by remember { mutableStateOf<Long?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("extract_gemini_api_key_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6750A4).copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = Color(0xFF6750A4),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "مفتاح Gemini API جديد 🔑",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "احذف المفتاح السابق واستخرج مفتاحك الجديد مجاناً",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_extract_key_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                // Alert Badge: Previous key status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "تمت إزالة المفتاح القديم. يمكنك الآن استخراج مفتاح API جديد خاص بك مجاناً 100% في أقل من دقيقة.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Step 1: Open Google AI Studio Button (Big, Prominent)
                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://aistudio.google.com/app/apikey")
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            Toast.makeText(
                                context,
                                "جارٍ فتح Google AI Studio... سجل الدخول واضغط Create API key",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "تعذر فتح المتصفح: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_open_google_ai_studio_extract"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A73E8)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "🌐 فتح Google AI Studio واستخراج المفتاح",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3 Simple Steps Guide
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📋 خطوات الاستخراج السريعة (مجاناً 100%):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        GuideStepItem(
                            stepNumber = "1",
                            title = "انقر على الزر الأزرق أعلاه لفتح منصة Google AI Studio."
                        )
                        GuideStepItem(
                            stepNumber = "2",
                            title = "سجل الدخول بحساب Google ثم اضغط زر «Create API key»."
                        )
                        GuideStepItem(
                            stepNumber = "3",
                            title = "انسخ المفتاح (يبدأ بـ AIzaSy...) ثم الصقه في المربع أدناه."
                        )
                        GuideStepItem(
                            stepNumber = "4",
                            title = "اضغط «حفظ وتفعيل المفتاح» للبدء بالدبلجة والذكاء الاصطناعي."
                        )
                    }
                }

                // Input Field for the new API Key
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "الصق مفتاحك الجديد هنا:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = {
                            keyInput = it
                            pingResult = null
                            isPingSuccess = null
                        },
                        label = { Text("مفتاح Gemini API (AIzaSy...)") },
                        placeholder = { Text("ألصق المفتاح الجديد هنا...") },
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("extract_api_key_input_field"),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "تبديل الرؤية"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = cm.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            val text = clip.getItemAt(0).text?.toString() ?: ""
                                            if (text.isNotBlank()) {
                                                keyInput = text.trim()
                                                pingResult = null
                                                isPingSuccess = null
                                                Toast.makeText(context, "تم لصق المفتاح من الحافظة ✓", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "لصق من الحافظة")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // Live Ping Result Banner if tested
                AnimatedVisibility(visible = pingResult != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPingSuccess == true) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                        border = BorderStroke(
                            1.dp,
                            if (isPingSuccess == true) Color(0xFF10B981) else Color(0xFFEF4444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isPingSuccess == true) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isPingSuccess == true) Color(0xFF059669) else Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = pingResult ?: "",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPingSuccess == true) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                                pingLatencyMs?.let {
                                    Text(
                                        text = "زمن استجابة الخادم: ${it}ms ⚡",
                                        fontSize = 10.5.sp,
                                        color = if (isPingSuccess == true) Color(0xFF047857) else Color(0xFFB91C1C)
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Save Button
                    Button(
                        onClick = {
                            val trimmed = keyInput.trim()
                            if (trimmed.isBlank()) {
                                Toast.makeText(context, "يرجى لصق أو إدخال مفتاح صالح أولاً", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (trimmed.startsWith("AQ.Ab8") || trimmed == "MY_GEMINI_API_KEY") {
                                Toast.makeText(context, "هذا المفتاح غير صالح. يرجى استخراج مفتاحك الخاص من الزر الأزرق أعلاه", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            onSaveKey(trimmed)
                            Toast.makeText(context, "تم حفظ وتفعيل مفتاح Gemini الجديد بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_extracted_key_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("حفظ وتفعيل المفتاح الجديد ✅", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Row: Test Button & Clear Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Live Ping Button
                        OutlinedButton(
                            onClick = {
                                val keyToTest = keyInput.trim()
                                if (keyToTest.isBlank()) {
                                    Toast.makeText(context, "يرجى إدخال مفتاح لاختباره", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                if (aiClient == null) {
                                    Toast.makeText(context, "المفتاح جاهز للحفظ", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                isTestingConnection = true
                                pingResult = null
                                coroutineScope.launch {
                                    val start = System.currentTimeMillis()
                                    val result = aiClient.sendChatMessage(
                                        messages = emptyList(),
                                        userPrompt = "رد بكلمة واحدة: جاهز",
                                        model = GeminiChatModel.FLASH_3_5,
                                        customApiKey = keyToTest
                                    )
                                    val latency = System.currentTimeMillis() - start
                                    pingLatencyMs = latency
                                    isTestingConnection = false
                                    if (result.isSuccess) {
                                        isPingSuccess = true
                                        pingResult = "المفتاح يعمل بنجاح! متصل بـ Gemini 3.5 Flash 🟢"
                                    } else {
                                        isPingSuccess = false
                                        val err = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                                        pingResult = "فشل التحقق من المفتاح: $err"
                                    }
                                }
                            },
                            enabled = !isTestingConnection && keyInput.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("test_extracted_key_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(6.dp))
                                Text("جاري الفحص...", fontSize = 11.5.sp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("اختبار الاتصال ⚡", fontSize = 11.5.sp)
                            }
                        }

                        // Delete / Clear Button
                        OutlinedButton(
                            onClick = {
                                keyInput = ""
                                pingResult = null
                                isPingSuccess = null
                                onClearKey()
                                Toast.makeText(context, "تم مسح وحذف المفتاح المسجل بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(44.dp)
                                .testTag("delete_saved_key_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("حذف المفتاح 🗑️", fontSize = 11.5.sp)
                        }
                    }
                }

                // Security & Privacy Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "المفتاح يُحفظ محلياً ومشفر تماماً على هاتفك فقط دون أي خوادم وسيطة 🔒",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideStepItem(
    stepNumber: String,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 15.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
