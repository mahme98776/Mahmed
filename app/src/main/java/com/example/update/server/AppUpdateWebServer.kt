package com.example.update.server

import android.content.Context
import android.util.Log
import com.example.update.AppUpdateManager
import com.example.update.model.ReleaseChannel
import com.example.update.web.WebPortalHtmlTemplates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class AppUpdateWebServer(
    private val context: Context,
    private val updateManager: AppUpdateManager,
    private val port: Int = 8080
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var serverJob: Job? = null
    private var serverSocket: ServerSocket? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow("http://localhost:$port")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _lanUrl = MutableStateFlow("http://localhost:$port")
    val lanUrl: StateFlow<String> = _lanUrl.asStateFlow()

    fun start() {
        if (_isRunning.value) return
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                _isRunning.value = true
                val lanIp = getLocalIpAddress()
                _serverUrl.value = "http://localhost:$port"
                _lanUrl.value = "http://$lanIp:$port"
                Log.d("AppUpdateWebServer", "mody.org server started at ${_lanUrl.value}")

                while (isActive && serverSocket?.isClosed == false) {
                    val clientSocket = serverSocket?.accept() ?: break
                    scope.launch {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                Log.e("AppUpdateWebServer", "Server error: ${e.message}", e)
            } finally {
                _isRunning.value = false
            }
        }
    }

    fun stop() {
        try {
            serverSocket?.close()
            serverSocket = null
            serverJob?.cancel()
            serverJob = null
        } catch (e: Exception) {
            Log.e("AppUpdateWebServer", "Error stopping server: ${e.message}")
        } finally {
            _isRunning.value = false
        }
    }

    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            socket.soTimeout = 5000
            socket.use { s ->
                val reader = BufferedReader(InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))
                val output = s.getOutputStream()

                val requestLine = reader.readLine() ?: return@withContext
                val parts = requestLine.split(" ")
                if (parts.size < 2) return@withContext

                val method = parts[0]
                val fullPath = parts[1]
                val path = fullPath.substringBefore("?")

                // Read Headers
                var contentLength = 0
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) break
                    if (line!!.startsWith("Content-Length:", ignoreCase = true)) {
                        contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                }

                // Read Body if POST
                var body = ""
                if (method.equals("POST", ignoreCase = true) && contentLength > 0) {
                    val charBuffer = CharArray(contentLength)
                    var readTotal = 0
                    while (readTotal < contentLength) {
                        val read = reader.read(charBuffer, readTotal, contentLength - readTotal)
                        if (read == -1) break
                        readTotal += read
                    }
                    body = String(charBuffer, 0, readTotal)
                }

                routeRequest(method, path, body, output)
            }
        } catch (e: java.net.SocketException) {
            Log.d("AppUpdateWebServer", "Client disconnected normally: ${e.message}")
        } catch (e: java.io.IOException) {
            Log.d("AppUpdateWebServer", "IO completed or interrupted: ${e.message}")
        } catch (e: Exception) {
            Log.w("AppUpdateWebServer", "Request error: ${e.message}")
        }
    }

    private fun routeRequest(method: String, path: String, body: String, out: OutputStream) {
        when {
            // 1. User Web Portal (mody.org landing page)
            (method == "GET" && (path == "/" || path == "/user" || path == "/updates" || path == "/index.html" || path == "/mody")) -> {
                val latest = updateManager.latestRelease.value ?: updateManager.releases.value.first()
                val html = WebPortalHtmlTemplates.generateUserPortalHtml(
                    latestRelease = latest,
                    allReleases = updateManager.releases.value,
                    serverUrl = _lanUrl.value
                )
                sendHtmlResponse(out, 200, html)
            }

            // 2. Developer Web Portal (HTML Admin Dashboard)
            (method == "GET" && (path == "/developer" || path == "/admin" || path == "/dev")) -> {
                val html = WebPortalHtmlTemplates.generateDeveloperPortalHtml(
                    releases = updateManager.releases.value,
                    serverUrl = _lanUrl.value
                )
                sendHtmlResponse(out, 200, html)
            }

            // 3. API - Publish Update (Upload APK details from Developer)
            (method == "POST" && path == "/api/publish-update") -> {
                handlePublishUpdate(body, out)
            }

            // 4. API - Delete Release
            (method == "POST" && path == "/api/delete-release") -> {
                handleDeleteRelease(body, out)
            }

            // 5. API - Latest Version JSON
            (method == "GET" && path == "/api/latest") -> {
                val latest = updateManager.latestRelease.value ?: updateManager.releases.value.first()
                val json = JSONObject().apply {
                    put("versionCode", latest.versionCode)
                    put("versionName", latest.versionName)
                    put("title", latest.releaseTitle)
                    put("notesAr", latest.releaseNotesArabic)
                    put("notesEn", latest.releaseNotesEnglish)
                    put("downloadUrl", latest.downloadUrl)
                    put("sizeMb", latest.apkSizeMb)
                    put("date", latest.releaseDate)
                    put("isCritical", latest.isCritical)
                    put("channel", latest.channel.name)
                    put("apkFileName", latest.apkFileName)
                    put("domain", WebPortalHtmlTemplates.PLATFORM_DOMAIN)
                }
                sendJsonResponse(out, 200, json.toString())
            }

            // 6. API - All Releases JSON
            (method == "GET" && path == "/api/releases") -> {
                val array = JSONArray()
                for (rel in updateManager.releases.value) {
                    array.put(JSONObject().apply {
                        put("id", rel.id)
                        put("versionCode", rel.versionCode)
                        put("versionName", rel.versionName)
                        put("title", rel.releaseTitle)
                        put("notesAr", rel.releaseNotesArabic)
                        put("sizeMb", rel.apkSizeMb)
                        put("date", rel.releaseDate)
                        put("downloads", rel.downloadCount)
                        put("channel", rel.channel.name)
                        put("apkFileName", rel.apkFileName)
                    })
                }
                sendJsonResponse(out, 200, array.toString())
            }

            // 7. APK Download Delivery
            (method == "GET" && path.startsWith("/download/")) -> {
                val releaseId = path.substringAfter("/download/")
                val targetRelease = updateManager.releases.value.find { it.id == releaseId }
                updateManager.incrementDownloadCount(releaseId)
                val fileName = targetRelease?.apkFileName?.ifBlank { null }
                    ?: "mody-dubbing-${targetRelease?.versionName ?: "latest"}.apk"
                sendApkFileResponse(out, fileName)
            }

            // 8. Favicon
            (method == "GET" && path == "/favicon.ico") -> {
                sendNoContent(out)
            }

            else -> {
                sendNotFound(out)
            }
        }
    }

    private fun handlePublishUpdate(body: String, out: OutputStream) {
        try {
            val params = parseUrlEncodedForm(body)
            val versionName = params["versionName"] ?: "1.3.0"
            val versionCode = params["versionCode"]?.toIntOrNull() ?: (updateManager.releases.value.maxOfOrNull { it.versionCode } ?: 1) + 1
            val releaseTitle = params["releaseTitle"] ?: "تحديث جديد"
            val releaseNotesArabic = params["releaseNotesArabic"] ?: "تحسينات عامة على استوديو الدبلجة."
            val channelStr = params["channel"] ?: "STABLE"
            val channel = try { ReleaseChannel.valueOf(channelStr) } catch (e: Exception) { ReleaseChannel.STABLE }
            val apkSizeMb = params["apkSizeMb"]?.toDoubleOrNull() ?: 18.5
            val downloadUrl = params["downloadUrl"] ?: "/download/latest.apk"
            val isCritical = params["isCritical"] == "true"
            val apkFileName = params["apkFileName"] ?: "mody-dubbing-v$versionName.apk"

            updateManager.publishNewRelease(
                versionName = versionName,
                versionCode = versionCode,
                releaseTitle = releaseTitle,
                releaseNotesArabic = releaseNotesArabic,
                apkSizeMb = apkSizeMb,
                downloadUrl = downloadUrl,
                isCritical = isCritical,
                channel = channel,
                apkFileName = apkFileName
            )

            // Redirect back to developer portal
            sendRedirect(out, "/developer")
        } catch (e: Exception) {
            sendHtmlResponse(out, 400, "<h1>خطأ في نشر التحديث: ${e.message}</h1>")
        }
    }

    private fun handleDeleteRelease(body: String, out: OutputStream) {
        try {
            val params = parseUrlEncodedForm(body)
            val releaseId = params["releaseId"]
            if (!releaseId.isNullOrBlank()) {
                updateManager.deleteRelease(releaseId)
            }
            sendRedirect(out, "/developer")
        } catch (e: Exception) {
            sendRedirect(out, "/developer")
        }
    }

    private fun parseUrlEncodedForm(body: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val pairs = body.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx > 0) {
                val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                map[key] = value
            }
        }
        return map
    }

    private fun sendHtmlResponse(out: OutputStream, statusCode: Int, html: String) {
        try {
            val bytes = html.toByteArray(StandardCharsets.UTF_8)
            val header = "HTTP/1.1 $statusCode OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: ${bytes.size}\r\n" +
                    "Connection: close\r\n" +
                    "\r\n"
            out.write(header.toByteArray(StandardCharsets.UTF_8))
            out.write(bytes)
            out.flush()
        } catch (e: Exception) {
            // Client closed stream prematurely
        }
    }

    private fun sendJsonResponse(out: OutputStream, statusCode: Int, json: String) {
        try {
            val bytes = json.toByteArray(StandardCharsets.UTF_8)
            val header = "HTTP/1.1 $statusCode OK\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "Content-Length: ${bytes.size}\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Connection: close\r\n" +
                    "\r\n"
            out.write(header.toByteArray(StandardCharsets.UTF_8))
            out.write(bytes)
            out.flush()
        } catch (e: Exception) {
            // Client closed stream prematurely
        }
    }

    private fun sendRedirect(out: OutputStream, location: String) {
        try {
            val header = "HTTP/1.1 303 See Other\r\n" +
                    "Location: $location\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n"
            out.write(header.toByteArray(StandardCharsets.UTF_8))
            out.flush()
        } catch (e: Exception) {
            // Client closed stream prematurely
        }
    }

    private fun sendApkFileResponse(out: OutputStream, fileName: String) {
        try {
            val dummyApkBytes = "MODY_ORG_APK_PACKAGE_DUBBING_STUDIO_BYTES_VALID_INSTALL".toByteArray(StandardCharsets.UTF_8)
            val header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/vnd.android.package-archive\r\n" +
                    "Content-Disposition: attachment; filename=\"$fileName\"\r\n" +
                    "Content-Length: ${dummyApkBytes.size}\r\n" +
                    "Connection: close\r\n" +
                    "\r\n"
            out.write(header.toByteArray(StandardCharsets.UTF_8))
            out.write(dummyApkBytes)
            out.flush()
        } catch (e: Exception) {
            // Client closed stream prematurely
        }
    }

    private fun sendNoContent(out: OutputStream) {
        try {
            val header = "HTTP/1.1 204 No Content\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n"
            out.write(header.toByteArray(StandardCharsets.UTF_8))
            out.flush()
        } catch (e: Exception) {
            // Client closed stream prematurely
        }
    }

    private fun sendNotFound(out: OutputStream) {
        val body = "<h1>404 Page Not Found - mody.org</h1>"
        sendHtmlResponse(out, 404, body)
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AppUpdateWebServer", "Error getting IP address: ${e.message}")
        }
        return "127.0.0.1"
    }
}
