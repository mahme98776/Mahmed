package com.example.update.web

import com.example.update.model.AppRelease
import com.example.update.model.ReleaseChannel

object WebPortalHtmlTemplates {

    const val PLATFORM_DOMAIN = "mody.org"
    const val PLATFORM_NAME = "موقع مودى للمطور والمستخدم | MODY.ORG"

    fun generateUserPortalHtml(
        latestRelease: AppRelease,
        allReleases: List<AppRelease>,
        serverUrl: String
    ): String {
        val releasesListHtml = StringBuilder()
        for (rel in allReleases) {
            val badgeColor = when (rel.channel) {
                ReleaseChannel.STABLE -> "#10b981"
                ReleaseChannel.BETA -> "#f59e0b"
                ReleaseChannel.NIGHTLY -> "#8b5cf6"
            }
            val criticalBadge = if (rel.isCritical) """<span class="badge badge-critical">تحديث إجباري ⚠️</span>""" else ""
            val apkName = if (rel.apkFileName.isNotBlank()) rel.apkFileName else "mody-dubbing-v${rel.versionName}.apk"
            
            releasesListHtml.append("""
                <div class="version-card">
                    <div class="version-header">
                        <div class="version-title-group">
                            <span class="version-badge-tag">v${rel.versionName}</span>
                            <span class="version-code">بناء #${rel.versionCode}</span>
                            <span class="badge" style="background:${badgeColor}20; color:${badgeColor}; border:1px solid ${badgeColor}60;">${rel.channel.titleAr}</span>
                            $criticalBadge
                        </div>
                        <span class="version-date">📅 ${rel.releaseDate}</span>
                    </div>
                    
                    <h3 class="release-title">${rel.releaseTitle}</h3>
                    
                    <div class="changelog-box">
                        <div class="changelog-header">✨ مميزات وسجل التحديث:</div>
                        <div class="changelog-content">${rel.releaseNotesArabic.replace("\n", "<br>")}</div>
                    </div>
                    
                    <div class="version-footer">
                        <div class="file-specs">
                            <span>📦 <strong>الحجم:</strong> ${rel.apkSizeMb} MB</span>
                            <span>📂 <strong>الملف:</strong> $apkName</span>
                            <span>📥 <strong>التنزيلات:</strong> ${rel.downloadCount}</span>
                        </div>
                        <a href="/download/${rel.id}" class="btn-download-sm" download="$apkName">
                            <span>تحميل ملف APK</span>
                            <span>⬇️</span>
                        </a>
                    </div>
                </div>
            """.trimIndent())
        }

        val latestApkName = if (latestRelease.apkFileName.isNotBlank()) latestRelease.apkFileName else "mody-dubbing-v${latestRelease.versionName}.apk"

        return """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>mody.org - مركز إصدارات وتحديثات التطبيق</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Tajawal:wght@400;500;700;800;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --domain-brand: #6366f1;
            --domain-accent: #06b6d4;
            --success-color: #10b981;
            --bg-dark: #090d16;
            --bg-card: #131b2e;
            --bg-card-hover: #1a253f;
            --border-color: #22304e;
            --border-glow: rgba(99, 102, 241, 0.35);
            --text-white: #ffffff;
            --text-dim: #94a3b8;
            --text-bright: #e2e8f0;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Tajawal', sans-serif; }
        body { background-color: var(--bg-dark); color: var(--text-white); min-height: 100vh; padding-bottom: 60px; line-height: 1.6; }
        
        /* Domain Top Bar */
        .domain-bar {
            background: linear-gradient(90deg, #1e1b4b, #0f172a);
            border-bottom: 1px solid var(--border-color);
            padding: 8px 24px;
            font-size: 0.85rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: var(--text-dim);
        }
        .domain-badge {
            background: rgba(99, 102, 241, 0.2);
            color: #818cf8;
            border: 1px solid #6366f1;
            padding: 2px 10px;
            border-radius: 20px;
            font-weight: 800;
            letter-spacing: 1px;
            text-transform: uppercase;
        }

        /* Navbar */
        .navbar {
            background: rgba(19, 27, 46, 0.9);
            backdrop-filter: blur(14px);
            border-bottom: 1px solid var(--border-color);
            padding: 16px 28px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: sticky;
            top: 0;
            z-index: 100;
        }
        .nav-brand {
            display: flex;
            align-items: center;
            gap: 12px;
            text-decoration: none;
            color: #fff;
        }
        .logo-box {
            width: 44px;
            height: 44px;
            background: linear-gradient(135deg, var(--domain-brand), var(--domain-accent));
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4rem;
            box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
        }
        .brand-text h1 { font-size: 1.25rem; font-weight: 900; line-height: 1.2; }
        .brand-text span { font-size: 0.8rem; color: var(--domain-accent); font-weight: 700; letter-spacing: 0.5px; }
        
        .nav-actions { display: flex; gap: 12px; align-items: center; }
        .btn-dev-portal {
            background: linear-gradient(135deg, #4338ca, #3730a3);
            color: #e0e7ff;
            border: 1px solid #6366f1;
            padding: 9px 18px;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 700;
            font-size: 0.9rem;
            display: flex;
            align-items: center;
            gap: 6px;
            transition: all 0.25s;
        }
        .btn-dev-portal:hover {
            background: #4f46e5;
            color: #fff;
            box-shadow: 0 4px 16px rgba(99, 102, 241, 0.5);
            transform: translateY(-2px);
        }

        .container { max-width: 960px; margin: 0 auto; padding: 30px 20px; }

        /* Hero Banner */
        .hero {
            background: radial-gradient(circle at top right, #201e52 0%, #111827 100%);
            border: 1px solid #374151;
            border-radius: 28px;
            padding: 44px 32px;
            text-align: center;
            box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6), 0 0 40px var(--border-glow);
            margin-bottom: 40px;
            position: relative;
            overflow: hidden;
        }
        .hero::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(99,102,241,0.08) 0%, transparent 60%);
            pointer-events: none;
        }
        .live-tag {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: rgba(16, 185, 129, 0.15);
            color: var(--success-color);
            border: 1px solid var(--success-color);
            padding: 6px 16px;
            border-radius: 30px;
            font-size: 0.85rem;
            font-weight: 800;
            margin-bottom: 18px;
        }
        .live-tag .dot {
            width: 8px;
            height: 8px;
            background: var(--success-color);
            border-radius: 50%;
            animation: pulse 1.5s infinite;
        }
        @keyframes pulse { 0% { opacity: 0.4; } 50% { opacity: 1; } 100% { opacity: 0.4; } }

        .hero h2 { font-size: 2.4rem; font-weight: 900; margin-bottom: 14px; color: #fff; }
        .hero p { color: var(--text-dim); font-size: 1.1rem; max-width: 680px; margin: 0 auto 28px; }

        /* Main Download Button */
        .download-action-box {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 14px;
            margin-bottom: 24px;
        }
        .main-download-btn {
            background: linear-gradient(90deg, #6366f1, #4f46e5 50%, #06b6d4);
            background-size: 200% auto;
            color: #fff;
            padding: 18px 44px;
            border-radius: 20px;
            font-size: 1.25rem;
            font-weight: 900;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 14px;
            box-shadow: 0 10px 30px rgba(99, 102, 241, 0.5);
            transition: all 0.3s ease;
            border: 1px solid rgba(255, 255, 255, 0.2);
        }
        .main-download-btn:hover {
            background-position: right center;
            transform: translateY(-3px) scale(1.02);
            box-shadow: 0 14px 40px rgba(99, 102, 241, 0.7);
        }
        .apk-filename-hint {
            font-family: monospace;
            font-size: 0.9rem;
            color: var(--domain-accent);
            background: rgba(6, 182, 212, 0.1);
            padding: 4px 14px;
            border-radius: 8px;
            border: 1px dashed rgba(6, 182, 212, 0.3);
        }

        .meta-strip {
            display: flex;
            justify-content: center;
            gap: 24px;
            color: var(--text-dim);
            font-size: 0.9rem;
            flex-wrap: wrap;
            padding-top: 18px;
            border-top: 1px solid rgba(255, 255, 255, 0.08);
        }
        .meta-strip span { display: flex; align-items: center; gap: 6px; }

        /* Highlights */
        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 20px;
            margin-bottom: 40px;
        }
        .feature-card {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: 20px;
            padding: 24px;
            transition: transform 0.2s, border-color 0.2s;
        }
        .feature-card:hover { transform: translateY(-4px); border-color: var(--domain-brand); }
        .feature-icon {
            font-size: 2rem;
            width: 48px;
            height: 48px;
            background: rgba(99, 102, 241, 0.15);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 16px;
        }
        .feature-card h3 { font-size: 1.15rem; font-weight: 800; margin-bottom: 8px; color: #fff; }
        .feature-card p { font-size: 0.92rem; color: var(--text-dim); }

        /* Versions History */
        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 22px;
        }
        .section-header h2 { font-size: 1.5rem; font-weight: 900; }

        .version-card {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: 22px;
            padding: 26px;
            margin-bottom: 20px;
            transition: all 0.2s;
        }
        .version-card:hover { border-color: var(--domain-brand); background: var(--bg-card-hover); }
        .version-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            flex-wrap: wrap;
            gap: 10px;
        }
        .version-title-group { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
        .version-badge-tag {
            font-size: 1.25rem;
            font-weight: 900;
            color: #fff;
            background: #1e293b;
            padding: 4px 12px;
            border-radius: 10px;
            border: 1px solid #334155;
        }
        .version-code { font-size: 0.85rem; color: var(--text-dim); }
        .version-date { font-size: 0.85rem; color: var(--text-dim); }
        .release-title { font-size: 1.2rem; font-weight: 800; color: #e2e8f0; margin-bottom: 12px; }

        .badge {
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 0.75rem;
            font-weight: 700;
            display: inline-block;
        }
        .badge-critical { background: rgba(239, 68, 68, 0.2); color: #ef4444; border: 1px solid #ef4444; }

        .changelog-box {
            background: #0b1120;
            border: 1px solid #1e293b;
            border-radius: 14px;
            padding: 16px;
            margin-bottom: 18px;
        }
        .changelog-header { font-size: 0.85rem; font-weight: 800; color: var(--domain-accent); margin-bottom: 8px; }
        .changelog-content { font-size: 0.95rem; color: #cbd5e1; line-height: 1.7; }

        .version-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-top: 1px solid #1e293b;
            padding-top: 16px;
            flex-wrap: wrap;
            gap: 12px;
        }
        .file-specs { display: flex; gap: 16px; font-size: 0.85rem; color: var(--text-dim); flex-wrap: wrap; }
        .file-specs strong { color: #fff; }

        .btn-download-sm {
            background: #3730a3;
            color: #fff;
            padding: 9px 20px;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 800;
            font-size: 0.9rem;
            border: 1px solid #6366f1;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: all 0.2s;
        }
        .btn-download-sm:hover { background: #4f46e5; transform: translateY(-2px); }

        /* Installation Guide */
        .install-guide {
            background: #0d1527;
            border: 1px solid #1e3a8a;
            border-radius: 22px;
            padding: 28px;
            margin-top: 36px;
        }
        .install-guide h3 { font-size: 1.25rem; font-weight: 900; color: #60a5fa; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
        .steps-list { list-style: none; counter-reset: step-counter; }
        .steps-list li {
            position: relative;
            padding-right: 38px;
            margin-bottom: 14px;
            color: #cbd5e1;
            font-size: 0.95rem;
        }
        .steps-list li::before {
            counter-increment: step-counter;
            content: counter(step-counter);
            position: absolute;
            right: 0;
            top: 0;
            width: 26px;
            height: 26px;
            background: #1e3a8a;
            color: #93c5fd;
            border-radius: 50%;
            text-align: center;
            line-height: 26px;
            font-weight: 900;
            font-size: 0.85rem;
        }

        /* Footer */
        .footer {
            margin-top: 50px;
            text-align: center;
            color: var(--text-dim);
            font-size: 0.85rem;
            border-top: 1px solid var(--border-color);
            padding-top: 24px;
        }
    </style>
</head>
<body>

    <!-- Domain Sub-Bar -->
    <div class="domain-bar">
        <div style="display: flex; align-items: center; gap: 8px;">
            <span>🌐 النطاق الرسمي:</span>
            <span class="domain-badge">$PLATFORM_DOMAIN</span>
        </div>
        <div>
            <span>🚀 منصة توزيع وتحميل التحديثات المباشرة</span>
        </div>
    </div>

    <!-- Main Navigation Bar -->
    <nav class="navbar">
        <a href="/" class="nav-brand">
            <div class="logo-box">🎙️</div>
            <div class="brand-text">
                <h1>استوديو دبلجة المقاطع العربي</h1>
                <span>$PLATFORM_DOMAIN • بوابة المستخدم</span>
            </div>
        </a>
        <div class="nav-actions">
            <a href="#changelogs" class="btn-dev-portal" style="background: transparent; border-color: #334155;">📜 سجل الإصدارات</a>
            <a href="/developer" class="btn-dev-portal">
                <span>👨‍💻 بوابة المطور (HTML Admin)</span>
            </a>
        </div>
    </nav>

    <div class="container">
        
        <!-- Hero Section -->
        <div class="hero">
            <div class="live-tag">
                <div class="dot"></div>
                <span>أحدث إصدار رسمي جاهز للتحميل: v${latestRelease.versionName}</span>
            </div>
            
            <h2>${latestRelease.releaseTitle}</h2>
            <p>احصل على أحدث نسخة من تطبيق الدبلجة لتستمتع بكافة الميزات الاحترافية الجديدة، دبلجة الذكاء الاصطناعي، ومزامنة واستخراج الفيديو بدقة فائقة.</p>
            
            <div class="download-action-box">
                <a href="/download/${latestRelease.id}" class="main-download-btn" id="heroDownloadBtn" download="$latestApkName">
                    <span>تحميل أحدث إصدار (APK)</span>
                    <span style="font-size: 1.4rem;">⬇️</span>
                </a>
                <span class="apk-filename-hint">📁 ملف التثبيت: $latestApkName</span>
            </div>
            
            <div class="meta-strip">
                <span>📅 <strong>تاريخ الإصدار:</strong> ${latestRelease.releaseDate}</span>
                <span>📦 <strong>الحجم:</strong> ${latestRelease.apkSizeMb} MB</span>
                <span>📱 <strong>النظام:</strong> ${latestRelease.minAndroidVersion}</span>
                <span>📥 <strong>التحميلات:</strong> ${latestRelease.downloadCount}</span>
            </div>
        </div>

        <!-- Highlights Grid -->
        <div class="features-grid">
            <div class="feature-card">
                <div class="feature-icon">🎬</div>
                <h3>تصدير ودبلجة متزامنة</h3>
                <p>دمج التسجيل الصوتي مع الفيديو الأصلي وتصدير ملفات MP4 نقية بضغطة زر واحدة.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">🤖</div>
                <h3>دبلجة بالذكاء الاصطناعي</h3>
                <p>توليد سيناريوهات ذكية، ترجمة اللهجات، وأصوات واقعية تناسب الشخصيات الكرتونية والسينمائية.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">⚡</div>
                <h3>تحميل وتحديث فوري</h3>
                <p>تثبيت ملفات الـ APK مباشرة من منصة $PLATFORM_DOMAIN دون وسيط وبأعلى سرعة ممكنة.</p>
            </div>
        </div>

        <!-- Changelog Section -->
        <div id="changelogs" class="section-header">
            <h2>📜 سجل الإصدارات وتفاصيل التحديثات</h2>
            <span style="color: var(--text-dim); font-size: 0.9rem;">إجمالي الإصدارات: ${allReleases.size}</span>
        </div>

        $releasesListHtml

        <!-- Instructions Guide -->
        <div class="install-guide">
            <h3>📖 دليل تثبيت ملف APK على جهاز أندرويد</h3>
            <ol class="steps-list">
                <li>اضغط على زر <strong>تحميل أحدث إصدار (APK)</strong> لحفظ ملف التطبيق على هاتفك.</li>
                <li>بعد اكتمال التنزيل، انقر على الإشعار أو توجه إلى مدير الملفات &gt; التنزيلات (Downloads).</li>
                <li>انقر على ملف <strong>$latestApkName</strong> لتثبيته. إذا طلب النظام الإذن، فعل "السماح بالتثبيت من هذا المصدر".</li>
                <li>اضغط <strong>تثبيت (Install)</strong> أو <strong>تحديث (Update)</strong> وستفتح النسخة الجديدة مع الحفاظ على كافة مشاريعك!</li>
            </ol>
        </div>

        <!-- Footer -->
        <div class="footer">
            <p>منصة $PLATFORM_DOMAIN • خادم تحديثات وتوزيع حزم استوديو الدبلجة العربي © 2026</p>
        </div>

    </div>

</body>
</html>
        """.trimIndent()
    }

    fun generateDeveloperPortalHtml(
        releases: List<AppRelease>,
        serverUrl: String,
        statusMessage: String? = null
    ): String {
        val releasesTableHtml = StringBuilder()
        for (rel in releases) {
            val badgeColor = when (rel.channel) {
                ReleaseChannel.STABLE -> "#10b981"
                ReleaseChannel.BETA -> "#f59e0b"
                ReleaseChannel.NIGHTLY -> "#8b5cf6"
            }
            val apkName = if (rel.apkFileName.isNotBlank()) rel.apkFileName else "mody-dubbing-v${rel.versionName}.apk"
            
            releasesTableHtml.append("""
                <tr>
                    <td>
                        <div style="font-weight: 800; font-size: 1.05rem; color: #fff;">v${rel.versionName}</div>
                        <small style="color:#94a3b8">كود #${rel.versionCode}</small>
                    </td>
                    <td>
                        <div style="font-weight: 700; color: #e2e8f0;">${rel.releaseTitle}</div>
                        <small style="color: #64748b; font-family: monospace;">$apkName</small>
                    </td>
                    <td>
                        <span class="badge" style="background:${badgeColor}20; color:${badgeColor}; border:1px solid ${badgeColor}60;">${rel.channel.titleAr}</span>
                    </td>
                    <td style="color: #94a3b8; font-size: 0.85rem;">${rel.releaseDate}</td>
                    <td><span style="color: #38bdf8; font-weight: 700;">${rel.apkSizeMb} MB</span></td>
                    <td><span style="color: #10b981; font-weight: 800;">${rel.downloadCount}</span></td>
                    <td>
                        <div style="display: flex; gap: 6px;">
                            <a href="/download/${rel.id}" class="btn-action-dl" title="تحميل APK">⬇️</a>
                            <form method="POST" action="/api/delete-release" style="display:inline;" onsubmit="return confirm('هل أنت متأكد من حذف هذا الإصدار (v${rel.versionName})؟');">
                                <input type="hidden" name="releaseId" value="${rel.id}">
                                <button type="submit" class="btn-action-delete">حذف</button>
                            </form>
                        </div>
                    </td>
                </tr>
            """.trimIndent())
        }

        val totalDownloads = releases.sumOf { it.downloadCount }
        val latestRelease = releases.maxByOrNull { it.versionCode }
        val nextVersionCode = (latestRelease?.versionCode ?: 1) + 1

        val alertHtml = if (!statusMessage.isNullOrBlank()) {
            """<div class="alert-success">✅ $statusMessage</div>"""
        } else ""

        return """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>mody.org - لوحة تحكم المطور وإدارة حزم APK</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Tajawal:wght@400;500;700;800;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #6366f1;
            --primary-hover: #4f46e5;
            --accent: #06b6d4;
            --success: #10b981;
            --danger: #ef4444;
            --bg: #070b14;
            --panel: #11192e;
            --panel-card: #18233f;
            --panel-border: #233152;
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Tajawal', sans-serif; }
        body { background-color: var(--bg); color: var(--text-main); min-height: 100vh; padding-bottom: 60px; }

        /* Top Header */
        .top-nav {
            background: var(--panel);
            border-bottom: 1px solid var(--panel-border);
            padding: 16px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: sticky;
            top: 0;
            z-index: 100;
        }
        .brand-dev {
            display: flex;
            align-items: center;
            gap: 12px;
            text-decoration: none;
            color: #fff;
        }
        .dev-badge {
            background: linear-gradient(135deg, #4338ca, #312e81);
            color: #c7d2fe;
            border: 1px solid #6366f1;
            padding: 4px 12px;
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: 800;
        }
        .header-actions { display: flex; gap: 12px; align-items: center; }
        .btn-view-user-portal {
            background: #1e293b;
            color: #38bdf8;
            padding: 9px 18px;
            border-radius: 12px;
            text-decoration: none;
            font-size: 0.9rem;
            font-weight: 700;
            border: 1px solid #334155;
            display: flex;
            align-items: center;
            gap: 6px;
            transition: all 0.2s;
        }
        .btn-view-user-portal:hover { background: #38bdf8; color: #070b14; }

        .container { max-width: 1200px; margin: 0 auto; padding: 30px 20px; }

        /* Stats Cards */
        .stats-row {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 18px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: var(--panel);
            border: 1px solid var(--panel-border);
            border-radius: 18px;
            padding: 22px;
            position: relative;
            overflow: hidden;
        }
        .stat-card::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 3px;
            background: var(--primary);
        }
        .stat-card.c-blue::after { background: #38bdf8; }
        .stat-card.c-green::after { background: #10b981; }
        .stat-card.c-purple::after { background: #a855f7; }

        .stat-label { font-size: 0.85rem; color: var(--text-muted); font-weight: 600; margin-bottom: 6px; }
        .stat-value { font-size: 2.2rem; font-weight: 900; color: #fff; }

        /* Main Grid: Upload/Publish Form + Releases Table */
        .main-grid {
            display: grid;
            grid-template-columns: 1fr 1.35fr;
            gap: 24px;
        }
        @media (max-width: 992px) {
            .main-grid { grid-template-columns: 1fr; }
        }

        .panel-box {
            background: var(--panel);
            border: 1px solid var(--panel-border);
            border-radius: 22px;
            padding: 28px;
        }
        .panel-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 22px;
            padding-bottom: 14px;
            border-bottom: 1px solid var(--panel-border);
        }
        .panel-header h2 { font-size: 1.25rem; font-weight: 900; color: #fff; display: flex; align-items: center; gap: 8px; }

        /* Form Inputs */
        .form-group { margin-bottom: 18px; }
        .form-row { display: flex; gap: 14px; }
        .form-row .form-group { flex: 1; }
        label { display: block; font-size: 0.88rem; font-weight: 700; color: #cbd5e1; margin-bottom: 6px; }
        .input-hint { font-size: 0.75rem; color: #64748b; margin-top: 4px; }

        input, select, textarea {
            width: 100%;
            background: #090e1a;
            border: 1.5px solid #293859;
            border-radius: 12px;
            padding: 12px 16px;
            color: #fff;
            font-size: 0.95rem;
            outline: none;
            transition: all 0.2s;
        }
        input:focus, select:focus, textarea:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
        }
        textarea { resize: vertical; min-height: 100px; line-height: 1.5; }

        /* File Upload Box */
        .file-upload-area {
            border: 2px dashed #3b82f6;
            background: rgba(59, 130, 246, 0.05);
            border-radius: 14px;
            padding: 20px;
            text-align: center;
            cursor: pointer;
            transition: all 0.2s;
            margin-bottom: 18px;
        }
        .file-upload-area:hover {
            background: rgba(59, 130, 246, 0.12);
            border-color: #60a5fa;
        }
        .file-upload-area input[type="file"] {
            display: none;
        }
        .upload-icon { font-size: 2rem; margin-bottom: 8px; }
        .upload-text { font-weight: 700; color: #93c5fd; font-size: 0.95rem; }
        .selected-file-name {
            margin-top: 8px;
            font-family: monospace;
            font-size: 0.85rem;
            color: #34d399;
            font-weight: bold;
        }

        .checkbox-label {
            display: flex;
            align-items: center;
            gap: 10px;
            cursor: pointer;
            background: #090e1a;
            padding: 12px 16px;
            border-radius: 12px;
            border: 1px solid #293859;
        }
        .checkbox-label input { width: auto; }

        .btn-publish {
            width: 100%;
            background: linear-gradient(90deg, #6366f1, #4f46e5);
            color: #fff;
            border: none;
            border-radius: 14px;
            padding: 16px;
            font-size: 1.1rem;
            font-weight: 900;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 10px;
            box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
            margin-top: 10px;
        }
        .btn-publish:hover {
            transform: translateY(-2px);
            box-shadow: 0 12px 30px rgba(99, 102, 241, 0.6);
        }

        /* Table */
        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; text-align: right; }
        th { background: #090e1a; padding: 14px 16px; color: var(--text-muted); font-size: 0.85rem; font-weight: 800; border-bottom: 1px solid var(--panel-border); }
        td { padding: 16px; border-bottom: 1px solid #1a233a; font-size: 0.92rem; vertical-align: middle; }

        .btn-action-delete {
            background: rgba(239, 68, 68, 0.15);
            color: #ef4444;
            border: 1px solid #ef4444;
            padding: 6px 14px;
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;
        }
        .btn-action-delete:hover { background: #ef4444; color: #fff; }

        .btn-action-dl {
            background: #1e293b;
            color: #38bdf8;
            border: 1px solid #334155;
            padding: 6px 10px;
            border-radius: 8px;
            text-decoration: none;
            font-size: 0.85rem;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }
        .btn-action-dl:hover { background: #38bdf8; color: #000; }

        .badge { padding: 4px 10px; border-radius: 8px; font-size: 0.75rem; font-weight: 700; display: inline-block; }

        .alert-success {
            background: rgba(16, 185, 129, 0.15);
            border: 1px solid #10b981;
            color: #10b981;
            padding: 14px 20px;
            border-radius: 14px;
            margin-bottom: 24px;
            font-weight: 700;
        }
    </style>
</head>
<body>

    <!-- Header -->
    <header class="top-nav">
        <div class="brand-dev">
            <span style="font-size: 1.5rem;">👨‍💻</span>
            <div>
                <div style="font-weight: 900; font-size: 1.15rem; color: #fff;">بوابة المطور - لوحة رفع ونشر التحديثات</div>
                <div style="font-size: 0.75rem; color: #94a3b8;">إدارة حزم APK وتوزيع الإصدارات عبر النطاق <strong style="color:#818cf8;">$PLATFORM_DOMAIN</strong></div>
            </div>
            <span class="dev-badge">HTML Admin Portal</span>
        </div>
        <div class="header-actions">
            <a href="/" class="btn-view-user-portal" target="_blank">
                <span>🌐 زيارة صفحة المستخدم ($PLATFORM_DOMAIN)</span>
            </a>
        </div>
    </header>

    <div class="container">
        
        $alertHtml

        <!-- Stats Overview -->
        <div class="stats-row">
            <div class="stat-card c-blue">
                <div class="stat-label">إجمالي التنزيلات للمستخدمين</div>
                <div class="stat-value" style="color: #38bdf8;">$totalDownloads 📥</div>
            </div>
            <div class="stat-card c-green">
                <div class="stat-label">أحدث إصدار منشور في $PLATFORM_DOMAIN</div>
                <div class="stat-value" style="color: #10b981;">v${latestRelease?.versionName ?: "1.0.0"} 🚀</div>
            </div>
            <div class="stat-card c-purple">
                <div class="stat-label">عدد الإصدارات المتوفرة</div>
                <div class="stat-value" style="color: #c084fc;">${releases.size} 📦</div>
            </div>
        </div>

        <div class="main-grid">
            
            <!-- Publish / Upload APK Form -->
            <div class="panel-box">
                <div class="panel-header">
                    <h2>
                        <span>🚀</span>
                        <span>إضافة ونشر إصدار جديد (Upload APK)</span>
                    </h2>
                </div>
                
                <form method="POST" action="/api/publish-update" id="publishForm">
                    
                    <!-- APK File Selector from Developer's Device -->
                    <label>اختيار وتحميل ملف الـ APK من جهاز المطور:</label>
                    <div class="file-upload-area" onclick="document.getElementById('apkFileInput').click();">
                        <div class="upload-icon">📦</div>
                        <div class="upload-text">اضغط هنا لاختيار ملف الـ APK من جهازك</div>
                        <div style="font-size: 0.75rem; color: #94a3b8; margin-top: 4px;">يقوم النظام تلقائياً بتحديد اسم الملف وحجم الحزمة</div>
                        <input type="file" id="apkFileInput" accept=".apk" onchange="handleFileSelect(this)">
                        <div id="selectedFileInfo" class="selected-file-name" style="display:none;"></div>
                    </div>
                    <input type="hidden" name="apkFileName" id="apkFileNameHidden" value="mody-dubbing-v1.3.0.apk">

                    <div class="form-row">
                        <div class="form-group">
                            <label>رقم الإصدار (Version Name)</label>
                            <input type="text" name="versionName" id="versionNameInput" value="1.$nextVersionCode.0" placeholder="مثال: 1.3.0" required>
                            <div class="input-hint">يظهر للمستخدم كاسم الإصدار الجديد</div>
                        </div>
                        <div class="form-group">
                            <label>كود البناء (Version Code)</label>
                            <input type="number" name="versionCode" id="versionCodeInput" value="$nextVersionCode" required>
                            <div class="input-hint">رقم تسلسلي لتحديد أولوية التحديث</div>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label>عنوان الإصدار أو التحديث</label>
                        <input type="text" name="releaseTitle" id="releaseTitleInput" placeholder="مثال: تحديث الذكاء الاصطناعي وتصدير الفيديو 4K" required>
                    </div>
                    
                    <div class="form-group">
                        <label>سجل التغييرات ومميزات التحديث (Changelog)</label>
                        <textarea name="releaseNotesArabic" id="releaseNotesInput" placeholder="• إضافة ميزة تصدير الفيديو المدمج مع الصوت بدقة فائقة&#10;• تسريع معالجة الذكاء الاصطناعي وتوليد الصوت&#10;• تحسين استقرار المزامنة والتسجيل" required></textarea>
                    </div>
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label>قناة الإصدار</label>
                            <select name="channel">
                                <option value="STABLE">مستقرة (Stable) - موصى به للجميع</option>
                                <option value="BETA">تجريبية (Beta)</option>
                                <option value="NIGHTLY">تطويرية (Nightly)</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>حجم الحزمة التقريبي (MB)</label>
                            <input type="number" step="0.1" name="apkSizeMb" id="apkSizeInput" value="18.5" required>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label>مسار التحميل المباشر</label>
                        <input type="text" name="downloadUrl" id="downloadUrlInput" value="/download/latest.apk" required>
                    </div>
                    
                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" id="isCritical" name="isCritical" value="true">
                            <span>تحديث إجباري ملزم (يطلب من المستخدمين الترقية فوراً) ⚠️</span>
                        </label>
                    </div>
                    
                    <button type="submit" class="btn-publish">
                        <span>نشر الإصدار وتوفيره للتحميل على mody.org</span>
                        <span>📡</span>
                    </button>
                </form>
            </div>

            <!-- Published Releases Archive -->
            <div class="panel-box">
                <div class="panel-header">
                    <h2>
                        <span>📋</span>
                        <span>سجل الإصدارات المتاحة للتحميل</span>
                    </h2>
                </div>
                
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>الإصدار</th>
                                <th>العنوان والملف</th>
                                <th>القناة</th>
                                <th>التاريخ</th>
                                <th>الحجم</th>
                                <th>التنزيلات</th>
                                <th>الإجراءات</th>
                            </tr>
                        </thead>
                        <tbody>
                            $releasesTableHtml
                        </tbody>
                    </table>
                </div>
            </div>

        </div>

    </div>

    <script>
        function handleFileSelect(input) {
            if (input.files && input.files[0]) {
                const file = input.files[0];
                const fileName = file.name;
                const sizeMb = (file.size / (1024 * 1024)).toFixed(1);
                
                const fileInfoDiv = document.getElementById('selectedFileInfo');
                fileInfoDiv.style.display = 'block';
                fileInfoDiv.innerHTML = '✅ تم اختيار الحزمة: <strong>' + fileName + '</strong> (' + sizeMb + ' MB)';
                
                document.getElementById('apkFileNameHidden').value = fileName;
                if (sizeMb > 0) {
                    document.getElementById('apkSizeInput').value = sizeMb;
                }
                
                // Smart guess version from filename if present (e.g. app-v1.4.0.apk)
                const match = fileName.match(/(\d+\.\d+(\.\d+)?)/);
                if (match) {
                    document.getElementById('versionNameInput').value = match[0];
                }
            }
        }
    </script>
</body>
</html>
        """.trimIndent()
    }
}
