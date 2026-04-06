package com.trangulo.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket
import java.net.InetSocketAddress

class AdbManager {

    private var socket: Socket? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    suspend fun connectAdb(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            _statusMessage.value = "Connecting to $host:$port..."
            socket = Socket()
            socket?.connect(InetSocketAddress(host, port), 5000)
            socket?.soTimeout = 3000
            _isConnected.value = true
            _statusMessage.value = "Connected to $host:$port"
            true
        } catch (e: Exception) {
            _isConnected.value = false
            _statusMessage.value = "Connection failed: ${e.message}"
            false
        }
    }

    suspend fun disconnectAdb() = withContext(Dispatchers.IO) {
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        socket = null
        _isConnected.value = false
        _statusMessage.value = "Disconnected"
    }

    suspend fun checkConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            socket?.let { s ->
                if (s.isClosed || !s.isConnected) {
                    _isConnected.value = false
                    return@withContext false
                }
                s.soTimeout = 2000
                val out = s.getOutputStream()
                out.write(0x000C)
                out.write("host:version".toByteArray())
                out.write(0x0A)
                out.flush()
                true
            } ?: false
        } catch (e: Exception) {
            _isConnected.value = false
            _statusMessage.value = "Connection lost: ${e.message}"
            false
        }
    }
}
