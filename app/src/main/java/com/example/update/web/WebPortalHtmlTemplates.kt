package com.example.update.web

import com.example.update.model.AppRelease
import com.example.update.model.ReleaseChannel

object WebPortalHtmlTemplates {

    const val PLATFORM_DOMAIN = "voicemaster.org"
    const val PLATFORM_NAME = "فويس ماستر برو | VoiceMaster Pro"
    const val COPYRIGHT_HOLDER = "محمد رضا محمود محمود سليمه"

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
            val apkName = if (rel.apkFileName.isNotBlank()) rel.apkFileName else "voicemaster-pro-v${rel.versionName}.apk"
            
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
                            <span>تحميل حزمة APK</span>
                            <span>⬇️</span>
                        </a>
                    </div>
                </div>
            """.trimIndent())
        }

        val latestApkName = if (latestRelease.apkFileName.isNotBlank()) latestRelease.apkFileName else "voicemaster-pro-v${latestRelease.versionName}.apk"

        return """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VoiceMaster Pro | فويس ماستر برو - المركز الرسمي للتحديثات</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Tajawal:wght@400;500;700;800;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --brand-primary: #6366f1;
            --brand-gradient: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%);
            --brand-accent: #38bdf8;
            --success-color: #10b981;
            --bg-dark: #070913;
            --bg-card: #0f172a;
            --bg-card-hover: #1e293b;
            --border-color: #1e293b;
            --border-glow: rgba(99, 102, 241, 0.4);
            --text-white: #ffffff;
            --text-dim: #94a3b8;
            --gold-accent: #f59e0b;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Tajawal', sans-serif; }
        body { background-color: var(--bg-dark); color: var(--text-white); min-height: 100vh; padding-bottom: 60px; line-height: 1.6; }
        
        /* Official Domain Top Bar */
        .domain-bar {
            background: linear-gradient(90deg, #0f172a, #1e1b4b, #0f172a);
            border-bottom: 1px solid var(--border-color);
            padding: 9px 24px;
            font-size: 0.85rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: var(--text-dim);
            flex-wrap: wrap;
            gap: 8px;
        }
        .domain-badge {
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.25), rgba(217, 70, 239, 0.25));
            color: #c084fc;
            border: 1px solid #a855f7;
            padding: 3px 12px;
            border-radius: 20px;
            font-weight: 900;
            letter-spacing: 1px;
            text-transform: uppercase;
        }

        /* Navbar */
        .navbar {
            background: rgba(15, 23, 42, 0.92);
            backdrop-filter: blur(16px);
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
            gap: 14px;
            text-decoration: none;
            color: #fff;
        }
        .logo-box {
            width: 48px;
            height: 48px;
            background: var(--brand-gradient);
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
            box-shadow: 0 4px 18px rgba(139, 92, 246, 0.45);
        }
        .brand-text h1 { font-size: 1.35rem; font-weight: 900; line-height: 1.2; letter-spacing: -0.5px; }
        .brand-text span { font-size: 0.8rem; color: var(--brand-accent); font-weight: 800; }
        
        .nav-actions { display: flex; gap: 12px; align-items: center; }
        .btn-nav {
            background: rgba(99, 102, 241, 0.15);
            color: #e0e7ff;
            border: 1px solid rgba(99, 102, 241, 0.4);
            padding: 9px 18px;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 800;
            font-size: 0.9rem;
            display: flex;
            align-items: center;
            gap: 6px;
            transition: all 0.25s;
        }
        .btn-nav:hover {
            background: var(--brand-primary);
            color: #fff;
            box-shadow: 0 4px 16px rgba(99, 102, 241, 0.5);
            transform: translateY(-2px);
        }

        .container { max-width: 980px; margin: 0 auto; padding: 32px 20px; }

        /* Hero Banner */
        .hero {
            background: radial-gradient(circle at 80% 20%, #2e1065 0%, #0f172a 70%, #030712 100%);
            border: 1px solid rgba(139, 92, 246, 0.35);
            border-radius: 30px;
            padding: 48px 36px;
            text-align: center;
            box-shadow: 0 24px 60px rgba(0, 0, 0, 0.7), 0 0 50px var(--border-glow);
            margin-bottom: 40px;
            position: relative;
            overflow: hidden;
        }
        .live-tag {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: rgba(16, 185, 129, 0.15);
            color: var(--success-color);
            border: 1px solid var(--success-color);
            padding: 6px 18px;
            border-radius: 30px;
            font-size: 0.88rem;
            font-weight: 800;
            margin-bottom: 20px;
        }
        .live-tag .dot {
            width: 8px;
            height: 8px;
            background: var(--success-color);
            border-radius: 50%;
            animation: pulse 1.5s infinite;
        }
        @keyframes pulse { 0% { opacity: 0.4; } 50% { opacity: 1; } 100% { opacity: 0.4; } }

        .hero h2 { font-size: 2.6rem; font-weight: 900; margin-bottom: 14px; color: #fff; line-height: 1.25; }
        .hero p { color: var(--text-dim); font-size: 1.15rem; max-width: 720px; margin: 0 auto 30px; }

        /* Main Download Button */
        .download-action-box {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 14px;
            margin-bottom: 26px;
        }
        .main-download-btn {
            background: var(--brand-gradient);
            background-size: 200% auto;
            color: #fff;
            padding: 18px 48px;
            border-radius: 22px;
            font-size: 1.3rem;
            font-weight: 900;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 14px;
            box-shadow: 0 12px 35px rgba(139, 92, 246, 0.55);
            transition: all 0.3s ease;
            border: 1px solid rgba(255, 255, 255, 0.25);
        }
        .main-download-btn:hover {
            transform: translateY(-3px) scale(1.02);
            box-shadow: 0 16px 45px rgba(217, 70, 239, 0.7);
        }
        .apk-filename-hint {
            font-family: monospace;
            font-size: 0.92rem;
            color: var(--brand-accent);
            background: rgba(56, 189, 248, 0.1);
            padding: 5px 16px;
            border-radius: 10px;
            border: 1px dashed rgba(56, 189, 248, 0.35);
        }

        .meta-strip {
            display: flex;
            justify-content: center;
            gap: 28px;
            color: var(--text-dim);
            font-size: 0.92rem;
            flex-wrap: wrap;
            padding-top: 20px;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
        }
        .meta-strip span { display: flex; align-items: center; gap: 6px; }

        /* Intellectual Property Certificate Banner */
        .ip-certificate-card {
            background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(139, 92, 246, 0.15));
            border: 1px solid rgba(245, 158, 11, 0.4);
            border-radius: 22px;
            padding: 24px 28px;
            margin-bottom: 40px;
            display: flex;
            align-items: center;
            gap: 20px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
        }
        .ip-badge-icon {
            font-size: 2.5rem;
            min-width: 60px;
            height: 60px;
            background: rgba(245, 158, 11, 0.18);
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid rgba(245, 158, 11, 0.5);
        }
        .ip-info h4 { font-size: 1.15rem; color: #fde68a; font-weight: 900; margin-bottom: 6px; }
        .ip-info p { font-size: 0.95rem; color: #e2e8f0; line-height: 1.6; }
        .ip-author-highlight { color: #38bdf8; font-weight: 800; }

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
        .feature-card:hover { transform: translateY(-4px); border-color: var(--brand-primary); }
        .feature-icon {
            font-size: 2rem;
            width: 50px;
            height: 50px;
            background: rgba(99, 102, 241, 0.18);
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 16px;
        }
        .feature-card h3 { font-size: 1.2rem; font-weight: 800; margin-bottom: 8px; color: #fff; }
        .feature-card p { font-size: 0.94rem; color: var(--text-dim); }

        /* Versions History */
        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 22px;
        }
        .section-header h2 { font-size: 1.55rem; font-weight: 900; }

        .version-card {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: 22px;
            padding: 26px;
            margin-bottom: 20px;
            transition: all 0.2s;
        }
        .version-card:hover { border-color: var(--brand-primary); background: var(--bg-card-hover); }
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
            padding: 4px 14px;
            border-radius: 10px;
            border: 1px solid #334155;
        }
        .version-code { font-size: 0.85rem; color: var(--text-dim); }
        .version-date { font-size: 0.85rem; color: var(--text-dim); }
        .release-title { font-size: 1.25rem; font-weight: 800; color: #e2e8f0; margin-bottom: 12px; }

        .badge {
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 0.75rem;
            font-weight: 700;
            display: inline-block;
        }
        .badge-critical { background: rgba(239, 68, 68, 0.2); color: #ef4444; border: 1px solid #ef4444; }

        .changelog-box {
            background: #070d1e;
            border: 1px solid #1e293b;
            border-radius: 14px;
            padding: 16px;
            margin-bottom: 18px;
        }
        .changelog-header { font-size: 0.88rem; font-weight: 800; color: var(--brand-accent); margin-bottom: 8px; }
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
        .file-specs { display: flex; gap: 16px; font-size: 0.88rem; color: var(--text-dim); flex-wrap: wrap; }
        .file-specs strong { color: #fff; }

        .btn-download-sm {
            background: var(--brand-gradient);
            color: #fff;
            padding: 10px 22px;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 800;
            font-size: 0.92rem;
            border: 1px solid rgba(255,255,255,0.2);
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.2s;
        }
        .btn-download-sm:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(139, 92, 246, 0.5); }

        /* Installation Guide */
        .install-guide {
            background: #091224;
            border: 1px solid #1e3a8a;
            border-radius: 24px;
            padding: 30px;
            margin-top: 36px;
        }
        .install-guide h3 { font-size: 1.3rem; font-weight: 900; color: #60a5fa; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
        .steps-list { list-style: none; counter-reset: step-counter; }
        .steps-list li {
            position: relative;
            padding-right: 40px;
            margin-bottom: 16px;
            color: #cbd5e1;
            font-size: 0.96rem;
        }
        .steps-list li::before {
            counter-increment: step-counter;
            content: counter(step-counter);
            position: absolute;
            right: 0;
            top: 0;
            width: 28px;
            height: 28px;
            background: #1e3a8a;
            color: #93c5fd;
            border-radius: 50%;
            text-align: center;
            line-height: 28px;
            font-weight: 900;
            font-size: 0.88rem;
        }

        /* Legal Copyright Footer */
        .footer {
            margin-top: 50px;
            text-align: center;
            color: var(--text-dim);
            font-size: 0.88rem;
            border-top: 1px solid var(--border-color);
            padding-top: 30px;
        }
        .footer-copyright {
            font-weight: 800;
            color: #e2e8f0;
            margin-bottom: 6px;
            font-size: 0.95rem;
        }
        .footer-author {
            color: #38bdf8;
            font-weight: 800;
        }
        .footer-location {
            font-size: 0.84rem;
            color: #94a3b8;
            margin-top: 4px;
        }
    </style>
</head>
<body>

    <!-- Domain Sub-Bar -->
    <div class="domain-bar">
        <div style="display: flex; align-items: center; gap: 8px;">
            <span>🌐 النطاق الرسمي للتطبيق:</span>
            <span class="domain-badge">$PLATFORM_DOMAIN</span>
        </div>
        <div>
            <span>⚡ استوديو الدبلجة وهندسة الصوت بالذكاء الاصطناعي</span>
        </div>
    </div>

    <!-- Main Navigation Bar -->
    <nav class="navbar">
        <a href="/" class="nav-brand">
            <div class="logo-box">🎙️</div>
            <div class="brand-text">
                <h1>$PLATFORM_NAME</h1>
                <span>$PLATFORM_DOMAIN • المركز الرسمي المعتمد للتحديثات</span>
            </div>
        </a>
        <div class="nav-actions">
            <a href="#changelogs" class="btn-nav" style="background: transparent; border-color: #334155;">📜 سجل الإصدارات</a>
            <a href="#changelogs" class="btn-nav">
                <span>⚡ التحديثات الفورية</span>
            </a>
        </div>
    </nav>

    <div class="container">
        
        <!-- Hero Section -->
        <div class="hero">
            <div class="live-tag">
                <div class="dot"></div>
                <span>أحدث إصدار رسمي معتمد: v${latestRelease.versionName}</span>
            </div>
            
            <h2>${latestRelease.releaseTitle}</h2>
            <p>حمّل الآن الإصدار الأحدث من تطبيق <strong>فويس ماستر برو | VoiceMaster Pro</strong> واستمتع بأقوى ميزات الدبلجة وهندسة الصوت بالذكاء الاصطناعي مع التصدير المباشر بدقة استثنائية.</p>
            
            <div class="download-action-box">
                <a href="/download/${latestRelease.id}" class="main-download-btn" id="heroDownloadBtn" download="$latestApkName">
                    <span>تحميل أحدث إصدار (APK)</span>
                    <span style="font-size: 1.4rem;">⬇️</span>
                </a>
                <span class="apk-filename-hint">📁 ملف التثبيت المعتمد: $latestApkName</span>
            </div>
            
            <div class="meta-strip">
                <span>📅 <strong>تاريخ الإصدار:</strong> ${latestRelease.releaseDate}</span>
                <span>📦 <strong>الحجم:</strong> ${latestRelease.apkSizeMb} MB</span>
                <span>📱 <strong>النظام:</strong> ${latestRelease.minAndroidVersion}</span>
                <span>📥 <strong>التحميلات:</strong> ${latestRelease.downloadCount}</span>
            </div>
        </div>

        <!-- Intellectual Property & Rights Card -->
        <div class="ip-certificate-card">
            <div class="ip-badge-icon">⚖️</div>
            <div class="ip-info">
                <h4>وثيقة حقوق الملكية الفكرية والنشر الحصرية © 2026</h4>
                <p>
                    هذا التطبيق ومنصة التوزيع <strong>$PLATFORM_DOMAIN</strong> مصممة ومطورة بالكامل بملكية فكرية خالصة للمؤسس والمبتكر: 
                    <span class="ip-author-highlight">$COPYRIGHT_HOLDER</span>
                    <br>
                    محمد رضا محمود محمود سليمه من أسس هذا التطبيق. جميع حقوق النشر، الشيفرات البرمجية، وخوارزميات الدبلجة الصوتية محفوظة قانونياً ودولياً.
                </p>
            </div>
        </div>

        <!-- Highlights Grid -->
        <div class="features-grid">
            <div class="feature-card">
                <div class="feature-icon">🎬</div>
                <h3>دبلجة فيديو متزامنة</h3>
                <p>دمج مسارات الصوت والتسجيلات بدقة متناهية وتصدير مقاطع MP4 فائقة النقاء.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">🤖</div>
                <h3>ذكاء اصطناعي صوتي</h3>
                <p>توليد سيناريوهات، أصوات سينمائية وكرتونية، ومطابقة التوقيت ومخارج الحروف بدقة.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">⚡</div>
                <h3>تحديثات وتنزيل فوري</h3>
                <p>تثبيت حزم الـ APK مباشرة من منصة $PLATFORM_DOMAIN الرسمية بسرعة وأمان تام.</p>
            </div>
        </div>

        <!-- Changelog Section -->
        <div id="changelogs" class="section-header">
            <h2>📜 سجل الإصدارات وتفاصيل التحديثات</h2>
            <span style="color: var(--text-dim); font-size: 0.92rem;">إجمالي الإصدارات: ${allReleases.size}</span>
        </div>

        $releasesListHtml

        <!-- Instructions Guide -->
        <div class="install-guide">
            <h3>📖 دليل تثبيت ملف APK على هاتفك</h3>
            <ol class="steps-list">
                <li>اضغط على زر <strong>تحميل أحدث إصدار (APK)</strong> لحفظ ملف التطبيق على جهازك.</li>
                <li>بعد اكتمال التنزيل، انقر على الإشعار أو توجه إلى مدير الملفات &gt; مجلد التنزيلات (Downloads).</li>
                <li>انقر على ملف <strong>$latestApkName</strong> لتثبيته. إذا طلب الهاتف إذناً، فعّل "السماح بالتثبيت من هذا المصدر".</li>
                <li>اضغط <strong>تثبيت (Install)</strong> أو <strong>تحديث (Update)</strong> وسيتم تحديث تطبيق فويس ماستر برو مع حفظ كافة مشاريعك!</li>
            </ol>
        </div>

        <!-- Legal Footer -->
        <div class="footer">
            <div class="footer-copyright">جميع حقوق الملكية الفكرية والنشر محفوظة © 2026 لتطبيق $PLATFORM_NAME</div>
            <div class="footer-author">المالك والمؤسس: $COPYRIGHT_HOLDER (محمد رضا محمود محمود سليمه من أسس هذا التطبيق)</div>
        </div>

    </div>

</body>
</html>
        """.trimIndent()
    }
}
