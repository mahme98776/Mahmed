package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.tts.CloudTtsConfig
import com.example.audio.tts.CloudTtsProvider
import com.example.audio.tts.CloudVoiceCatalog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudTtsConfigDialog(
    config: CloudTtsConfig,
    onSaveConfig: (CloudTtsConfig) -> Unit,
    onTestConnection: ((Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(config.provider) }
    var elevenLabsKey by remember { mutableStateOf(config.elevenLabsApiKey) }
    var elevenLabsVoiceId by remember { mutableStateOf(config.elevenLabsVoiceId) }
    var googleKey by remember { mutableStateOf(config.googleCloudApiKey) }
    var googleVoiceName by remember { mutableStateOf(config.googleCloudVoiceName) }
    var speakingRate by remember { mutableStateOf(config.speakingRate) }
    var pitch by remember { mutableStateOf(config.pitch) }
    var stability by remember { mutableStateOf(config.stability) }
    var similarity by remember { mutableStateOf(config.similarityBoost) }
    var isEnabled by remember { mutableStateOf(config.isEnabled) }

    var isKeyVisible by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultStatus by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cloud_tts_config_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF6366F1).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "محرك الصوت البشري (TTS)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ربط ElevenLabs أو Google Cloud TTS",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Enable / Disable Cloud TTS toggle
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تفعيل الصوت السحابي الفائق",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "استخدام أصوات بشرية طبيعية جداً بدلاً من الصوت الافتراضي",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { isEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF6366F1)
                                ),
                                modifier = Modifier.testTag("tts_enable_switch")
                            )
                        }
                    }
                }

                // 2. Provider Selection Tabs
                item {
                    Text(
                        text = "اختر مزود خدمة الصوت الذكي:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CloudTtsProvider.values().forEach { provider ->
                            val isSelected = provider == selectedProvider
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedProvider = provider
                                        testResultStatus = null
                                    }
                                    .testTag("provider_choice_${provider.name}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(provider.iconEmoji, fontSize = 22.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = provider.titleArabic,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = provider.descriptionArabic,
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF6366F1),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. API Key & Model Configuration
                if (selectedProvider == CloudTtsProvider.ELEVEN_LABS) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "مفتاح API الخاص بـ ElevenLabs:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = elevenLabsKey,
                                    onValueChange = {
                                        elevenLabsKey = it
                                        testResultStatus = null
                                    },
                                    placeholder = { Text("أدخل مفتاح xi-api-key هنا...", fontSize = 12.sp) },
                                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                            Icon(
                                                if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF6366F1))
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("elevenlabs_api_key_field")
                                )

                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "اختر الصوت البشري (Voice ID):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    items(CloudVoiceCatalog.elevenLabsVoices) { voice ->
                                        val isVoiceSelected = voice.id == elevenLabsVoiceId
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isVoiceSelected) Color(0xFF6366F1).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = if (isVoiceSelected) BorderStroke(1.dp, Color(0xFF6366F1)) else null,
                                            modifier = Modifier
                                                .clickable { elevenLabsVoiceId = voice.id }
                                                .testTag("eleven_voice_${voice.id}")
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                                Text(
                                                    text = voice.name,
                                                    fontWeight = if (isVoiceSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp,
                                                    color = if (isVoiceSelected) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (voice.gender == "FEMALE") "أنثى" else "ذكر",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "ثبات النبرة (Stability): ${(stability * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = stability,
                                    onValueChange = { stability = it },
                                    valueRange = 0.1f..1.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF6366F1), activeTrackColor = Color(0xFF6366F1))
                                )
                            }
                        }
                    }
                } else if (selectedProvider == CloudTtsProvider.GOOGLE_CLOUD_TTS) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "مفتاح Google Cloud API Key:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = googleKey,
                                    onValueChange = {
                                        googleKey = it
                                        testResultStatus = null
                                    },
                                    placeholder = { Text("أدخل مفتاح Google Cloud TTS هنا...", fontSize = 12.sp) },
                                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                            Icon(
                                                if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF3B82F6))
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("google_cloud_api_key_field")
                                )

                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "أصوات WaveNet الفصيحة للغة العربية:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    items(CloudVoiceCatalog.googleCloudVoices) { voice ->
                                        val isVoiceSelected = voice.id == googleVoiceName
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isVoiceSelected) Color(0xFF3B82F6).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = if (isVoiceSelected) BorderStroke(1.dp, Color(0xFF3B82F6)) else null,
                                            modifier = Modifier
                                                .clickable { googleVoiceName = voice.id }
                                                .testTag("google_voice_${voice.id}")
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                                Text(
                                                    text = voice.name,
                                                    fontWeight = if (isVoiceSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.sp,
                                                    color = if (isVoiceSelected) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = voice.descriptionArabic,
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Voice Dynamics (Speaking Rate & Pitch)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("سرعة الإلقاء (Speaking Rate)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${String.format(Locale.US, "%.2f", speakingRate)}x", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = speakingRate,
                                onValueChange = { speakingRate = it },
                                valueRange = 0.6f..1.5f,
                                modifier = Modifier.testTag("speaking_rate_slider")
                            )

                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("طبقة الصوت (Pitch)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${String.format(Locale.US, "%+.1f", pitch)} st", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = pitch,
                                onValueChange = { pitch = it },
                                valueRange = -6.0f..6.0f,
                                modifier = Modifier.testTag("pitch_slider")
                            )
                        }
                    }
                }

                // 5. Test Connection Section
                if (selectedProvider != CloudTtsProvider.DEVICE_TTS) {
                    item {
                        OutlinedButton(
                            onClick = {
                                isTestingConnection = true
                                testResultStatus = null
                                val currentDraftConfig = CloudTtsConfig(
                                    provider = selectedProvider,
                                    elevenLabsApiKey = elevenLabsKey,
                                    elevenLabsVoiceId = elevenLabsVoiceId,
                                    googleCloudApiKey = googleKey,
                                    googleCloudVoiceName = googleVoiceName,
                                    speakingRate = speakingRate,
                                    pitch = pitch,
                                    stability = stability,
                                    similarityBoost = similarity,
                                    isEnabled = isEnabled
                                )
                                onSaveConfig(currentDraftConfig)
                                onTestConnection { success, msg ->
                                    isTestingConnection = false
                                    testResultStatus = Pair(success, msg)
                                }
                            },
                            enabled = !isTestingConnection,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("test_tts_connection_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("جارٍ اختبار الاتصال وتوليد عينة صوتية...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("تجربة اتصال المحرك ونطق عينة فصيحة", fontSize = 12.sp)
                            }
                        }

                        testResultStatus?.let { (success, msg) ->
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (success) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (success) Color(0xFF10B981) else Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (success) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        fontSize = 11.sp,
                                        color = if (success) Color(0xFF065F46) else Color(0xFF991B1B)
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
                    val updatedConfig = CloudTtsConfig(
                        provider = selectedProvider,
                        elevenLabsApiKey = elevenLabsKey,
                        elevenLabsVoiceId = elevenLabsVoiceId,
                        googleCloudApiKey = googleKey,
                        googleCloudVoiceName = googleVoiceName,
                        speakingRate = speakingRate,
                        pitch = pitch,
                        stability = stability,
                        similarityBoost = similarity,
                        isEnabled = isEnabled
                    )
                    onSaveConfig(updatedConfig)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_cloud_tts_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("حفظ وتطبيق الإعدادات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("إلغاء")
            }
        }
    )
}
