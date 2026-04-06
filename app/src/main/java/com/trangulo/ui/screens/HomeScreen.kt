package com.trangulo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trangulo.ui.components.ChipStatus
import com.trangulo.ui.components.StatusChip
import com.trangulo.ui.components.TranguloCard
import com.trangulo.ui.theme.CardBg
import com.trangulo.ui.theme.CardFg
import com.trangulo.viewmodel.TranguloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TranguloViewModel) {
    val shellActive by viewModel.shellActive.collectAsState()
    val adbConnected by viewModel.adbConnected.collectAsState()
    val serviceRunning by viewModel.serviceRunning.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val logOutput by viewModel.logOutput.collectAsState()
    val isRoot by viewModel.isRoot.collectAsState()

    var commandText by remember { mutableStateOf("") }
    var adbHost by remember { mutableStateOf("192.168.1.1") }
    var adbPort by remember { mutableStateOf("5555") }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trangulo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TranguloCard(
                    title = "Shell",
                    subtitle = if (isRoot) "Root access" else "Standard shell",
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusChip(
                        status = if (shellActive) ChipStatus.ACTIVE else ChipStatus.INACTIVE,
                        label = if (shellActive) "Active" else "Inactive"
                    )
                }

                TranguloCard(
                    title = "ADB",
                    subtitle = if (adbConnected) "Wireless" else "Disconnected",
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusChip(
                        status = if (adbConnected) ChipStatus.ACTIVE else ChipStatus.INACTIVE,
                        label = if (adbConnected) "Connected" else "Disconnected"
                    )
                }
            }

            TranguloCard(
                title = "Service Control",
                subtitle = "Start or stop the shell service"
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.startService() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !serviceRunning
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Service")
                    }
                    OutlinedButton(
                        onClick = { viewModel.stopService() },
                        modifier = Modifier.weight(1f),
                        enabled = serviceRunning
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Service")
                    }
                }
            }

            Divider()

            TranguloCard(
                title = "ADB Wireless",
                subtitle = "Connect to a device via ADB over TCP/IP"
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = adbHost,
                        onValueChange = { adbHost = it },
                        label = { Text("Host") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = adbPort,
                        onValueChange = { adbPort = it.filter { c -> c.isDigit() } },
                        label = { Text("Port") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val port = adbPort.toIntOrNull() ?: 5555
                            viewModel.connectAdb(adbHost, port)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !adbConnected && adbHost.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect")
                    }
                    OutlinedButton(
                        onClick = { viewModel.disconnectAdb() },
                        modifier = Modifier.weight(1f),
                        enabled = adbConnected
                    ) {
                        Icon(
                            imageVector = Icons.Default.Usb,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnect")
                    }
                }
            }

            Divider()

            TranguloCard(
                title = "Terminal",
                subtitle = "Execute commands in the active shell"
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBg)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (logOutput.isBlank()) {
                        Text(
                            text = "No output yet. Start the service and run commands.",
                            color = CardFg.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = logOutput,
                            color = CardFg,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commandText,
                        onValueChange = { commandText = it },
                        label = { Text("Command") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            if (commandText.isNotEmpty()) {
                                IconButton(onClick = { commandText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )
                    IconButton(
                        onClick = {
                            if (commandText.isNotBlank()) {
                                viewModel.executeCommand(commandText)
                                commandText = ""
                            }
                        },
                        enabled = shellActive && commandText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (shellActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Execute",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { viewModel.clearLogs() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Logs")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
