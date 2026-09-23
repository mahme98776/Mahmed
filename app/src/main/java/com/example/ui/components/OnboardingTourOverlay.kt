package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class representing a single step in the interactive onboarding tour.
 */
data class OnboardingStep(
    val id: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val proTipArabic: String,
    val icon: ImageVector,
    val iconTint: Color,
    val highlightTargetTag: String,
    val targetAreaDescription: String,
    val positionAlignment: Alignment = Alignment.BottomCenter
)

object OnboardingTourManager {
    private const val PREFS_NAME = "dubbing_app_onboarding_prefs"
    private const val KEY_TOUR_COMPLETED = "has_completed_onboarding_tour_v1"

    fun isTourCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_TOUR_COMPLETED, false)
    }

    fun setTourCompleted(context: Context, completed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_TOUR_COMPLETED, completed).apply()
    }

    val defaultSteps = listOf(
        OnboardingStep(
            id = "play_pause_scene",
            titleArabic = "▶️ زر تشغيل المشهد ومزامنة الدبلجة",
            descriptionArabic = "اضغط على هذا الزر لتشغيل المشهد وتجربة مزامنة الصوت المدبلج بدقة واحترافية متناهية!",
            proTipArabic = "نصيحة: يمكنك استيراد صوت جاهز أو توليد صوت جديد بالذكاء الاصطناعي بنقرة واحدة.",
            icon = Icons.Default.PlayArrow,
            iconTint = Color(0xFFD0BCFF),
            highlightTargetTag = "play_pause_dub_button",
            targetAreaDescription = "الزر الدائري الرئيسي في منتصف لوحة التحكم السفلية",
            positionAlignment = Alignment.TopCenter
        ),
        OnboardingStep(
            id = "trim_audio",
            titleArabic = "✂️ أداة قص وضبط مسار الصوت",
            descriptionArabic = "يمكنك من خلال هذه الأداة قص البدايات الصامتة، حذف الأجزاء غير المرغوبة، وضبط توقيت الدبلجة بدقة الملي ثانية!",
            proTipArabic = "نصيحة: ميزة 'الحذف الذكي للصمت' تقوم بتنقية الصوت آلياً بضغطة زر واحدة.",
            icon = Icons.Default.ContentCut,
            iconTint = Color(0xFF90CAF9),
            highlightTargetTag = "quick_trim_audio_btn",
            targetAreaDescription = "زر القص أسفل موجات الصوت",
            positionAlignment = Alignment.TopCenter
        ),
        OnboardingStep(
            id = "voice_effects",
            titleArabic = "🎛️ مغير الأصوات والمؤثرات الصوتية",
            descriptionArabic = "اختر من بين أكثر من 16 نمطاً صوتياً ومؤثراً احترافياً (أبطال أنمي، روبوت، وثائقي، صدى هائل، إذاعة قديمة والمزيد).",
            proTipArabic = "نصيحة: يمكنك تجربة كل نبرة صوت بسماع عينة صوتية حية قبل تطبيقها على مشروعك.",
            icon = Icons.Default.GraphicEq,
            iconTint = Color(0xFFFFB74D),
            highlightTargetTag = "quick_effect_chip_btn",
            targetAreaDescription = "شريحة المؤثر الصوتي بجانب لوحة التحكم",
            positionAlignment = Alignment.TopCenter
        ),
        OnboardingStep(
            id = "teleprompter",
            titleArabic = "📜 شاشة التلقين النصي الذكية",
            descriptionArabic = "اقرأ حوار الشخصيات متزامناً مع المشهد كلمة بكلمة. يتم تلوين السطر النشط تلقائياً مع تشكيل الحركات الإعرابية الصحيحة.",
            proTipArabic = "نصيحة: يمكنك إضافة وتعديل أسطر النص أو الضغط على السطر للاستماع إلى نطقه الفصيح.",
            icon = Icons.Default.Subtitles,
            iconTint = Color(0xFF81C784),
            highlightTargetTag = "teleprompter_panel",
            targetAreaDescription = "بطاقة الحوار النصي أسفل مشغل الفيديو",
            positionAlignment = Alignment.BottomCenter
        ),
        OnboardingStep(
            id = "processing_hub",
            titleArabic = "🎬 مركز المعالجة والمعاينة المزدوجة",
            descriptionArabic = "قبل التصدير، استخدم شاشة المعالجة للمقارنة بين الفيديو الأصلي والمدبلج جنباً إلى جنب (Side-by-Side) أو عبر ممسحة المقارنة.",
            proTipArabic = "نصيحة: يوفر المركز مدققاً نحوياً لمخارج الحروف وخافضاً ذكياً للموسيقى (Smart Ducking).",
            icon = Icons.Default.Movie,
            iconTint = Color(0xFFC084FC),
            highlightTargetTag = "open_processing_preview_hub_button",
            targetAreaDescription = "أيقونة الفيلم البنفسجية في الشريط العلوي للاستوديو",
            positionAlignment = Alignment.BottomCenter
        ),
        OnboardingStep(
            id = "guide_and_pdf",
            titleArabic = "📖 دليل الاستخدام وكتيب الـ PDF",
            descriptionArabic = "إذا احتجت أي مساعدة أو أردت تحميل كتيب الاستخدام الرسمي بصيغة PDF ومشاركته، اضغط على زر الكتاب في أي وقت!",
            proTipArabic = "نصيحة: يمكنك إعادة هذه الجولة الإرشادية متى شئت من داخل شاشة الدليل.",
            icon = Icons.Default.AutoAwesome,
            iconTint = Color(0xFF34D399),
            highlightTargetTag = "open_guide_and_pdf_button",
            targetAreaDescription = "أيقونة الكتاب الخضراء في الشريط العلوي للاستوديو",
            positionAlignment = Alignment.BottomCenter
        )
    )
}

/**
 * Fullscreen Interactive Onboarding Spotlight & Tooltip Overlay
 */
@Composable
fun OnboardingTourOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onFinishTour: () -> Unit = onDismiss,
    steps: List<OnboardingStep> = OnboardingTourManager.defaultSteps,
    modifier: Modifier = Modifier
) {
    if (!isVisible || steps.isEmpty()) return

    val context = LocalContext.current
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps.getOrNull(currentStepIndex) ?: steps.first()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* block background clicks */ }
            .testTag("onboarding_tour_overlay")
    ) {
        // Spotlight glow header indicator
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Step count badge and Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tour Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2B2930),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "جولة الاستخدام السريعة (${currentStepIndex + 1}/${steps.size})",
                            color = Color(0xFFEADDFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Skip / Close Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF381E72).copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        OnboardingTourManager.setTourCompleted(context, true)
                        onDismiss()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تخطي الجولة",
                            color = Color(0xFFCAC4D0),
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق الجولة",
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Step Progress Bar
            LinearProgressIndicator(
                progress = { (currentStepIndex + 1).toFloat() / steps.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFD0BCFF),
                trackColor = Color(0xFF49454F)
            )
        }

        // Animated Central Target Spotlight Anchor Ring
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing Outer Halo
            Surface(
                shape = CircleShape,
                color = step.iconTint.copy(alpha = haloAlpha * 0.25f),
                border = BorderStroke(2.dp, step.iconTint.copy(alpha = haloAlpha)),
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
            ) {}

            // Inner Highlight Circle
            Surface(
                shape = CircleShape,
                color = Color(0xFF211F26),
                border = BorderStroke(2.dp, step.iconTint),
                modifier = Modifier
                    .size(80.dp)
                    .shadow(16.dp, CircleShape, spotColor = step.iconTint)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = step.iconTint,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Main Animated Tooltip Floating Card (Anchored bottom)
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 60 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 60 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_tooltip_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(step.iconTint, Color(0xFF4A4458))))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with target location hint
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = step.iconTint.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, step.iconTint.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = step.iconTint,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "موقع الميزة: ${step.targetAreaDescription}",
                                color = step.iconTint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Title
                    Text(
                        text = step.titleArabic,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    // Description
                    Text(
                        text = step.descriptionArabic,
                        color = Color(0xFFE6E1E5),
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    // Pro-Tip Box
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2B2930),
                        border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = step.proTipArabic,
                                color = Color(0xFFFFE082),
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Action Controls: Back, Step Dots, Next / Finish
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button
                        if (currentStepIndex > 0) {
                            OutlinedButton(
                                onClick = { currentStepIndex-- },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF49454F)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCAC4D0)),
                                modifier = Modifier.testTag("onboarding_prev_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "السابق",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("السابق", fontSize = 12.sp)
                            }
                        } else {
                            Spacer(Modifier.width(80.dp))
                        }

                        // Step Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.indices.forEach { index ->
                                val isSelected = index == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) step.iconTint else Color(0xFF49454F)
                                        )
                                )
                            }
                        }

                        // Next or Finish Button
                        val isLastStep = currentStepIndex == steps.size - 1
                        Button(
                            onClick = {
                                if (isLastStep) {
                                    OnboardingTourManager.setTourCompleted(context, true)
                                    onFinishTour()
                                } else {
                                    currentStepIndex++
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLastStep) Color(0xFF34D399) else Color(0xFF6750A4),
                                contentColor = if (isLastStep) Color(0xFF003314) else Color.White
                            ),
                            modifier = Modifier.testTag(if (isLastStep) "onboarding_finish_btn" else "onboarding_next_btn")
                        ) {
                            Text(
                                text = if (isLastStep) "تم، لنبدأ! 🎉" else "التالي",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isLastStep) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
