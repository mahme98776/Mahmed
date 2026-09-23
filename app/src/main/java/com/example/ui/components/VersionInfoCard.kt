package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.DubbingViewModel

/**
 * Version Info Component
 * Reads versionName and versionCode directly from BuildConfig and displays them in App Settings.
 * Allows users to see their current release details and check against latest Firebase/GitHub releases.
 */
@Composable
fun VersionInfoCard(
    viewModel: DubbingViewModel,
    onNavigateToUpdateCenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val updateManager = viewModel.updateManager
    val latestRelease by updateManager.latestRelease.collectAsState()
    val isChecking by updateManager.isCheckingUpdates.collectAsState()
    val updateCheckResult by updateManager.updateCheckResult.collectAsState()

    val currentVersionName = BuildConfig.VERSION_NAME
    val currentVersionCode = BuildConfig.VERSION_CODE

    val isUpdateAvailable = updateCheckResult?.isUpdateAvailable == true || (
        latestRelease != null && (
            latestRelease!!.versionCode > currentVersionCode ||
            latestRelease!!.versionName != currentVersionName
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("version_info_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            if (isUpdateAvailable) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "معلومات الإصدار",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "معلومات الإصدار والنظام 📦",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "بيانات البناء الرسمية من BuildConfig",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isUpdateAvailable) Color(0xFF7C4DFF).copy(alpha = 0.2f) else Color(0xFF4CAF50).copy(alpha = 0.2f),
                    border = BorderStroke(
                        1.dp,
                        if (isUpdateAvailable) Color(0xFF7C4DFF) else Color(0xFF4CAF50)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isUpdateAvailable) Color(0xFFD0BCFF) else Color(0xFF81C784),
                            modifier = Modifier.size(7.dp)
                        ) {}
                        Text(
                            text = if (isUpdateAvailable) "تحديث جديد متوفر 🚀" else "أحدث إصدار ✅",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUpdateAvailable) Color(0xFFD0BCFF) else Color(0xFF81C784)
                        )
                    }
                }
            }

            // Version Details Grid
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("رقم الإصدار (versionName)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "v$currentVersionName",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("app_version_name_text")
                        )
                    }

                    Column {
                        Text("كود البناء (versionCode)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "#$currentVersionCode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("app_version_code_text")
                        )
                    }

                    Column {
                        Text("حالة البناء", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (BuildConfig.DEBUG) "Debug Build 🛠️" else "Release Build 🔒",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Available update preview if any
            if (isUpdateAvailable && latestRelease != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF7C4DFF).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(18.dp))
                        Text(
                            text = "الإصدار الجديد v${latestRelease!!.versionName}: ${latestRelease!!.releaseTitle}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToUpdateCenter,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                        .testTag("version_info_open_update_center_btn")
                ) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("مركز التحديثات والويب 🌐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.checkForAppUpdates() },
                    enabled = !isChecking,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(42.dp)
                        .testTag("version_info_refresh_btn")
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("فحص الآن 🔄", fontSize = 11.5.sp)
                }
            }
        }
    }
}
