package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High Quality Arabic PDF Manual Generator
 * Generates an official, comprehensive user guide and tool reference manual for the Dubbing App.
 */
object PdfManualGenerator {

    data class PdfGenerationResult(
        val success: Boolean,
        val file: File? = null,
        val uri: Uri? = null,
        val message: String = ""
    )

    fun generateCompleteAppManualPdf(context: Context): PdfGenerationResult {
        val document = PdfDocument()

        val pageWidth = 595 // A4 standard width in points (72 dpi)
        val pageHeight = 842 // A4 standard height in points (72 dpi)

        try {
            // Colors
            val primaryPurple = Color.rgb(124, 58, 237)
            val darkPurple = Color.rgb(30, 27, 75)
            val accentGreen = Color.rgb(16, 185, 129)
            val darkBg = Color.rgb(15, 12, 22)
            val cardBg = Color.rgb(248, 250, 252)
            val textDark = Color.rgb(30, 41, 59)
            val textGray = Color.rgb(100, 116, 139)
            val borderLight = Color.rgb(226, 232, 240)

            // Paints
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }

            val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(216, 180, 254)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
            }

            val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryPurple
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }

            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textDark
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
            }

            val boldBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textDark
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }

            val smallGrayPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textGray
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
            }

            // -------------------------------------------------------------
            // PAGE 1: Cover & Program Overview + Studio Guide
            // -------------------------------------------------------------
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = document.startPage(pageInfo1)
            val canvas1 = page1.canvas

            // Background Header
            paint.color = primaryPurple
            canvas1.drawRect(0f, 0f, pageWidth.toFloat(), 130f, paint)

            paint.color = darkPurple
            canvas1.drawRect(0f, 130f, pageWidth.toFloat(), 140f, paint)

            // Header Content
            canvas1.drawText("دليل الاستخدام الشامل - فويس ماستر برو | VoiceMaster Pro 🎙️", pageWidth - 30f, 45f, titlePaint)
            canvas1.drawText("جميع حقوق الملكية الفكرية محفوظة © 2026 للمطور: محمد رضا محمود محمود السيد سليمة", pageWidth - 30f, 75f, subtitlePaint)
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            canvas1.drawText("مصر - المنوفية - شبين الكوم - شارع القفاص • تاريخ الإصدار: $dateStr • v2.6 Pro", pageWidth - 30f, 105f, subtitlePaint)

            // Overview Box
            var currentY = 160f
            paint.color = cardBg
            canvas1.drawRoundRect(RectF(30f, currentY, pageWidth - 30f, currentY + 75f), 8f, 8f, paint)
            paint.color = borderLight
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas1.drawRoundRect(RectF(30f, currentY, pageWidth - 30f, currentY + 75f), 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            canvas1.drawText("📌 نظرة عامة على البرنامج وماذا يقدم:", pageWidth - 45f, currentY + 24f, sectionTitlePaint)
            val overviewText = "استوديو الدبلجة الاحترافي هو تطبيق متكامل لدبلجة الرسوم المتحركة والفيديوهات باللغة العربية الفصحى وأسلوب الدبلجة الكلاسيكية الأصيل. يشمل تسجيل الصوت البشري، الذكاء الاصطناعي لفصل وتحويل الصوت، التدقيق اللغوي والتشكيل، مكساج 3 مسارات، ومعاينة ومقارنة دقيقة قبل التصدير بصيغ MP4 و MP3 و SRT."
            drawRtlParagraph(canvas1, overviewText, 45f, currentY + 38f, pageWidth - 90f, bodyPaint)

            // Section 1: Studio Screen (الاستوديو)
            currentY += 95f
            drawSectionCard(
                canvas = canvas1,
                title = "1. شاشة استوديو الدبلجة الرئيسية (Studio Screen)",
                location = "الموقع: التبويب الأول في الشريط السفلي [الاستوديو 🎙️]",
                yStart = currentY,
                height = 230f,
                pageWidth = pageWidth,
                sectionTitlePaint = sectionTitlePaint,
                boldBodyPaint = boldBodyPaint,
                bodyPaint = bodyPaint,
                smallGrayPaint = smallGrayPaint
            )

            val studioTools = listOf(
                "• مسار الفيديو والتليبرومبتر (Teleprompter):" to "عرض الفيديو المتزامن مع سكرول تلقائي لكلمات الحوار وتحديد السطر النشط بلون ذهبي.",
                "• محرك التسجيل البشري (Voice Recorder):" to "تسجيل أداء صوتي بجودة استوديو 48kHz مع عداد تنازلي ومؤقت SMPTE فائق الدقة.",
                "• مكساج 3 مسارات (3-Track Audio Mixer):" to "التحكم المستقل بصوت المؤدي، والصوت الأصلي، وموسيقى الخلفية مع كتم فوري (Mute).",
                "• مؤثرات ونبرات الصوت (Voice Effects & Pitch):" to "تطبيق نبرات الأبطال، الأشرار، الصدى الملحمي، وفلاتر الراديو ونقاء الصوت البشري.",
                "• شريط التايم لاين والقفز إطاراً بإطار:" to "التنقل الدقيق في أجزاء الثانية وضبط بداية ونهاية كل سطر حواري بنقرة زر."
            )
            drawToolList(canvas1, studioTools, currentY + 45f, pageWidth - 50f, boldBodyPaint, bodyPaint)

            // Section 2: Video Dubber Screen (دبلجة الفيديو)
            currentY += 245f
            drawSectionCard(
                canvas = canvas1,
                title = "2. دبلجة الفيديو الذكية بالذكاء الاصطناعي (Video Auto Dubber)",
                location = "الموقع: التبويب الثاني في الشريط السفلي [دبلجة فيديو 📹]",
                yStart = currentY,
                height = 220f,
                pageWidth = pageWidth,
                sectionTitlePaint = sectionTitlePaint,
                boldBodyPaint = boldBodyPaint,
                bodyPaint = bodyPaint,
                smallGrayPaint = smallGrayPaint
            )

            val videoDubTools = listOf(
                "• استيراد الفيديو المخصص (Import Video):" to "اختيار أي فيديو MP4 من الهاتف أو المشاهد الجاهزة مع استخراج الصوت تلقائياً.",
                "• التعرف الصوتي الذكي (Speech-to-Text):" to "تحويل الحوار المنطوق في الفيديو إلى نصوص عربية متزامنة مع التوقيتات الزمنية.",
                "• الترجمة وتكييف الحوار (Dialogue Adaptation):" to "تعديل الجمل لتناسب حركة الشفاه (Lip-Sync) والأسلوب العربي الفصيح.",
                "• استنساخ وتوليد الصوت (Voice Synthesis):" to "توليد أصوات الأنمي المدبلجة بدقة عالية ودمجها مع الموسيقى التصويرية."
            )
            drawToolList(canvas1, videoDubTools, currentY + 45f, pageWidth - 50f, boldBodyPaint, bodyPaint)

            // Footer Page 1
            canvas1.drawText("صفحة 1 من 3 • استوديو الدبلجة العربي الاحترافي", pageWidth / 2f + 50f, pageHeight - 20f, smallGrayPaint)
            document.finishPage(page1)

            // -------------------------------------------------------------
            // PAGE 2: Processing & Preview Hub + AI Dub + Instant Dub
            // -------------------------------------------------------------
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = document.startPage(pageInfo2)
            val canvas2 = page2.canvas

            // Top Header Mini
            paint.color = primaryPurple
            canvas2.drawRect(0f, 0f, pageWidth.toFloat(), 50f, paint)
            canvas2.drawText("مركز المعالجة والمعاينة • تحويل النص إلى صوت • الدبلجة الفورية", pageWidth - 30f, 32f, titlePaint.apply { textSize = 14f })

            currentY = 65f

            // Section 3: Video Processing & Preview Hub (مركز المعالجة والمعاينة)
            drawSectionCard(
                canvas = canvas2,
                title = "3. مركز معالجة ومعاينة الفيديو قبل التصدير (Processing & Preview Hub)",
                location = "الموقع: التبويب الثالث [معاينة ومعالجة 🎬] أو زر المقارنة في شريط الاستوديو",
                yStart = currentY,
                height = 250f,
                pageWidth = pageWidth,
                sectionTitlePaint = sectionTitlePaint,
                boldBodyPaint = boldBodyPaint,
                bodyPaint = bodyPaint,
                smallGrayPaint = smallGrayPaint
            )

            val processingTools = listOf(
                "• المعاينة جنباً إلى جنب (Side-by-Side):" to "عرض الفيديو الأصلي والفيديو المدبلج في شاشتين متزامنتين لمقارنة التوقيت والأداء.",
                "• ممسحة المقارنة التفاعلية (Split Wipe Slider):" to "سحب فاصل الشاشة يمنة ويسرة للمقارنة البصرية المباشرة وجودة الصورة.",
                "• التبديل الفوري (A/B Quick Flip):" to "التبديل بضغطة زر واحدة بين النسخة الأصلية والمدبلجة دون توقف التشغيل.",
                "• هندسة ومكساج الصوت (Audio Mastering):" to "موازنة تلقائية سريعة، ميزة التخفيض الذكي للموسيقى (Auto-Ducking)، ومؤشر VU.",
                "• تدقيق الحروف والتشكيل (Tashkeel & Phonetics):" to "فحص الحركات الإعرابية، الأخطاء الإملائية، والتشكيل الآلي بنقرة واحدة.",
                "• ضبط إعدادات التصدير (Video Config):" to "اختيار الدقة (4K/1080p/720p)، معدل الإطارات (60/30/24 FPS)، وحرق الترجمة."
            )
            drawToolList(canvas2, processingTools, currentY + 45f, pageWidth - 50f, boldBodyPaint, bodyPaint)

            // Section 4: Instant Dubbing (الدبلجة الفورية)
            currentY += 265f
            drawSectionCard(
                canvas = canvas2,
                title = "4. شاشة الدبلجة الحية الفورية (Instant Live Dubbing)",
                location = "الموقع: التبويب الرابع في الشريط السفلي [دبلجة فورية ⚡]",
                yStart = currentY,
                height = 210f,
                pageWidth = pageWidth,
                sectionTitlePaint = sectionTitlePaint,
                boldBodyPaint = boldBodyPaint,
                bodyPaint = bodyPaint,
                smallGrayPaint = smallGrayPaint
            )

            val instantTools = listOf(
                "• نمط الدبلجة المباشرة أثناء العرض:" to "تسجيل صوتك فورياً أثناء تشغيل المشهد دون الحاجة لتقطيع يدوي مسبق.",
                "• الملقن التلقائي فائق الوضوح:" to "عرض الكلمات مع وميض توقيت البدء لكل جملة لتسهيل الإلقاء المتقن.",
                "• التغذية الراجعة الصوتية المباشرة:" to "سماع صوتك مع التأثير المختار مباشرة عبر سماعات الرأس."
            )
            drawToolList(canvas2, instantTools, currentY + 45f, pageWidth - 50f, boldBodyPaint, bodyPaint)

            // Section 5: Text to Speech (نص لصوت)
            currentY += 225f
            drawSectionCard(
                canvas = canvas2,
                title = "5. تحويل النص إلى صوت واستنساخ النبرات (AI Text-to-Speech)",
                location = "الموقع: التبويب الخامس في الشريط السفلي [نص لصوت 🗣️]",
                yStart = currentY,
                height = 200f,
                pageWidth = pageWidth,
                sectionTitlePaint = sectionTitlePaint,
                boldBodyPaint = boldBodyPaint,
                bodyPaint = bodyPaint,
                smallGrayPaint = smallGrayPaint
            )

            val ttsTools = listOf(
                "• مكتبة الأصوات والشخصيات:" to "أصوات أبطال الأنمي، الرواة، الشخصيات الكرتونية، ونبرات الحكمة والقوة.",
                "• الضبط الصوتي للنبرة والسرعة:" to "التحكم في سرعة الإلقاء (Pitch / Speed) وعمق طبقة الصوت.",
                "• التشكيل العربي التلقائي الفوري:" to "إضافة الحركات الإعرابية للكلمات غير المشكولة لضمان سلامة النطق."
            )
            drawToolList(canvas2, ttsTools, currentY + 45f, pageWidth - 50f, boldBodyPaint, bodyPaint)

            // Footer Page 2
            canvas2.drawText("صفحة 2 من 3 • استوديو الدبلجة العربي الاحترافي", pageWidth / 2f + 50f, pageHeight - 20f, smallGrayPaint)
            document.finishPage(page2)

            // -------------------------------------------------------------
            // PAGE 3: Storage, Projects, Soundboard, Exporting & FAQ
            // -------------------------------------------------------------
            val pageInfo3 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
            val page3 = document.startPage(pageInfo3)
            val canvas3 = page3.canvas

            // Top Header Mini
            paint.color = primaryPurple
            canvas3.drawRect(0f, 0f, pageWidth.toFloat(), 50f, paint)
            canvas3.drawText("إدارة المشاريع • المؤثرات • التصدير • الأسئلة الشائعة", pageWidth - 30f, 32f, titlePaint.apply { textSize = 14f })

            currentY = 65f

            // Section 6: Projects & Clips Library
            drawSectionCard(
                canvas = canvas3,
                title = "6. مكتبة المشاهد وإدارة المشروعات (Clips & Projects)",
                location = "الموقع: تبويبي [المشاهد 🎞️] و [مشاريعي 📁] في الشريط السفلي",
                yStart = currentY,
                height = 160f,
                pageWidth = pageWidth,
                sectionTitlePaint = sectionTitlePaint,
                boldBodyPaint = boldBodyPaint,
                bodyPaint = bodyPaint,
                smallGrayPaint = smallGrayPaint
            )

            val storageTools = listOf(
                "• مكتبة المشاهد الكلاسيكية:" to "مشاهد تدريبية منوعة (كونان، ماوكلي، جزيرة الكنز، دراغون بول، أبطال الديجيتال).",
                "• حفظ المشاريع والمسودات (Drafts):" to "تخزين المسارات الصوتية، التعديلات، والنصوص مع إمكانية المتابعة بأي وقت.",
                "• النسخ الاحتياطي ومشاركة الحزم:" to "تصدير حزمة المشروع كملف مضغوط ZIP أو مشاركته مع أجهزة أخرى."
            )
            drawToolList(canvas3, storageTools, currentY + 45f, pageWidth - 50f, boldBodyPaint, bodyPaint)

            // Section 7: Soundboard & Sound Effects
            currentY += 175f
            drawSectionCard(
                canvas = canvas3,
                title = "7. لوحة المؤثرات الصوتية والسينمائية (Soundboard)",
                location = "الموقع: التبويب الثامن في الشريط السفلي [المؤثرات 🔊]",
                yStart = currentY,
                height = 140f,
                pageWidth = pageWidth,
                sectionTitlePaint = sectionTitlePaint,
                boldBodyPaint = boldBodyPaint,
                bodyPaint = bodyPaint,
                smallGrayPaint = smallGrayPaint
            )

            val sfxTools = listOf(
                "• مؤثرات الكرتون والأنمي الشهيرة:" to "أصوات اللمعان السحري، الضربات القتالية، الصدمة، الركض، وأصوات الحيوانات.",
                "• إضافة المؤثرات للتايم لاين:" to "دمج المؤثر الصوتي في أي ثانية محددة من المشهد لتعزيز الإثارة."
            )
            drawToolList(canvas3, sfxTools, currentY + 45f, pageWidth - 50f, boldBodyPaint, bodyPaint)

            // Section 8: Exporting Formats & Quality
            currentY += 155f
            drawSectionCard(
                canvas = canvas3,
                title = "8. خيارات التصدير والمشاركة (Export Options)",
                location = "الموقع: زر التصدير [تصدير 📤] في أعلى كل شاشة أو داخل مركز المعالجة",
                yStart = currentY,
                height = 150f,
                pageWidth = pageWidth,
                sectionTitlePaint = sectionTitlePaint,
                boldBodyPaint = boldBodyPaint,
                bodyPaint = bodyPaint,
                smallGrayPaint = smallGrayPaint
            )

            val exportTools = listOf(
                "• تصدير فيديو كامل (MP4 Video):" to "فيديو عالي الدقة يجمع الصورة، الصوت البشري، الموسيقى، والترجمة المدمجة.",
                "• تصدير الصوت الماستر (MP3 Audio):" to "ملف صوتي نقي وممكوس للمشاركة عبر تطبيقات الصوت والبودكاست.",
                "• تصدير ملف الترجمة (SRT Subtitles):" to "ملف ترجمة عربية متطابق مع التوقيت بالملي ثانية لموقع يوتيوب والمنصات."
            )
            drawToolList(canvas3, exportTools, currentY + 45f, pageWidth - 50f, boldBodyPaint, bodyPaint)

            // Section 9: Quick Troubleshooting & FAQ
            currentY += 165f
            paint.color = cardBg
            canvas3.drawRoundRect(RectF(30f, currentY, pageWidth - 30f, currentY + 185f), 8f, 8f, paint)
            paint.color = borderLight
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas3.drawRoundRect(RectF(30f, currentY, pageWidth - 30f, currentY + 185f), 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            canvas3.drawText("❓ أهم النصائح والأسئلة الشائعة (FAQ & Tips):", pageWidth - 45f, currentY + 22f, sectionTitlePaint)

            val tips = listOf(
                "1. للحصول على أفضل دبلجة، استخدم سماعات رأس سلكية أثناء التسجيل لتجنب التقاط صوت السماعات الخارجية.",
                "2. استخدم ميزة [داكينج Auto-Ducking] لخفض صوت الموسيقى تلقائياً بنسبة 70% عندما تبدأ بالحديث.",
                "3. قبل التصدير النهائي، افتح [معاينة ومعالجة] واضغط [تدقيق الحركات ✍️] ثم [تصحيح آلي] لضمان التشكيل الفصيح.",
                "4. جميع الفيديوهات المصدرة تُحفظ تلقائياً في مجلد الأفلام (Movies/DubbedVideos) ومتاحة في معرض الصور."
            )
            var tipY = currentY + 40f
            for (tip in tips) {
                drawRtlParagraph(canvas3, tip, 45f, tipY, pageWidth - 90f, bodyPaint)
                tipY += 32f
            }

            // Footer Page 3
            canvas3.drawText("صفحة 3 من 3 • نهاية الدليل • تم إنشاؤه عبر تطبيق استوديو الدبلجة", pageWidth / 2f + 50f, pageHeight - 20f, smallGrayPaint)
            document.finishPage(page3)

            // Save PDF to Documents/DubbingManual
            val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DubbingManual")
            if (!outputDir.exists()) outputDir.mkdirs()

            val pdfFile = File(outputDir, "VoiceMaster_Pro_Guide_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf")
            val fos = FileOutputStream(pdfFile)
            document.writeTo(fos)
            fos.flush()
            fos.close()
            document.close()

            val uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
            } catch (e: Exception) {
                Uri.fromFile(pdfFile)
            }

            return PdfGenerationResult(
                success = true,
                file = pdfFile,
                uri = uri,
                message = "تم إنشاء كتيب الدليل الشامل بصيغة PDF بنجاح في:\n${pdfFile.name}"
            )

        } catch (e: Exception) {
            try { document.close() } catch (_: Exception) {}
            return PdfGenerationResult(
                success = false,
                message = "حدث خطأ أثناء إنشاء ملف PDF: ${e.localizedMessage}"
            )
        }
    }

    private fun drawSectionCard(
        canvas: Canvas,
        title: String,
        location: String,
        yStart: Float,
        height: Float,
        pageWidth: Int,
        sectionTitlePaint: TextPaint,
        boldBodyPaint: TextPaint,
        bodyPaint: TextPaint,
        smallGrayPaint: TextPaint
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(248, 250, 252)
        val rect = RectF(30f, yStart, pageWidth - 30f, yStart + height)
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Header Strip inside Card
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(RectF(30f, yStart, pageWidth - 30f, yStart + 35f), 8f, 8f, paint)

        // Title and Location
        canvas.drawText(title, pageWidth - 45f, yStart + 18f, sectionTitlePaint)
        canvas.drawText(location, pageWidth - 45f, yStart + 30f, smallGrayPaint)
    }

    private fun drawToolList(
        canvas: Canvas,
        items: List<Pair<String, String>>,
        startY: Float,
        rightX: Float,
        boldPaint: TextPaint,
        bodyPaint: TextPaint
    ) {
        var y = startY
        for ((tool, desc) in items) {
            canvas.drawText(tool, rightX, y, boldPaint)
            drawRtlParagraph(canvas, desc, 45f, y + 12f, rightX - 45f, bodyPaint)
            y += 34f
        }
    }

    private fun drawRtlParagraph(canvas: Canvas, text: String, leftX: Float, topY: Float, width: Float, textPaint: TextPaint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
                .build()

            canvas.save()
            canvas.translate(leftX, topY)
            staticLayout.draw(canvas)
            canvas.restore()
        } else {
            @Suppress("DEPRECATION")
            val staticLayout = StaticLayout(
                text,
                textPaint,
                width.toInt(),
                Layout.Alignment.ALIGN_NORMAL,
                1.15f,
                0f,
                false
            )
            canvas.save()
            canvas.translate(leftX, topY)
            staticLayout.draw(canvas)
            canvas.restore()
        }
    }

    fun openOrSharePdf(context: Context, file: File, isShare: Boolean = false) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = if (isShare) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "دليل استخدام تطبيق استوديو الدبلجة")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            val chooser = Intent.createChooser(intent, if (isShare) "مشاركة كتيب الدليل PDF" else "فتح دليل الاستخدام PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
