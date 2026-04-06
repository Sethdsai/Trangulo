package com.trangulo.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ShellManager {

    private var process: Process? = null
    private var writer: java.io.OutputStream? = null
    private var reader: BufferedReader? = null
    private var isReading = false

    private val _output = MutableStateFlow("")
    val output: StateFlow<String> = _output

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isRoot = MutableStateFlow(false)
    val isRoot: StateFlow<Boolean> = _isRoot

    suspend fun startShell(): Boolean = withContext(Dispatchers.IO) {
        if (_isRunning.value) return@withContext true
        try {
            process = Runtime.getRuntime().exec("su")
            reader = BufferedReader(InputStreamReader(process!!.inputStream))
            writer = process!!.outputStream
            Thread.sleep(300)
            val errorReader = BufferedReader(InputStreamReader(process!!.errorStream))
            val errorLine = errorReader.readLine()
            _isRoot.value = errorLine == null
            if (!_isRoot.value) {
                process?.destroy()
                process = Runtime.getRuntime().exec("sh")
                reader = BufferedReader(InputStreamReader(process!!.inputStream))
                writer = process!!.outputStream
            }
            _isRunning.value = true
            isReading = true
            startReading()
            true
        } catch (e: Exception) {
            try {
                process = ProcessBuilder("sh").start()
                reader = BufferedReader(InputStreamReader(process!!.inputStream))
                writer = process!!.outputStream
                _isRunning.value = true
                _isRoot.value = false
                isReading = true
                startReading()
                true
            } catch (ex: Exception) {
                _output.value = _output.value + "\n[ERROR] Failed to start shell: ${ex.message}"
                false
            }
        }
    }

    private fun startReading() {
        Thread {
            try {
                val buf = CharArray(1024)
                while (isReading && !Thread.currentThread().isInterrupted) {
                    val count = reader?.read(buf) ?: -1
                    if (count == -1) break
                    val text = String(buf, 0, count)
                    _output.value = _output.value + text
                }
            } catch (_: Exception) {
            }
        }.start()
    }

    suspend fun executeCommand(cmd: String): String = withContext(Dispatchers.IO) {
        if (!_isRunning.value) return@withContext "[ERROR] Shell not running"
        try {
            writer?.write(("$cmd\n").toByteArray())
            writer?.flush()
            "[CMD] $cmd"
        } catch (e: Exception) {
            "[ERROR] ${e.message}"
        }
    }

    suspend fun stopShell() = withContext(Dispatchers.IO) {
        isReading = false
        try {
            writer?.write("exit\n".toByteArray())
            writer?.flush()
            Thread.sleep(200)
            process?.destroy()
        } catch (_: Exception) {
        }
        process = null
        writer = null
        reader = null
        _isRunning.value = false
        _isRoot.value = false
    }

    fun clearOutput() {
        _output.value = ""
    }
}
