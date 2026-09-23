package com.example.ui.screens

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppTab
import com.example.export.PdfManualGenerator
import com.example.ui.components.OnboardingTourManager
import com.example.ui.components.OnboardingTourOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Data Model for App Guide Section & Tool Explanation
 */
data class ToolGuideItem(
    val id: String,
    val titleArabic: String,
    val category: String,
    val targetTab: AppTab?,
    val locationDescription: String,
    val icon: ImageVector,
    val iconColor: Color,
    val whatItDoes: String,
    val howToUse: String,
    val proTip: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppGuideAndPdfScreen(
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfStatusMessage by remember { mutableStateOf<String?>(null) }
    var showTourOverlay by remember { mutableStateOf(false) }

    // Categories
    val categories = listOf(
        "الكل",
        "🎙️ الاستوديو",
        "📹 دبلجة فيديو",
        "🎬 معاينة ومعالجة",
        "⚡ دبلجة فورية",
        "🗣️ نص لصوت",
        "🎞️ المشاهد",
        "📁 مشاريعي",
        "🔊 المؤثرات",
        "📤 التصدير"
    )

    // Complete Database of Tools and Screens
    val allTools = remember {
        listOf(
            // --- Studio Tools ---
            ToolGuideItem(
                id = "audio_dubbing_studio",
                titleArabic = "استوديو دبلجة الصوت بالذكاء الاصطناعي (AI Audio Dubber)",
                category = "🎙️ الاستوديو",
                targetTab = AppTab.AUDIO_DUB,
                locationDescription = "قائمة الأدوات السريعة ⚡ أو تبويب [دبلجة الصوت 🎙️]",
                icon = Icons.Default.GraphicEq,
                iconColor = Color(0xFF818CF8),
                whatItDoes = "دبلجة وتحويل ملفات الصوت والتسجيلات المباشرة إلى كافة اللهجات العربية والأصوات السينمائية مع مكس A/B متزامن.",
                howToUse = "استورد ملف MP3/WAV أو سجل صوتاً، اختر اللهجة المستهدفة والشخصية الصوتية، ثم اضغط بدء الدبلجة بالذكاء الاصطناعي.",
                proTip = "استخدم زر التبديل A/B لمقارنة نقاء وإحساس الصوت الأصلي مع الصوت المدبلج الجديد."
            ),
            ToolGuideItem(
                id = "studio_main",
                titleArabic = "شاشة الاستوديو ومحرر الدبلجة (Dubbing Studio & Video Editor)",
                category = "🎬 الاستوديو",
                targetTab = AppTab.STUDIO,
                locationDescription = "التبويب الأول في الشريط السفلي [الاستوديو 🎬]",
                icon = Icons.Default.Movie,
                iconColor = Color(0xFFC084FC),
                whatItDoes = "مساحة العمل المركزية لدبلجة المشاهد الكرتونية والفيديوهات، مزامنة مسارات الصوت، وتطبيق التأثيرات الذكية.",
                howToUse = "اختر مشهداً من المكتبة، استمع للحوار، واستورد أو ولد صوتاً بالذكاء الاصطناعي مع مزامنة كاملة للسكربت.",
                proTip = "يمكنك موازنة مستويات الصوت وقص المسار بدقة الملي ثانية للحصول على دبلجة احترافية متقنة."
            ),
            ToolGuideItem(
                id = "studio_teleprompter",
                titleArabic = "الملقن التلقائي الذكي (Smart Teleprompter)",
                category = "🎙️ الاستوديو",
                targetTab = AppTab.STUDIO,
                locationDescription = "وسط شاشة الاستوديو أسفل مشغل الفيديو مباشرة",
                icon = Icons.Default.AutoAwesome,
                iconColor = Color(0xFFFBBF24),
                whatItDoes = "يعرض أسطر الحوار العربي مع تمرير تلقائي وتظليل السطر النشط باللون الذهبي متزامناً مع الثواني والأجزاء.",
                howToUse = "يمكنك التبديل بين نمط السكربت العادي ونمط التليبرومبتر عبر زر التبديل، وضبط سرعة التمرير وحجم الخط.",
                proTip = "يمكنك تعديل أي سطر حواري أو إعادة صياغته بنقرة واحدة على أيقونة القلم بجانب النص."
            ),
            ToolGuideItem(
                id = "studio_mixer_3track",
                titleArabic = "مكساج الصوت ثلاثي المسارات (3-Track Mixer)",
                category = "🎙️ الاستوديو",
                targetTab = AppTab.STUDIO,
                locationDescription = "شريط تحكم الصوت أسفل التايم لاين وفي مركز المعالجة",
                icon = Icons.Default.GraphicEq,
                iconColor = Color(0xFF34D399),
                whatItDoes = "يتحكم بمستويات 3 مسارات مستقلة: صوت الدبلجة البشرية، صوت الفيديو الأصلي، وموسيقى الخلفية الحماسية.",
                howToUse = "اسحب مزلاجات الصوت لكل مسار (0% إلى 200%)، أو استخدم زر Mute لكتم المسار الأصلي تماماً وسماع صوتك فقط.",
                proTip = "اضغط على زر [موازنة ذكية Quick Balance] لضبط النسب الاحترافية القياسية بلمسة واحدة."
            ),
            ToolGuideItem(
                id = "studio_effects",
                titleArabic = "مؤثرات الاستوديو ونبرات الشخصيات (Voice Presets & DSP)",
                category = "🎙️ الاستوديو",
                targetTab = AppTab.STUDIO,
                locationDescription = "زر [المؤثرات] في أعلى الاستوديو واللوحة المنبثقة",
                icon = Icons.Default.Tune,
                iconColor = Color(0xFF60A5FA),
                whatItDoes = "معالجة طبقات الصوت الرقمية (DSP) لإعطاء صوتك طابع الرواة الأسطوريين، الأبطال، الأشرار، أو فلاتر الراديو والصدى.",
                howToUse = "اختر أحد المؤثرات الجاهزة (صوت البطل، صدى درامي، صوت الفضاء، الراديو القديم) وسيطبق فورياً على تسجيلك.",
                proTip = "اختر تأثير [صدى المسرح الدرامي Drama Echo] عند دبلجة اللحظات الحماسية والمؤثرة."
            ),

            // --- Video Dubber Tools ---
            ToolGuideItem(
                id = "video_dub_auto",
                titleArabic = "دبلجة الفيديو الذكية واستخراج الحوار (AI Auto Video Dubber)",
                category = "📹 دبلجة فيديو",
                targetTab = AppTab.VIDEO_DUB,
                locationDescription = "التبويب الثاني في الشريط السفلي [دبلجة فيديو 📹]",
                icon = Icons.Default.Videocam,
                iconColor = Color(0xFFF43F5E),
                whatItDoes = "استيراد أي فيديو خارجي من الهاتف، وتحليله بالذكاء الاصطناعي لفصل الصوت، واستخراج النص، وإعادة توليد الدبلجة.",
                howToUse = "اضغط [استيراد فيديو من الهاتف]، اختر مقطع MP4، ثم اضغط [بدء المعالجة الآلية] ليقوم النظام بفصل المسارات وتجهيز السكربت.",
                proTip = "الفيديوهات ذات الصوت الواضح تعطي دقة تفريغ نصي تفوق 98% وتزامن شفايف فائق الدقة."
            ),
            ToolGuideItem(
                id = "video_dub_stt_transcribe",
                titleArabic = "التفريغ الصوتي وترجمة الحوار (Speech-to-Text & Script)",
                category = "📹 دبلجة فيديو",
                targetTab = AppTab.VIDEO_DUB,
                locationDescription = "داخل شاشة دبلجة الفيديو بعد استيراد المقطع",
                icon = Icons.Default.AutoAwesome,
                iconColor = Color(0xFFA855F7),
                whatItDoes = "تحويل الكلمات المنطوقة إلى نص عربي منظم ومقسم إلى مقاطع زمنية (Segments) محددة بالبداية والنهاية.",
                howToUse = "راجع النصوص المستخرجة وعدّل أي كلمة أو اسم شخصية مباشرة داخل الجدول الزمني.",
                proTip = "يمكنك تصدير هذه النصوص كملف ترجمة SRT منفصل للاستخدام على يوتيوب ومنصات الفيديو."
            ),

            // --- Pre-Export Processing & Preview Hub Tools ---
            ToolGuideItem(
                id = "proc_side_by_side",
                titleArabic = "المعاينة التفاعلية المزدوجة (Side-by-Side Viewport)",
                category = "🎬 معاينة ومعالجة",
                targetTab = AppTab.PROCESSING_PREVIEW,
                locationDescription = "التبويب الثالث [معاينة ومعالجة 🎬] أو زر الفيلم في شريط الاستوديو",
                icon = Icons.Default.Movie,
                iconColor = Color(0xFFEC4899),
                whatItDoes = "تشغيل الفيديو الأصلي جنباً إلى جنب بالتزامن الدقيق مع الفيديو المدبلج لمقارنة تعبيرات الوجه وحركة الشفاه.",
                howToUse = "اختر نمط العرض [جنباً إلى جنب] ثم اضغط زر التشغيل لمراقبة الشاشتين متزامنتين بالملي ثانية.",
                proTip = "راقب عداد SMPTE الزمني للتأكد من أن نبرات الانفعال تطابق حركات الشخصية."
            ),
            ToolGuideItem(
                id = "proc_wipe_slider",
                titleArabic = "ممسحة المقارنة التفاعلية (Interactive Split Wipe Slider)",
                category = "🎬 معاينة ومعالجة",
                targetTab = AppTab.PROCESSING_PREVIEW,
                locationDescription = "خيار [ممسحة مقارنة Split] داخل مركز المعالجة والمعاينة",
                icon = Icons.Default.Tune,
                iconColor = Color(0xFF06B6D4),
                whatItDoes = "شاشة واحدة مقسومة بخط عمودي يمكنك سحبه بإصبعك لمشاهدة الجزء الأصلي على اليسار والمدبلج على اليمين مباشرة.",
                howToUse = "اسحب الخط الفاصل يمنة ويسرة أثناء تشغيل الفيديو لمقارنة نقاء الصورة واندماج الدبلجة.",
                proTip = "تساعدك هذه الميزة على كشف أي تأخير في تزامن حركة الشفاه (Lip-sync Latency)."
            ),
            ToolGuideItem(
                id = "proc_auto_ducking",
                titleArabic = "التخفيض الذكي التلقائي للموسيقى (Smart Auto-Ducking)",
                category = "🎬 معاينة ومعالجة",
                targetTab = AppTab.PROCESSING_PREVIEW,
                locationDescription = "قسم [هندسة ومكساج الصوت] داخل شاشة المعالجة والمعاينة",
                icon = Icons.Default.GraphicEq,
                iconColor = Color(0xFF10B981),
                whatItDoes = "يقوم بخفض مستوى موسيقى الخلفية تلقائياً بمقدار 70% عند بدء صوت الحوار البشري، وإعادتها فور صمت المؤدي.",
                howToUse = "قم بتفعيل مفتاح [تخفيض ذكي Auto-Ducking] ليتم الماسترينج تلقائياً دون الحاجة لتعديلات يدوية معقدة.",
                proTip = "هذا هو السر التقني المستخدم في كبرى استوديوهات الدبلجة لضمان وضوح كل كلمة مع الحفاظ على الحماس الموسيقي."
            ),
            ToolGuideItem(
                id = "proc_tashkeel_audit",
                titleArabic = "مدقق التشكيل والنطق ومخارج الحروف (Tashkeel & Phonetics Audit)",
                category = "🎬 معاينة ومعالجة",
                targetTab = AppTab.PROCESSING_PREVIEW,
                locationDescription = "قسم [التدقيق الصوتي واللغوي] في شاشة المعالجة",
                icon = Icons.Default.AutoAwesome,
                iconColor = Color(0xFFF59E0B),
                whatItDoes = "فحص نصوص الحوار العربي لاكتشاف الحركات الإعرابية الناقصة أو الأخطاء الشائعة وحساب درجة الفصاحة (0-100%).",
                howToUse = "اضغط زر [فحص وتشكيل الحركات ✍️] لمراجعة التقرير، ثم اضغط [تصحيح وتشكيل آلي] لإضافة الحركات الإعرابية تلقائياً.",
                proTip = "التشكيل الصحيح للكلمات يمنح أصوات الذكاء الاصطناعي والمؤدين البشر نطاقاً فصيحاً لا تشوبه لكنة."
            ),
            ToolGuideItem(
                id = "proc_export_settings",
                titleArabic = "محرك إعدادات الدقة والترميز (Render & Export Settings)",
                category = "🎬 معاينة ومعالجة",
                targetTab = AppTab.PROCESSING_PREVIEW,
                locationDescription = "قسم [إعدادات الفيديو والتصدير] في شاشة المعالجة",
                icon = Icons.Default.Download,
                iconColor = Color(0xFF38BDF8),
                whatItDoes = "تحديد أبعاد الفيديو (4K Ultra HD, 1080p FHD, 720p HD)، معدل الإطارات (60/30/24 FPS)، وتضمين الترجمة المدمجة.",
                howToUse = "حدد الدقة ومعدل البت ومعدل الإطارات المناسب لمنصة النشر (يوتيوب، تيك توك، انستغرام).",
                proTip = "اختر دقة 1080p بمعدل 30FPS لحجم ملف مثالي وسرعة تصدير فائقة وجودة نقية."
            ),

            // --- Instant Dubbing ---
            ToolGuideItem(
                id = "instant_live",
                titleArabic = "الدبلجة الحية المباشرة (Instant Live Dubbing)",
                category = "⚡ دبلجة فورية",
                targetTab = AppTab.INSTANT_DUB,
                locationDescription = "التبويب الرابع في الشريط السفلي [دبلجة فورية ⚡]",
                icon = Icons.Default.AutoAwesome,
                iconColor = Color(0xFFEAB308),
                whatItDoes = "تسجيل صوتي فوري وتلقائي يتزامن مع الفيديو دون الحاجة لتقسيم المشاهد مسبقاً، مثالي للتدريب السريع وتحديات الدبلجة.",
                howToUse = "اضغط [ابدأ التحدي الفوري]، وتابع مؤشر البدء بالألوان الأخضر والأحمر للإلقاء في الوقت المناسب تماماً.",
                proTip = "تحتوي هذه الشاشة على مؤقت إشعار بصري ينبهك قبل ثانيتين من بدء دورك في الكلام."
            ),

            // --- AI Text to Speech ---
            ToolGuideItem(
                id = "ai_tts_voices",
                titleArabic = "تحويل النص إلى صوت واستنساخ الأنمي (AI Text-to-Speech)",
                category = "🗣️ نص لصوت",
                targetTab = AppTab.AI_DUB,
                locationDescription = "التبويب الخامس في الشريط السفلي [نص لصوت 🗣️]",
                icon = Icons.Default.GraphicEq,
                iconColor = Color(0xFF8B5CF6),
                whatItDoes = "توليد أصوات عربية فصيحة واقعية بمختلف النبرات (صوت البطل، صوت الحكيم، صوت الراوي الكلاسيكي، صوت الشرير).",
                howToUse = "اكتب النص العربي في المربع، اختر الشخصية ونبرة الصوت وسرعة الإلقاء، ثم اضغط [توليد الصوت وسماعه].",
                proTip = "يمكنك إضافة التشكيل التلقائي للنص قبل التوليد للحصول على أقصى درجات النقاء اللغوي."
            ),

            // --- Gemini One-Click Dubbing ---
            ToolGuideItem(
                id = "gemini_one_click",
                titleArabic = "دبلجة Gemini الشاملة بضغطة زر (Audio -> Text -> Translate -> Speech)",
                category = "⚡ الذكاء الاصطناعي",
                targetTab = AppTab.GEMINI_ONE_CLICK,
                locationDescription = "التبويب المخصص في قائمة [المزيد من الأدوات ⚡]",
                icon = Icons.Default.AutoAwesome,
                iconColor = Color(0xFF38BDF8),
                whatItDoes = "تحويل الصوت إلى نص (Speech-to-Text)، وترجمته للغة المستهدفة، وتوليد نطق صوتي واقعي فوراً بضغطة زر واحدة عبر Gemini.",
                howToUse = "سجل صوتك أو اختر مقطعاً صوتياً، وحدد اللغة المستهدفة، ثم اضغط زر الدبلجة الموحدة لإنشاء الدبلجة بالكامل تلقائياً.",
                proTip = "يمكنك فحص واختبار اتصال Gemini API ومفتاحك في نفس الشاشة لضمان استجابة سريعة ودقيقة."
            ),

            // --- Projects Management ---
            ToolGuideItem(
                id = "projects_manager",
                titleArabic = "إدارة المشاريع والمسودات (Projects & Drafts)",
                category = "📁 مشاريعي",
                targetTab = AppTab.PROJECTS,
                locationDescription = "التبويب السابع في الشريط السفلي [مشاريعي 📁]",
                icon = Icons.Default.Folder,
                iconColor = Color(0xFFF97316),
                whatItDoes = "حفظ واسترجاع كل مشاريع الدبلجة بمساراتها الصوتية ونصوصها، مع إمكانية تعديلها أو تصديرها في أي وقت.",
                howToUse = "اضغط على أي مشروع محفوظ لفتحه ومتابعة العمل عليه، أو استخدم خيارات الحذف والمشاركة والنسخ الاحتياطي.",
                proTip = "يقوم التطبيق بحفظ عملك تلقائياً كمسودة آمنة لتجنب فقدان أي بيانات في حال إغلاق التطبيق."
            ),

            // --- Unified Security & Developer Portal ---
            ToolGuideItem(
                id = "security_dev_portal",
                titleArabic = "أمان الأجهزة وبوابة التطوير (Security & Developer Portal)",
                category = "🛡️ الأمان والتطوير",
                targetTab = AppTab.SECURITY_DASHBOARD,
                locationDescription = "التبويب المخصص في قائمة [المزيد من الأدوات ⚡] والإعدادات",
                icon = Icons.Default.Security,
                iconColor = Color(0xFFFFD54F),
                whatItDoes = "لوحة موحدة لرادار صد الهجمات وتتبع أجهزة Firebase Auth مع وصول مباشر لأدوات المطور وفحص المحركات وتشخيص Gemini API.",
                howToUse = "ادخل مباشرة للوحة لمراجعة سلامة النظام وسجل الأجهزة المصرحة وطباعة التقارير وإجراء اختبارات المحركات.",
                proTip = "يمكن فحص حالة المحركات والاتصال بنقرة واحدة بدون الحاجة لرموز أو تسجيل دخول معقد."
            ),

            // --- Update & Web Portal ---
            ToolGuideItem(
                id = "voicemaster_updates",
                titleArabic = "مركز التحديثات وبوابة الويب voicemaster.org (Updates & Web Portal)",
                category = "⚡ دبلجة فورية",
                targetTab = AppTab.UPDATE_CENTER,
                locationDescription = "قائمة الأدوات الإضافية [مركز التحديثات والويب 🌐]",
                icon = Icons.Default.OpenInNew,
                iconColor = Color(0xFF38BDF8),
                whatItDoes = "بوابة توزيع التحديثات الرسمية voicemaster.org لفحص وتنزيل أحدث حزم APK واستعراض سجل التحسينات.",
                howToUse = "ادخل مركز التحديثات لفحص الإصدارات الجديدة، أو افتح صفحة الويب لتنزيل ملف الـ APK المباشر على أي جهاز.",
                proTip = "يمكن تشغيل خادم الويب المدمج والدخول من متصفح الكمبيوتر أو هاتف آخر على نفس شبكة الواي فاي بسهولة."
            ),

            // --- Export Dialog ---
            ToolGuideItem(
                id = "export_dialog",
                titleArabic = "نافذة التصدير الاحترافي الشامل (Export Project Dialog)",
                category = "📤 التصدير",
                targetTab = null,
                locationDescription = "زر [تصدير 📤] في الزاوية العلوية للاستوديو وشاشة المعالجة",
                icon = Icons.Default.Download,
                iconColor = Color(0xFF10B981),
                whatItDoes = "تصدير العمل النهائي بصيغ متعددة: فيديو MP4 مدمج بالكامل، صوت MP3 ماستر، ملف ترجمة SRT، أو حزمة مضغوطة ZIP.",
                howToUse = "انقر زر [تصدير المشروع]، اختر الصيغة والجودة المطلوبة، ثم اضغط [بدء التصدير] واحفظ الملف في هاتفك أو شاركه.",
                proTip = "الملفات المصدرة تُحفظ في مجلد الفيديوهات (Movies/DubbedVideos) لتظهر فوراً في تطبيق المعرض (Gallery)."
            )
        )
    }

    // Filtered Items
    val filteredTools = remember(searchQuery, selectedCategory, allTools) {
        allTools.filter { item ->
            val matchesCategory = (selectedCategory == "الكل" || item.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    item.titleArabic.contains(searchQuery, ignoreCase = true) ||
                    item.whatItDoes.contains(searchQuery, ignoreCase = true) ||
                    item.howToUse.contains(searchQuery, ignoreCase = true) ||
                    item.locationDescription.contains(searchQuery, ignoreCase = true) ||
                    item.proTip.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_guide_and_pdf_screen")
    ) {
        Scaffold(
            containerColor = Color(0xFF0F0C16),
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            item {
                GuideHeroHeader(
                    onGeneratePdf = {
                        isGeneratingPdf = true
                        pdfStatusMessage = "جاري توليد كتيب الدليل بصيغة PDF..."
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                PdfManualGenerator.generateCompleteAppManualPdf(context)
                            }
                            isGeneratingPdf = false
                            if (result.success && result.file != null) {
                                generatedPdfFile = result.file
                                pdfStatusMessage = result.message
                                Toast.makeText(context, "تم إنشاء ملف PDF بنجاح! 📄", Toast.LENGTH_LONG).show()
                            } else {
                                pdfStatusMessage = result.message
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    isGeneratingPdf = isGeneratingPdf,
                    generatedFile = generatedPdfFile,
                    pdfStatusMessage = pdfStatusMessage,
                    onOpenPdf = { file ->
                        PdfManualGenerator.openOrSharePdf(context, file, isShare = false)
                    },
                    onSharePdf = { file ->
                        PdfManualGenerator.openOrSharePdf(context, file, isShare = true)
                    }
                )
            }

            // Interactive Onboarding Tour Launcher Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("launch_interactive_tour_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1528)),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFFD0BCFF), Color(0xFF34D399))))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFD0BCFF).copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFD0BCFF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "💡 الجولة الإرشادية التفاعلية (Tour)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "فقاعات إرشادية خطوة بخطوة لأهم أدوات التطبيق",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showTourOverlay = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_tour_from_guide_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("بدء الجولة الآن 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    OnboardingTourManager.setTourCompleted(context, false)
                                    Toast.makeText(context, "تمت إعادة ضبط الجولة، ستظهر تلقائياً في الاستوديو!", Toast.LENGTH_SHORT).show()
                                    onNavigateToTab(AppTab.STUDIO)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reset_tour_from_guide_btn"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFF))
                            ) {
                                Text("إعادة الضبط والانتقال 🎙️", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Quick App Architecture Map
            item {
                AppArchitectureFlowCard()
            }

            // Search and Category Filter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guide_search_input"),
                        placeholder = {
                            Text(
                                "ابحث عن أي أداة، شاشة، ميزة أو طريقة استخدام...",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = Color(0xFFA78BFA)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Text("✕", color = Color(0xFF94A3B8), fontSize = 14.sp)
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E182A),
                            unfocusedContainerColor = Color(0xFF181322),
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color(0xFF332747),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    // Category Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7C3AED),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E182A),
                                    labelColor = Color(0xFFCBD5E1)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFA78BFA) else Color(0xFF372C4C)
                                )
                            )
                        }
                    }
                }
            }

            // Section Title with results count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الأدوات والشاشات (${filteredTools.size}):",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                    Text(
                        text = "اضغط على الانتقال السريع للانتقال للشاشة",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Tool Items
            if (filteredTools.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1628)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 36.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "لم نجد أي أداة مطابقة لبحثك",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "جرب كتابة كلمات أخرى مثل (تسجيل، تشكيل، مكساج، تصدير، أنمي)",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredTools, key = { it.id }) { tool ->
                    ToolExplanationCard(
                        tool = tool,
                        onNavigate = { tab ->
                            onNavigateToTab(tab)
                        }
                    )
                }
            }

            // Step by step workflow
            item {
                ProductionWorkflowGuideCard()
            }

            // FAQ & Tips
            item {
                FaqGuideCard()
            }
        }
    }

    // Interactive Tour Overlay
    OnboardingTourOverlay(
        isVisible = showTourOverlay,
        onDismiss = { showTourOverlay = false },
        onFinishTour = { showTourOverlay = false }
    )
}
}

/**
 * Top Hero Header with Instant PDF Export Button
 */
@Composable
fun GuideHeroHeader(
    onGeneratePdf: () -> Unit,
    isGeneratingPdf: Boolean,
    generatedFile: File?,
    pdfStatusMessage: String?,
    onOpenPdf: (File) -> Unit,
    onSharePdf: (File) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guide_hero_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2E1065),
                            Color(0xFF170D2B)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "دليل الاستخدام وكتيب PDF الشامل 📖",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "شرح مفصل لكل أداة، مكانها، وظيفتها، وتوليد كتيب رسمي",
                            fontSize = 12.sp,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                }

                Text(
                    text = "يتضمن هذا الدليل شرحاً وافياً لجميع أجزاء استوديو الدبلجة: مسارات الصوت، الذكاء الاصطناعي، التشكيل والتدقيق، المعاينة المزدوجة، وخيارات التصدير. يمكنك تصدير الدليل كملف PDF عالي الجودة وحفظه أو طباعته.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFE2E8F0)
                )

                HorizontalDivider(color = Color(0xFF4C1D95).copy(alpha = 0.6f))

                // PDF Generator Action Area
                if (generatedFile == null) {
                    Button(
                        onClick = onGeneratePdf,
                        enabled = !isGeneratingPdf,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_pdf_manual_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            contentColor = Color.White
                        )
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("جاري إنشاء كتيب الدليل PDF...", fontSize = 13.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "توليد وتصدير كتيب الدليل بصيغة PDF 📄",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "تم تجهيز كتيب PDF بنجاح (${(generatedFile.length() / 1024)} KB)",
                                    color = Color(0xFFD1FAE5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onOpenPdf(generatedFile) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("open_generated_pdf_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("فتح ملف PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onSharePdf(generatedFile) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("share_generated_pdf_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFE2E8F0)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFA78BFA))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("مشاركة PDF", fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = onGeneratePdf,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "إعادة التوليد",
                                    tint = Color(0xFFA78BFA)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual App Architecture Flow
 */
@Composable
fun AppArchitectureFlowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191424)),
        border = BorderStroke(1.dp, Color(0xFF33274A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "خريطة تدفق العمل في التطبيق (App Workflow Map):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "1. اختيار المشهد أو الفيديو ➔ 2. قراءة السكربت والتسجيل ➔ 3. مكساج المسارات والمؤثرات ➔ 4. التدقيق والمعاينة المزدوجة ➔ 5. التصدير النهائي (MP4/MP3/SRT)",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Individual Tool Card with full explanations and instant jump
 */
@Composable
fun ToolExplanationCard(
    tool: ToolGuideItem,
    onNavigate: (AppTab) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tool_guide_card_${tool.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E172B)),
        border = BorderStroke(1.dp, Color(0xFF3A2D52))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tool.iconColor.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, tool.iconColor.copy(alpha = 0.5f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = tool.iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = tool.titleArabic,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = tool.locationDescription,
                            fontSize = 11.sp,
                            color = Color(0xFFA78BFA)
                        )
                    }
                }

                if (tool.targetTab != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF7C3AED).copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color(0xFF7C3AED)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigate(tool.targetTab) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "انتقال سريع 🚀",
                                fontSize = 11.sp,
                                color = Color(0xFFD8B4FE),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF332747))

            // What it does
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "⚙️ ما تفعله الأداة:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = tool.whatItDoes,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFE2E8F0)
                )
            }

            // How to use
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🎯 طريقة الاستخدام:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                )
                Text(
                    text = tool.howToUse,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFE2E8F0)
                )
            }

            // Pro Tip Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF281D3E),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "نصيحة احترافية: ${tool.proTip}",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFFFEF3C7)
                    )
                }
            }
        }
    }
}

/**
 * Practical Step-by-Step Workflow
 */
@Composable
fun ProductionWorkflowGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161221)),
        border = BorderStroke(1.dp, Color(0xFF3A2D52))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎬 خطوات دبلجة أول مشهد احترافي (Step-by-Step Guide):",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24)
            )

            val steps = listOf(
                "1. اختر مشهداً من تبويب [المشاهد 🎞️] أو استورد فيديو خارجي من [دبلجة فيديو 📹].",
                "2. في [الاستوديو 🎙️]، اضغط زر التشغيل لمشاهدة الفيديو وحفظ توقيت الحوار.",
                "3. ارتدِ سماعات الرأس ثم اضغط على زر التسجيل الأحمر لبدء الإلقاء بصوتك.",
                "4. اضبط مستويات المسارات الثلاثة، واختر مؤثر الصوت (مثل صدى المسرح أو البطل).",
                "5. انتقل لتبويب [معاينة ومعالجة 🎬] واستخدم ممسحة المقارنة Split لتفقد تزامن الشفاه.",
                "6. اضغط على [تدقيق الحركات ✍️] للتأكد من التشكيل العربي الفصيح.",
                "7. اضغط [تصدير المشروع] واختر فيديو MP4 بجودة 1080p لتحصل على فيديو مدبلج متكامل!"
            )

            for (step in steps) {
                Text(
                    text = step,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFE2E8F0)
                )
            }
        }
    }
}

/**
 * FAQ & Troubleshooting
 */
@Composable
fun FaqGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161221)),
        border = BorderStroke(1.dp, Color(0xFF3A2D52))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "❓ الأسئلة الشائعة وحلول المشاكل (FAQ):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            val faqs = listOf(
                "س: صوتي يظهر به صدى خارجي أو تسمع صوت الفيديو معه؟" to "ج: هذا يحدث عند استخدام مكبرات الصوت الخارجية للهاتف. الحل هو وضع سماعة رأس سلكية أو بلوتوث أثناء التسجيل.",
                "س: أين أجد الفيديوهات التي قمت بتصديرها على هاتفي؟" to "ج: تُحفظ في الذاكرة الرئيسية بمجلد Movies/DubbedVideos وتظهر مباشرة في تطبيق الصور ومعرض الهاتف.",
                "س: كيف أجعل صوت الموسيقى ينخفض عندما أبدأ بالكلام؟" to "ج: في شاشة [معاينة ومعالجة]، قم بتفعيل خيار [تخفيض ذكي Auto-Ducking].",
                "س: كيف أضيف نصوص وحوارات مخصصة لمشهد خارجي؟" to "ج: في الاستوديو اضغط على أيقونة (+) لإضافة سطر جديد أو عدل الأسطر الحالية مباشرة."
            )

            for ((q, a) in faqs) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = q, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD8B4FE))
                    Text(text = a, fontSize = 11.5.sp, lineHeight = 16.sp, color = Color(0xFFCBD5E1))
                }
            }
        }
    }
}
