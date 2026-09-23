package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class OnboardingHelpStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val details: List<String>,
    val proTip: String
)

/**
 * Interactive Onboarding & Help Walkthrough Dialog.
 * Explains how to use the AI Dubbing platform step-by-step for new users.
 */
@Composable
fun OnboardingHelpDialog(
    onDismiss: () -> Unit,
    onComplete: (dontShowAgain: Boolean) -> Unit,
    initialStep: Int = 0
) {
    var currentStepIndex by remember { mutableIntStateOf(initialStep) }
    var dontShowAgain by remember { mutableStateOf(false) }

    val steps = remember {
        listOf(
            OnboardingHelpStep(
                stepNumber = 1,
                title = "استيراد الفيديو أو رابط يوتيوب 🎬",
                subtitle = "البداية السريعة والذكية لمشروعك",
                icon = Icons.Default.Movie,
                accentColor = Color(0xFF2196F3),
                details = listOf(
                    "اختر ملف فيديو محلياً من هاتفك (MP4, MKV, WebM) بجودة عالية.",
                    "أو الصق رابط فيديو يوتيوب لتوليد دبلجة آلية فورية وسحب الترجمات تلقائياً.",
                    "يدعم التطبيق الفيديوهات الطويلة ومقاطع ريلز وشورتس."
                ),
                proTip = "المعالجة تتم محلياً وتراعي خصوصية وأمان مقاطعك بالكامل."
            ),
            OnboardingHelpStep(
                stepNumber = 2,
                title = "تحليل الذكاء الاصطناعي والترجمة 🤖",
                subtitle = "التعرف الصوتي الدقيق والمزامنة الزمنية",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFF9C27B0),
                details = listOf(
                    "محرك الذكاء الاصطناعي يستخرج الكلام الصوتي بدقة بالغة بالمللي ثانية.",
                    "ترجمة الحوار آلياً مع مراعاة المعنى السياقي وتوافق حركة الشفاه (Lip-Sync).",
                    "إمكانية تعديل نصوص الترجمة والتوقيتات يدوياً في أي لحظة."
                ),
                proTip = "يمكنك الدبلجة من وإلى أي لغة عالمية مع دعم كامل للعربية الفصحى."
            ),
            OnboardingHelpStep(
                stepNumber = 3,
                title = "هندسة الأصوات البشرية وتوليد الدبلجة 🎙️",
                subtitle = "مكتبة أصوات واقعية متعددة الشخصيات",
                icon = Icons.Default.RecordVoiceOver,
                accentColor = Color(0xFF00B0FF),
                details = listOf(
                    "اختر من بين تشكيلة أصوات وثائقية، درامية، إخبارية، وحماسية.",
                    "تحكم كامل في نبرة الصوت (Pitch) وسرعة الإلقاء (Speech Rate).",
                    "عرض الترددات الصوتية (Waveform & Spectrum) بهندسة صوتية متطورة."
                ),
                proTip = "يتم حفظ تفضيلات سرعة ونبرة الصوت في DataStore لاسترجاعها دائماً."
            ),
            OnboardingHelpStep(
                stepNumber = 4,
                title = "المزامنة الذكية وخفض صوت الخلفية 🎚️",
                subtitle = "Smart Audio Ducking & Mixing",
                icon = Icons.Default.GraphicEq,
                accentColor = Color(0xFFFF9800),
                details = listOf(
                    "تخفيض صوت خلفية الفيديو الأصلي تلقائياً عند حديث المعلق لصفاء تام.",
                    "موازنة صوتية سينمائية بين الموسيقى الخلفية وصوت الدبلجة الجديد.",
                    "مؤقت ومعاينة لحظية بالصوت والصورة قبل الاعتماد النهائي."
                ),
                proTip = "يمكنك ضبط نسبة خفض الخلفية بحرية من شاشة الإعدادات أو الاستوديو."
            ),
            OnboardingHelpStep(
                stepNumber = 5,
                title = "تصدير عالي الدقة وحفظ المشاريع 🚀",
                subtitle = "إخراج الفيديو أو الملفات الصوتية ومشاركتها",
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF00E676),
                details = listOf(
                    "تصدير الفيديو بدقة تصل إلى 1080p و 4K بترميز سينمائي نقي.",
                    "إمكانية تصدير المسار الصوتي المستقل (WAV / MP3) أو ملف الترجمة (SRT).",
                    "حفظ مشاريعك في قاعدة البيانات المحلية المشفرة للمتابعة لاحقاً."
                ),
                proTip = "حسابك محمي بدرع أمني مشفر لحماية الملكية والبيانات."
            )
        )
    }

    val currentStep = steps[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("onboarding_help_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, currentStep.accentColor.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Step indicator and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = currentStep.accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = currentStep.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "دليل الاستخدام والترحيب 💡",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "خطوة ${currentStepIndex + 1} من ${steps.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = currentStep.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Progress Step Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, step ->
                        val isCurrent = index == currentStepIndex
                        val isPassed = index < currentStepIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isCurrent) 28.dp else 10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isCurrent -> currentStep.accentColor
                                        isPassed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    }
                                )
                                .clickable { currentStepIndex = index }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Step Content Box with Animated Transition
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "step_content"
                ) { step ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Hero Step Banner
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = step.accentColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, step.accentColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = step.accentColor,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = step.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = step.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = step.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Details Checklist
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            step.details.forEach { detail ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = step.accentColor,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = detail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Pro Tip Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "💡 نصيحة أمان وخبرة: ${step.proTip}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // "Don't show again" Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dontShowAgain = !dontShowAgain },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = currentStep.accentColor
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "عدم إظهار هذه اللوحة الترحيبية تلقائياً عند فتح التطبيق",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Navigation Buttons (Next / Prev / Start)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("السابق", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onComplete(dontShowAgain)
                            }
                        },
                        modifier = Modifier.weight(if (currentStepIndex > 0) 1.5f else 1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentStep.accentColor
                        )
                    ) {
                        Text(
                            text = if (currentStepIndex < steps.size - 1) "التالي ➡️" else "ابدأ الآن 🚀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        if (currentStepIndex < steps.size - 1) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
