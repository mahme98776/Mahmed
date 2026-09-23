package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserSettings
import com.example.network.ServerConnectionStatus
import com.example.ui.DubbingViewModel

/**
 * Visual composable displaying the real-time connection status
 * (Connected, Disconnected, Connecting, Processing, or Error) using LaunchedEffect to track status.
 */
@Composable
fun ServerConnectionStatusBadge(
    connectionStatus: ServerConnectionStatus,
    onTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg, statusIcon, statusTitle, statusSub) = when (connectionStatus) {
        is ServerConnectionStatus.Connected -> {
            Tuple5(
                Color(0xFF10B981),
                Color(0xFF10B981).copy(alpha = 0.12f),
                Icons.Default.CheckCircle,
                "متصل بسيرفر الكمبيوتر (${connectionStatus.ip}:${connectionStatus.port})",
                "زمن الاستجابة: ${connectionStatus.pingMs}ms • ${connectionStatus.serverInfo ?: "كارت الشاشة جاهز للدبلجة"}"
            )
        }
        is ServerConnectionStatus.Connecting -> {
            Tuple5(
                Color(0xFFF59E0B),
                Color(0xFFF59E0B).copy(alpha = 0.12f),
                Icons.Default.Refresh,
                "جاري محاولة الاتصال بالكمبيوتر...",
                "يتم فحص استجابة الشبكة والمنفذ"
            )
        }
        is ServerConnectionStatus.Processing -> {
            Tuple5(
                Color(0xFF3B82F6),
                Color(0xFF3B82F6).copy(alpha = 0.12f),
                Icons.Default.Speed,
                "جاري المعالجة بواسطة سكريبت Lingo...",
                connectionStatus.stage
            )
        }
        is ServerConnectionStatus.Error -> {
            Tuple5(
                Color(0xFFEF4444),
                Color(0xFFEF4444).copy(alpha = 0.12f),
                Icons.Default.ErrorOutline,
                "غير متصل بسيرفر الكمبيوتر",
                connectionStatus.message
            )
        }
        is ServerConnectionStatus.Disconnected -> {
            Tuple5(
                Color(0xFF6B7280),
                Color(0xFF6B7280).copy(alpha = 0.12f),
                Icons.Default.WifiOff,
                "غير متصل",
                "اضغط لإجراء فحص الاتصال بالشبكة المحلية أو اليو إس بي"
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("server_connection_status_badge"),
        shape = RoundedCornerShape(14.dp),
        color = statusBg,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (connectionStatus is ServerConnectionStatus.Connecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = statusColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = statusTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = statusSub,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onTestClick,
                modifier = Modifier.size(36.dp).testTag("btn_refresh_server_ping")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "إعادة فحص الاتصال",
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

/**
 * Complete, production-grade Card component for Settings:
 * 1. IP / Port configuration inputs and persistence into DataStore (replacing hardcoded values).
 * 2. Real-time connection badge with LaunchedEffect monitoring.
 * 3. Error handling with troubleshooting dialog explaining Wi-Fi / IP checks.
 * 4. Audio file selection from device, MultipartBody conversion, and sending to Lingo Python server.
 */
@Composable
fun LingoServerSettingsCard(
    viewModel: DubbingViewModel,
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectionStatus by viewModel.lingoConnectionManager.connectionStatus.collectAsStateWithLifecycle()

    var inputIp by remember(userSettings.serverIp) { mutableStateOf(userSettings.serverIp) }
    var inputPort by remember(userSettings.serverPort) { mutableStateOf(userSettings.serverPort.toString()) }
    var showTroubleshootingDialog by remember { mutableStateOf(false) }
    var troubleshootingContent by remember { mutableStateOf("") }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAudioName by remember { mutableStateOf<String?>(null) }

    // Launcher for picking audio files from local storage
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAudioUri = uri
            selectedAudioName = "ملف صوتي تم تحديده (${uri.lastPathSegment ?: "audio"})"
            Toast.makeText(context, "تم اختيار الملف الصوتي بنجاح 🎵", Toast.LENGTH_SHORT).show()
        }
    }

    // Monitor reachability and launch periodic ping check
    LaunchedEffect(userSettings.serverIp, userSettings.serverPort) {
        viewModel.lingoConnectionManager.updateTarget(userSettings.serverIp, userSettings.serverPort)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_lingo_server_settings"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "خادم دبلجة الكمبيوتر المحلي ومكتبة Lingo 🐍",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ربط التطبيق بسيرفر البايثون عبر الشبكة المحلية (LAN) أو كابل الـ USB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Real-Time Connection Status Indicator Badge
            ServerConnectionStatusBadge(
                connectionStatus = connectionStatus,
                onTestClick = { viewModel.testLingoServerConnection() }
            )

            // Active processing progress indicator if Lingo is executing
            if (connectionStatus is ServerConnectionStatus.Processing) {
                val proc = connectionStatus as ServerConnectionStatus.Processing
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = proc.progressFraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(proc.progressFraction * 100).toInt()}% • ${proc.stage}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Error notice banner with troubleshooting action
            if (connectionStatus is ServerConnectionStatus.Error) {
                val err = connectionStatus as ServerConnectionStatus.Error
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "فشل الاتصال بـ ${userSettings.serverIp}:${userSettings.serverPort}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(
                            onClick = {
                                troubleshootingContent = err.detailedTroubleshooting
                                showTroubleshootingDialog = true
                            }
                        ) {
                            Text("كيفية الحل؟", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Configuration Input Fields (Editable IP & Port persisted in DataStore)
            Text(
                text = "إعدادات عنوان السيرفر والمنفذ (حفظ محلي في DataStore):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputIp,
                    onValueChange = { inputIp = it },
                    label = { Text("عنوان الـ IP أو الرابط") },
                    placeholder = { Text("192.168.1.3") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (inputIp.contains("192.") || inputIp.contains("10.")) Icons.Default.Wifi else Icons.Default.Cable,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .weight(1.8f)
                        .testTag("input_server_ip"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = inputPort,
                    onValueChange = { inputPort = it.filter { ch -> ch.isDigit() } },
                    label = { Text("المنفذ") },
                    placeholder = { Text("8000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_server_port"),
                    singleLine = true
                )
            }

            // Save & Test Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val portInt = inputPort.toIntOrNull() ?: 8000
                        viewModel.updateServerConfiguration(inputIp, portInt)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_server_config")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("حفظ الإعدادات في DataStore", fontSize = 12.5.sp)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.testLingoServerConnection()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_test_server_connection")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("اختبار الاتصال", fontSize = 12.5.sp)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Audio Selection & Multipart Transmission Section
            Text(
                text = "إرسال ملف صوتي محلي إلى محرك Lingo للدبلجة بالكمبيوتر:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_pick_audio_for_lingo")
                ) {
                    Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (selectedAudioName != null) "تغيير الملف الصوتي" else "اختيار ملف صوتي من الهاتف",
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        if (selectedAudioUri == null) {
                            Toast.makeText(context, "يرجى اختيار ملف صوتي أولاً", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.sendAudioToLingoServer(
                            audioUri = selectedAudioUri!!,
                            targetLanguage = "Arabic",
                            dialect = "Modern Standard Arabic",
                            speed = userSettings.speechRate,
                            onSuccess = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            },
                            onError = { errTitle, errDetails ->
                                troubleshootingContent = "$errTitle\n\n$errDetails"
                                showTroubleshootingDialog = true
                            }
                        )
                    },
                    enabled = selectedAudioUri != null && connectionStatus !is ServerConnectionStatus.Processing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_send_audio_to_lingo")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("إرسال للدبلجة 🚀", fontSize = 12.sp)
                }
            }

            selectedAudioName?.let { name ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = name,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Comprehensive Error Handling & Troubleshooting Dialog
    if (showTroubleshootingDialog) {
        AlertDialog(
            onDismissRequest = { showTroubleshootingDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "دليل حل مشكلات الاتصال بالسيرفر 🛠️",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = troubleshootingContent,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showTroubleshootingDialog = false
                    viewModel.testLingoServerConnection()
                }) {
                    Text("إعادة المحاولة الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTroubleshootingDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
