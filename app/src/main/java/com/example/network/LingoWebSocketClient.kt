package com.example.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

sealed class WebSocketConnectionState {
    object Disconnected : WebSocketConnectionState()
    object Connecting : WebSocketConnectionState()
    data class Connected(val url: String) : WebSocketConnectionState()
    data class Error(val reason: String) : WebSocketConnectionState()
}

/**
 * Manages persistent WebSocket connection with the Python server (ws://192.168.1.3:8000/ws/dubbing)
 * for bi-directional real-time audio chunk streaming, live telemetry, and instant dubbing.
 */
class LingoWebSocketClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var shouldKeepConnected: Boolean = false

    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    private val _incomingAudioBytes = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val incomingAudioBytes: SharedFlow<ByteArray> = _incomingAudioBytes.asSharedFlow()

    private var currentIp: String = "192.168.1.3"
    private var currentPort: Int = 8000
    private var currentEndpoint: String = "ws/dubbing"

    fun connect(ip: String, port: Int, endpoint: String = "ws/dubbing") {
        currentIp = ip.trim()
        currentPort = port
        currentEndpoint = endpoint.trim().removePrefix("/")
        shouldKeepConnected = true

        initiateWebSocket()
    }

    private fun initiateWebSocket() {
        webSocket?.cancel()
        _connectionState.value = WebSocketConnectionState.Connecting

        val protocol = if (currentIp.contains("trycloudflare.com") || currentIp.contains("ngrok.io")) "wss" else "ws"
        val cleanHost = currentIp.removePrefix("http://").removePrefix("https://").removePrefix("ws://").removePrefix("wss://")
        val wsUrl = if (cleanHost.contains(":") || protocol == "wss") {
            "$protocol://$cleanHost/$currentEndpoint"
        } else {
            "$protocol://$cleanHost:$currentPort/$currentEndpoint"
        }

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("LingoWebSocket", "Connected to $wsUrl")
                _connectionState.value = WebSocketConnectionState.Connected(wsUrl)
                reconnectJob?.cancel()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    _incomingMessages.emit(text)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                scope.launch {
                    _incomingAudioBytes.emit(bytes.toByteArray())
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _connectionState.value = WebSocketConnectionState.Disconnected
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w("LingoWebSocket", "WebSocket failure: ${t.message}")
                _connectionState.value = WebSocketConnectionState.Error(t.localizedMessage ?: "فشل الاتصال بـ WebSocket")
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!shouldKeepConnected) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(5000)
            if (isActive && shouldKeepConnected) {
                Log.d("LingoWebSocket", "Attempting WebSocket reconnect...")
                initiateWebSocket()
            }
        }
    }

    fun sendText(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    fun sendAudioChunk(data: ByteArray): Boolean {
        return webSocket?.send(ByteString.of(*data)) ?: false
    }

    fun disconnect() {
        shouldKeepConnected = false
        reconnectJob?.cancel()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = WebSocketConnectionState.Disconnected
    }
}
