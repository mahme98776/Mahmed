package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

sealed class ServerConnectionStatus {
    object Disconnected : ServerConnectionStatus()
    object Connecting : ServerConnectionStatus()
    data class Connected(
        val ip: String,
        val port: Int,
        val pingMs: Long,
        val serverInfo: String? = null
    ) : ServerConnectionStatus()
    data class Processing(val stage: String, val progressFraction: Float) : ServerConnectionStatus()
    data class Error(val message: String, val detailedTroubleshooting: String) : ServerConnectionStatus()
}

/**
 * Manages connection testing, health pings, network reachability, and automatic reconnection
 * to the Python server at 192.168.1.3 (or user-customized IP/port).
 */
class ConnectionManager(
    private val context: Context,
    val retrofitManager: RetrofitClientManager = RetrofitClientManager(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _connectionStatus = MutableStateFlow<ServerConnectionStatus>(ServerConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ServerConnectionStatus> = _connectionStatus.asStateFlow()

    private var currentIp: String = "192.168.1.3"
    private var currentPort: Int = 8000
    private var isAutoReconnectEnabled: Boolean = true
    private var monitoringJob: Job? = null

    init {
        startConnectionMonitor()
    }

    fun updateTarget(ip: String, port: Int) {
        currentIp = ip.trim()
        currentPort = port
        checkConnectionNow()
    }

    fun setAutoReconnect(enabled: Boolean) {
        isAutoReconnectEnabled = enabled
    }

    /**
     * Checks if the device is connected to Wi-Fi or local network.
     */
    fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    /**
     * Attempts to ping and connect to the configured Python server.
     */
    fun checkConnectionNow() {
        scope.launch {
            testReachability(currentIp, currentPort)
        }
    }

    suspend fun testReachability(ip: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        _connectionStatus.value = ServerConnectionStatus.Connecting
        val startTime = System.currentTimeMillis()

        // 1. Check local WiFi
        if (!isWifiConnected()) {
            val error = ServerConnectionStatus.Error(
                message = "الهاتف غير متصل بشبكة Wi-Fi",
                detailedTroubleshooting = "يرجى تشغيل الواي فاي والتأكد من الاتصال بنفس الراوتر المتصل به جهاز الكمبيوتر ($ip)."
            )
            _connectionStatus.value = error
            return@withContext false
        }

        // 2. Perform low-level socket reachability check
        val socketReachable = try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 3500)
                true
            }
        } catch (e: Exception) {
            false
        }

        // 3. If socket opened, verify HTTP health endpoint via Retrofit
        if (socketReachable) {
            try {
                val api = retrofitManager.getApiService(ip, port)
                val response = api.checkHealth()
                val latency = System.currentTimeMillis() - startTime

                if (response.isSuccessful) {
                    val body = response.body()
                    val info = body?.let { "${it.server} (${it.cudaDevice ?: "CPU"})" } ?: "سيرفر البايثون نشط"
                    _connectionStatus.value = ServerConnectionStatus.Connected(
                        ip = ip,
                        port = port,
                        pingMs = latency,
                        serverInfo = info
                    )
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w("ConnectionManager", "Health check API returned error: ${e.message}")
            }

            // Socket succeeded even if health endpoint is 404
            val latency = System.currentTimeMillis() - startTime
            _connectionStatus.value = ServerConnectionStatus.Connected(
                ip = ip,
                port = port,
                pingMs = latency,
                serverInfo = "منفذ البايثون $port متصل بنجاح"
            )
            return@withContext true
        } else {
            val troubleshootingMsg = buildString {
                append("تعذر الوصول إلى $ip:$port.\n\n")
                append("خطوات التحقق:\n")
                append("1. تأكد من أن الهاتف والكمبيوتر متصلان بنفس شبكة الواي فاي.\n")
                append("2. تأكد من تشغيل سكريبت بايثون على الكمبيوتر بوضع 0.0.0.0 (وليس 127.0.0.1).\n")
                append("3. تأكد من سماح جدار حماية الويندوز (Windows Firewall) للمنفذ $port بالمرور.")
            }

            _connectionStatus.value = ServerConnectionStatus.Error(
                message = "فشل الاتصال بسيرفر الكمبيوتر ($ip:$port)",
                detailedTroubleshooting = troubleshootingMsg
            )
            return@withContext false
        }
    }

    /**
     * Continuous background monitor with automatic reconnection logic.
     */
    private fun startConnectionMonitor() {
        monitoringJob?.cancel()
        monitoringJob = scope.launch {
            while (isActive) {
                delay(10_000) // Check every 10 seconds
                if (isAutoReconnectEnabled) {
                    val current = _connectionStatus.value
                    if (current is ServerConnectionStatus.Disconnected || current is ServerConnectionStatus.Error) {
                        Log.d("ConnectionManager", "Auto-reconnecting to $currentIp:$currentPort...")
                        testReachability(currentIp, currentPort)
                    }
                }
            }
        }
    }

    fun updateProcessingStatus(stage: String, progress: Float) {
        _connectionStatus.value = ServerConnectionStatus.Processing(stage, progress)
    }

    fun restoreConnectedState() {
        _connectionStatus.value = ServerConnectionStatus.Connected(
            ip = currentIp,
            port = currentPort,
            pingMs = 15L,
            serverInfo = "متصل وجاهز"
        )
    }
}
