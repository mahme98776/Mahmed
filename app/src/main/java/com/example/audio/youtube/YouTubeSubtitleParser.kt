package com.example.audio.youtube

import java.util.Locale
import java.util.regex.Pattern

data class YouTubeDubSegment(
    val index: Int,
    val startSeconds: Float,
    val endSeconds: Float,
    val sourceText: String,
    val translatedText: String = "",
    val audioPath: String? = null,
    val speedMultiplier: Float = 1.0f,
    val isProcessed: Boolean = false,
    val originalDurationSeconds: Float = (endSeconds - startSeconds).coerceAtLeast(0.5f),
    val synthesizedDurationSeconds: Float = 0f
) {
    val durationSeconds: Float
        get() = (endSeconds - startSeconds).coerceAtLeast(0.5f)

    val formattedStartTime: String
        get() = YouTubeSubtitleParser.formatTimestampDisplay(startSeconds)

    val formattedEndTime: String
        get() = YouTubeSubtitleParser.formatTimestampDisplay(endSeconds)
}

object YouTubeSubtitleParser {

    /**
     * Extracts video ID from common YouTube URL variants:
     * - https://www.youtube.com/watch?v=dQw4w9WgXcQ
     * - https://youtu.be/dQw4w9WgXcQ
     * - https://youtube.com/shorts/dQw4w9WgXcQ
     * - https://m.youtube.com/watch?v=dQw4w9WgXcQ
     */
    fun extractYouTubeVideoId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.length == 11 && !trimmed.contains("http") && !trimmed.contains("/")) {
            return trimmed
        }
        val patterns = listOf(
            "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?user_id=)[^#\\&\\?\\n]*",
            "(?<=shorts\\/)[^#\\&\\?\\n]*"
        )
        for (p in patterns) {
            val compiled = Pattern.compile(p)
            val matcher = compiled.matcher(trimmed)
            if (matcher.find()) {
                val match = matcher.group()
                if (match.isNotBlank()) return match
            }
        }
        return null
    }

    /**
     * Formats seconds into standard SRT format: 00:00:00,000
     */
    fun formatTimestampSrt(seconds: Float): String {
        val totalMs = (seconds * 1000f).toLong().coerceAtLeast(0L)
        val hours = totalMs / 3600000L
        val minutes = (totalMs % 3600000L) / 60000L
        val secs = (totalMs % 60000L) / 1000L
        val millis = totalMs % 1000L
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, secs, millis)
    }

    /**
     * Formats seconds into clean UI display: 00:00.0 or 00:00:00
     */
    fun formatTimestampDisplay(seconds: Float): String {
        val totalSec = seconds.toInt().coerceAtLeast(0)
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val secs = totalSec % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, secs)
        }
    }

    /**
     * Parses SRT or WebVTT timestamp string (e.g. 00:01:23,456 or 01:23.456) into seconds
     */
    fun parseTimestamp(timestampStr: String): Float {
        val clean = timestampStr.trim().replace(',', '.')
        val parts = clean.split(":")
        return try {
            when (parts.size) {
                3 -> {
                    val h = parts[0].toFloat()
                    val m = parts[1].toFloat()
                    val s = parts[2].toFloat()
                    h * 3600f + m * 60f + s
                }
                2 -> {
                    val m = parts[0].toFloat()
                    val s = parts[1].toFloat()
                    m * 60f + s
                }
                1 -> parts[0].toFloat()
                else -> 0f
            }
        } catch (_: Exception) {
            0f
        }
    }

    /**
     * Parses full SRT subtitle text into synchronized [YouTubeDubSegment] items.
     */
    fun parseSrt(srtContent: String): List<YouTubeDubSegment> {
        val segments = mutableListOf<YouTubeDubSegment>()
        if (srtContent.isBlank()) return segments

        val normalized = srtContent.replace("\r\n", "\n").replace("\r", "\n")
        val blocks = normalized.split(Regex("\\n\\s*\\n"))

        var autoIndex = 1
        for (block in blocks) {
            val lines = block.trim().lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            var timeLineIndex = -1
            for (i in lines.indices) {
                if (lines[i].contains("-->")) {
                    timeLineIndex = i
                    break
                }
            }

            if (timeLineIndex != -1) {
                val timeLine = lines[timeLineIndex]
                val timeParts = timeLine.split("-->")
                if (timeParts.size == 2) {
                    val startSec = parseTimestamp(timeParts[0])
                    val endSec = parseTimestamp(timeParts[1])
                    val textLines = lines.drop(timeLineIndex + 1)
                    val text = textLines.joinToString(" ").replace(Regex("<[^>]*>"), "").trim()

                    if (text.isNotBlank() && endSec > startSec) {
                        segments.add(
                            YouTubeDubSegment(
                                index = autoIndex++,
                                startSeconds = startSec,
                                endSeconds = endSec,
                                sourceText = text,
                                translatedText = ""
                            )
                        )
                    }
                }
            }
        }
        return segments
    }

    /**
     * Generates a standard SRT format file string from a list of segments.
     */
    fun generateSrt(segments: List<YouTubeDubSegment>, useTranslated: Boolean = true): String {
        val sb = StringBuilder()
        for ((idx, seg) in segments.withIndex()) {
            val lineNum = idx + 1
            val startTime = formatTimestampSrt(seg.startSeconds)
            val endTime = formatTimestampSrt(seg.endSeconds)
            val text = if (useTranslated && seg.translatedText.isNotBlank()) {
                seg.translatedText
            } else {
                seg.sourceText
            }

            sb.append(lineNum).append("\n")
            sb.append(startTime).append(" --> ").append(endTime).append("\n")
            sb.append(text).append("\n\n")
        }
        return sb.toString().trimEnd()
    }

    /**
     * Generates realistic demo YouTube subtitle segments based on clip theme.
     */
    fun generateDemoSegments(sampleKey: String): List<YouTubeDubSegment> {
        return when (sampleKey) {
            "tech" -> listOf(
                YouTubeDubSegment(1, 0.5f, 3.8f, "Welcome back everyone! Today we are testing next-gen artificial intelligence chips.", "أهلاً بكم مجدداً! اليوم نقوم باختبار شرائح الذكاء الاصطناعي للجيل القادم."),
                YouTubeDubSegment(2, 4.2f, 7.9f, "The processing speed is unprecedented, reaching up to fifty teraflops in real-time.", "سرعة المعالجة غير مسبوقة، حيث تصل إلى خمسين تيرافلوبس في الوقت الفعلي."),
                YouTubeDubSegment(3, 8.4f, 12.0f, "Let's benchmark the thermal performance under heavy neural network workloads.", "دعونا نقيس الأداء الحراري تحت أعباء عمل الشبكات العصبية المكثفة."),
                YouTubeDubSegment(4, 12.5f, 16.2f, "As you can see on the oscilloscope, power consumption remains surprisingly low.", "كما ترون على شاشة راسم الإشارة، يظل استهلاك الطاقة منخفضاً ومدهشاً."),
                YouTubeDubSegment(5, 16.8f, 20.5f, "Don't forget to like and subscribe for more deep tech teardowns!", "لا تنسوا الإعجاب والاشتراك لمتابعة المزيد من المراجعات التقنية العميقة!")
            )
            "nature" -> listOf(
                YouTubeDubSegment(1, 1.0f, 4.5f, "In the heart of the ancient rainforest, silence hides countless untold stories.", "في قلب الغابات المطيرة القديمة، يُخفي الصمت قصصاً لا تُحصى."),
                YouTubeDubSegment(2, 5.0f, 9.2f, "Predators move with silent grace as the twilight cast long shadows across the canopy.", "تتحرك المفترسات بركود وهدوء بينما يُلقي الغسق ظلالاً ممتدة عبر قمم الأشجار."),
                YouTubeDubSegment(3, 9.8f, 13.5f, "Every creature here has adapted to survive the relentless cycles of the wilderness.", "لقد تكيّف كل كائن هنا للنجاة في دورات الطبيعة البرية القاسية."),
                YouTubeDubSegment(4, 14.0f, 18.0f, "A delicate ecological balance that has endured for millions of years.", "توازن بيئي دقيق صمد وازدهر لملايين السنين.")
            )
            "gaming" -> listOf(
                YouTubeDubSegment(1, 0.4f, 3.6f, "Look out behind the portal! The final boss is charging up an ultimate attack!", "انتبه خلف البوابة! الزعيم الأخير يشحن ضربته القاضية الآن!"),
                YouTubeDubSegment(2, 4.0f, 7.5f, "Quick, dodge to the right and activate the energy shield immediately!", "بسرعة، تفادَ نحو اليمين وفعّل درع الطاقة فوراً!"),
                YouTubeDubSegment(3, 8.0f, 11.2f, "Unbelievable counter-attack! We finally secured the victory!", "هجوم مضاد أسطوري! لقد حسمنا النصر أخيراً!")
            )
            else -> listOf(
                YouTubeDubSegment(1, 0.5f, 4.0f, "Hello and welcome to this video tutorial.", "مرحباً بكم في هذا الشرح المصور والمبسط."),
                YouTubeDubSegment(2, 4.5f, 8.5f, "In this guide, we will learn how automated dubbing works with AI.", "في هذا الدليل، سنتعلم كيف تعمل الدبلجة الآلية بالذكاء الاصطناعي."),
                YouTubeDubSegment(3, 9.0f, 13.0f, "We will synchronize subtitles, translate them, and generate new speech.", "سنقوم بمزامنة الترجمات وترجمتها وتوليد أصوات جديدة ومطابقة.")
            )
        }
    }
}
