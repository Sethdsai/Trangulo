package com.trangulo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trangulo.service.AdbManager
import com.trangulo.service.ShellManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranguloViewModel : ViewModel() {

    private val shellManager = ShellManager()
    private val adbManager = AdbManager()

    private val _serviceRunning = MutableStateFlow(false)
    val serviceRunning: StateFlow<Boolean> = _serviceRunning.asStateFlow()

    private val _shellActive = MutableStateFlow(false)
    val shellActive: StateFlow<Boolean> = _shellActive.asStateFlow()

    private val _adbConnected = MutableStateFlow(false)
    val adbConnected: StateFlow<Boolean> = _adbConnected.asStateFlow()

    private val _statusMessage = MutableStateFlow("Ready")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _logOutput = MutableStateFlow("")
    val logOutput: StateFlow<String> = _logOutput.asStateFlow()

    private val _isRoot = MutableStateFlow(false)
    val isRoot: StateFlow<Boolean> = _isRoot.asStateFlow()

    init {
        viewModelScope.launch {
            shellManager.isRunning.collect { running ->
                _shellActive.value = running
            }
        }
        viewModelScope.launch {
            shellManager.isRoot.collect { root ->
                _isRoot.value = root
            }
        }
        viewModelScope.launch {
            shellManager.output.collect { text ->
                _logOutput.value = text
            }
        }
        viewModelScope.launch {
            adbManager.isConnected.collect { connected ->
                _adbConnected.value = connected
            }
        }
        viewModelScope.launch {
            adbManager.statusMessage.collect { msg ->
                if (msg.isNotEmpty()) {
                    _statusMessage.value = msg
                }
            }
        }
    }

    fun startService() {
        viewModelScope.launch {
            try {
                _statusMessage.value = "Starting shell service..."
                val started = shellManager.startShell()
                if (started) {
                    _serviceRunning.value = true
                    _statusMessage.value = if (_isRoot.value) "Root shell active" else "Shell active (non-root)"
                } else {
                    _statusMessage.value = "Failed to start shell"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun stopService() {
        viewModelScope.launch {
            try {
                shellManager.stopShell()
                _serviceRunning.value = false
                _statusMessage.value = "Service stopped"
            } catch (e: Exception) {
                _statusMessage.value = "Error stopping service: ${e.message}"
            }
        }
    }

    fun connectAdb(host: String, port: Int) {
        viewModelScope.launch {
            try {
                val connected = adbManager.connectAdb(host, port)
                if (connected) {
                    _statusMessage.value = "ADB connected to $host:$port"
                }
            } catch (e: Exception) {
                _statusMessage.value = "ADB connection error: ${e.message}"
            }
        }
    }

    fun disconnectAdb() {
        viewModelScope.launch {
            try {
                adbManager.disconnectAdb()
                _statusMessage.value = "ADB disconnected"
            } catch (e: Exception) {
                _statusMessage.value = "Error disconnecting: ${e.message}"
            }
        }
    }

    fun executeCommand(cmd: String) {
        viewModelScope.launch {
            try {
                val result = shellManager.executeCommand(cmd)
                _statusMessage.value = result
            } catch (e: Exception) {
                _statusMessage.value = "Command error: ${e.message}"
            }
        }
    }

    fun clearLogs() {
        shellManager.clearOutput()
    }
}
