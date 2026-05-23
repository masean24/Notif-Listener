package com.example.ui.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.model.NotificationLog
import com.example.data.model.WebhookLog
import com.example.data.model.WebhookTarget
import com.example.data.model.RoutingProfile
import com.example.ui.viewmodel.QrisViewModel
import com.example.util.OemAutoStartHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrisMainScreen(viewModel: QrisViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("dashboard") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "QRIS Monitor Bridge", 
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = Color(0xFFE6E1E9)
                    ) 
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings, 
                            contentDescription = "Pengaturan",
                            tint = Color(0xFFE6E1E9)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F)
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == "dashboard",
                    onClick = { currentTab = "dashboard" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("tab_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == "webhook",
                    onClick = { currentTab = "webhook" },
                    icon = { Icon(Icons.Default.Send, contentDescription = "Webhook") },
                    label = { Text("Webhook") },
                    modifier = Modifier.testTag("tab_webhook")
                )
                NavigationBarItem(
                    selected = currentTab == "filter",
                    onClick = { currentTab = "filter" },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Rute") },
                    label = { Text("Rute") },
                    modifier = Modifier.testTag("tab_filter")
                )
                NavigationBarItem(
                    selected = currentTab == "speaker",
                    onClick = { currentTab = "speaker" },
                    icon = { Icon(Icons.Default.VolumeUp, contentDescription = "Speaker") },
                    label = { Text("Speaker") },
                    modifier = Modifier.testTag("tab_speaker")
                )
                NavigationBarItem(
                    selected = currentTab == "history",
                    onClick = { currentTab = "history" },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "dashboard" -> DashboardTab(viewModel)
                "webhook" -> WebhookConfigTab(viewModel)
                "filter" -> FilterTab(viewModel)
                "speaker" -> SpeakerSettingsTab(viewModel)
                "history" -> HistoryTab(viewModel)
            }
        }
    }

    if (showSettingsDialog) {
        SystemSettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

@Composable
fun DashboardTab(viewModel: QrisViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-refresh states periodically
    var isAccessGranted by remember { mutableStateOf(false) }
    var isBatteryOptimizingOff by remember { mutableStateOf(false) }
    
    fun refreshSystemStates() {
        val cn = ComponentName(context, "com.example.service.QrisNotificationListenerService")
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        isAccessGranted = flat != null && flat.contains(cn.flattenToString())

        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        isBatteryOptimizingOff = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else {
            true
        }
    }

    LaunchedEffect(Unit) {
        while(true) {
            refreshSystemStates()
            delay(1500)
        }
    }

    // Connect parameters
    val totalNotif by viewModel.totalNotificationsCount.collectAsState()
    val successWebs by viewModel.successWebhookCount.collectAsState()
    val failedWebs by viewModel.failedWebhookCount.collectAsState()
    val pendingWebs by viewModel.pendingWebhookCount.collectAsState()
    val webhookTargets by viewModel.targets.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header intro
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF49454F)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Status Monitor Pembayaran",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD0BCFF)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mengambil notifikasi pembayaran QRIS langsung dari perangkat secara realtime, membacakannya dengan suara, dan mendorongnya ke webhook server Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE6E1E9),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Checklist Status Items
        Text("Pemeriksaan Persyaratan Sistem", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

        // 1. Notification Access
        StatusIndicatorCard(
            title = "Izin Akses Notifikasi",
            description = if (isAccessGranted) "Izin diberikan, terus memantau." else "Izin diperlukan untuk membaca notifikasi pembayaran.",
            isOk = isAccessGranted,
            actionLabel = "Buka Izin",
            onAction = {
                try {
                    context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal membuka pengaturan", Toast.LENGTH_SHORT).show()
                }
            },
            tag = "btn_open_notification_access"
        )

        // 2. Webhook target status
        val targetsOk = webhookTargets.isNotEmpty()
        StatusIndicatorCard(
            title = "Konfigurasi Webhook",
            description = if (targetsOk) "${webhookTargets.size} target diaktifkan." else "Belum ada tujuan webhook yang ditambahkan.",
            isOk = targetsOk,
            actionLabel = if (targetsOk) "Lihat Target" else "Atur Sekarang",
            onAction = {
                // Instantly navigate or let user configure
            },
            tag = "btn_configure_webhook"
        )

        // 3. Listener state indicators
        val listenerActive = isAccessGranted // bound strictly via the listener access
        StatusIndicatorCard(
            title = "Layanan Pemantau (Listener)",
            description = if (listenerActive) "🟢 Aktif (Latar belakang memantau)" else "🔴 Mati (Izin dinonaktifkan)",
            isOk = listenerActive,
            onAction = null,
            tag = "status_listener"
        )

        // 4. Battery Optimization status
        StatusIndicatorCard(
            title = "Abaikan Optimasi Baterai",
            description = if (isBatteryOptimizingOff) "Whitelist aktif (Aman dari pembatasan latar belakang)" else "⚠️ Sistem dapat menghentikan monitor sewaktu-waktu.",
            isOk = isBatteryOptimizingOff,
            actionLabel = "Perbaiki",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Minta whitelist secara manual di pengaturan", Toast.LENGTH_LONG).show()
                    }
                }
            },
            tag = "btn_ignore_battery"
        )

        // 5. OEM Autostart Warnings
        if (OemAutoStartHelper.isOemDevice()) {
            OemAutoStartWarningCard(context)
        }

        // Collect newly added revenue indicators
        val totalRevenue by viewModel.totalRevenueAmount.collectAsState()
        val revenueBreakdown by viewModel.revenueByApp.collectAsState()

        // Stats summary Grid
        Text("Statistik Penjualan Hari Ini", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        
        // Premium Total Revenue Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Total Uang Masuk Terbaca",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                    maximumFractionDigits = 0
                }
                Text(
                    text = rupiahFormat.format(totalRevenue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(modifier = Modifier.weight(1f), title = "Total Pembayaran", value = totalNotif.toString(), color = MaterialTheme.colorScheme.primary)
            StatCard(modifier = Modifier.weight(1f), title = "Sukses Kirim", value = successWebs.toString(), color = Color(0xFF81C784))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(modifier = Modifier.weight(1f), title = "Gagal Kirim", value = failedWebs.toString(), color = Color(0xFFE57373))
            StatCard(modifier = Modifier.weight(1f), title = "Antrean Pending", value = pendingWebs.toString(), color = Color(0xFFFFB74D))
        }

        if (revenueBreakdown.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Distribusi Pendapatan QRIS",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val sortedBreakdown = revenueBreakdown.entries.sortedByDescending { it.value }
                    val maxRevenue = sortedBreakdown.firstOrNull()?.value ?: 1
                    
                    sortedBreakdown.forEach { (appName, amountPlus) ->
                        val ratio = if (maxRevenue > 0) amountPlus.toFloat() / maxRevenue.toFloat() else 0f
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(appName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                val rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                                    maximumFractionDigits = 0
                                }
                                Text(rupiah.format(amountPlus), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Visual horizontal progress index
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = ratio.coerceIn(0.01f, 1.0f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Speaker Test
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Uji Coba Pengeras Suara", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("Tekan tombol untuk tes pengucapan nominal.", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { viewModel.testSpeak() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("btn_test_speaker_quick")
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tes Suara")
                }
            }
        }
    }
}

@Composable
fun StatusIndicatorCard(
    title: String,
    description: String,
    isOk: Boolean,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isOk) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOk) Color(0xFF4CAF50) else Color(0xFFF44336))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 18.dp)
                )
            }
            if (actionLabel != null && onAction != null) {
                OutlinedButton(
                    onClick = onAction,
                    border = ButtonDefaults.outlinedButtonBorder,
                    modifier = Modifier
                        .testTag(tag)
                        .padding(start = 8.dp)
                ) {
                    Text(text = actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OemAutoStartWarningCard(context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2620)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFB74D))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ponsel ${OemAutoStartHelper.getOemName()} Terdeteksi!", 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB74D)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = OemAutoStartHelper.getAutoStartInstructions(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE6E1E9)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { OemAutoStartHelper.openAutoStartSettings(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D), contentColor = Color(0xFF381E72)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_oem_autostart")
            ) {
                Text("Buka Pengaturan Mulai Otomatis", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun WebhookConfigTab(viewModel: QrisViewModel) {
    val webhookTargets by viewModel.targets.collectAsState()
    val testResult by viewModel.testSenderResult.collectAsState()
    val context = LocalContext.current

    var showEditor by remember { mutableStateOf(false) }
    var targetToEdit by remember { mutableStateOf<WebhookTarget?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (webhookTargets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("⚠️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Belum Ada Target Webhook", 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Tambahkan server tujuan webhook Anda agar pembayaran QRIS dapat langsung dipost ke sistem Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        targetToEdit = null
                        showEditor = true 
                    },
                    modifier = Modifier.testTag("btn_add_webhook_first")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Webhook Target")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Daftar Server Webhook", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(webhookTargets) { target ->
                    WebhookTargetCard(
                        target = target,
                        onEdit = { 
                            targetToEdit = target
                            showEditor = true 
                        },
                        onDelete = { viewModel.deleteTarget(target) },
                        onToggleEnabled = { isEnabled -> 
                            viewModel.updateTarget(target.copy(enabled = isEnabled)) 
                        },
                        onTestSend = { 
                            viewModel.testWebhook(target) 
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            FloatingActionButton(
                onClick = {
                    targetToEdit = null
                    showEditor = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_webhook"),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Webhook")
            }
        }
    }

    if (showEditor) {
        WebhookEditorDialog(
            target = targetToEdit,
            onDismiss = { showEditor = false },
            onSave = { entity ->
                if (targetToEdit == null) {
                    viewModel.insertTarget(entity)
                } else {
                    viewModel.updateTarget(entity)
                }
                showEditor = false
            }
        )
    }

    if (testResult != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearTestResult() },
            title = { Text("Uji Coba Pengiriman") },
            text = { Text(testResult ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearTestResult() }) {
                    Text("OK")
                }
            },
            modifier = Modifier.testTag("dialog_test_result")
        )
    }
}

@Composable
fun WebhookTargetCard(
    target: WebhookTarget,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onTestSend: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(target.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = target.url, 
                        style = MaterialTheme.typography.bodySmall, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = target.enabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.testTag("switch_target_${target.id}")
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode: ${target.payloadMode.replaceFirstChar { it.uppercase() }}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                if (!target.customTemplate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Custom Template",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onTestSend,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("btn_test_target_${target.id}")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tes Send", fontSize = 11.sp)
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.testTag("btn_edit_target_${target.id}")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("btn_delete_target_${target.id}")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebhookEditorDialog(
    target: WebhookTarget?,
    onDismiss: () -> Unit,
    onSave: (WebhookTarget) -> Unit
) {
    var name by remember { mutableStateOf(target?.name ?: "") }
    var url by remember { mutableStateOf(target?.url ?: "") }
    var secret by remember { mutableStateOf(target?.secret ?: "") }
    var payloadMode by remember { mutableStateOf(target?.payloadMode ?: "simple") }
    var customTemplate by remember { mutableStateOf(target?.customTemplate ?: "") }
    var isEnabled by remember { mutableStateOf(target?.enabled ?: true) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (target == null) "Tambah Webhook" else "Edit Webhook", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Webhook (contoh: Server Toko)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_target_name")
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL Webhook (POST)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_target_url")
                )
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("X-Webhook-Secret Header Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_target_secret")
                )
                
                Text("Payload Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { payloadMode = "simple" }
                    ) {
                        RadioButton(selected = payloadMode == "simple", onClick = { payloadMode = "simple" })
                        Text("Simple (MengQRIS)")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { payloadMode = "extended" }
                    ) {
                        RadioButton(selected = payloadMode == "extended", onClick = { payloadMode = "extended" })
                        Text("Extended JSON")
                    }
                }

                OutlinedTextField(
                    value = customTemplate,
                    onValueChange = { customTemplate = it },
                    label = { Text("Custom JSON Template (Kosongkan jika default)") },
                    placeholder = { Text("{\"pesan\":\"Pembayaran {amount} masuk!\"}") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_target_template")
                )
                Text(
                    text = "Placeholder: {app_name}, {app_package}, {title}, {text}, {big_text}, {amount}, {timestamp}, {device_id}, {dedupe_key}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || url.isBlank()) {
                        errorMsg = "Nama dan URL wajib diisi!"
                        return@Button
                    }
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        errorMsg = "URL harus diawali dengan http:// atau https://"
                        return@Button
                    }
                    
                    val entity = WebhookTarget(
                        id = target?.id ?: 0,
                        name = name,
                        url = url,
                        secret = secret,
                        payloadMode = payloadMode,
                        customTemplate = customTemplate.ifBlank { null },
                        enabled = isEnabled
                    )
                    onSave(entity)
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        modifier = Modifier.testTag("dialog_webhook_editor")
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterTab(viewModel: QrisViewModel) {
    val profiles by viewModel.profiles.collectAsState()
    val webhooks by viewModel.targets.collectAsState()
    val isRoutingModeOnlyFirst by viewModel.isRoutingModeOnlyFirst.collectAsState()

    // For legacy fallback display
    val allowedPackages by viewModel.allowedPackages.collectAsState()
    val positiveKeywords by viewModel.positiveKeywords.collectAsState()
    val negativeKeywords by viewModel.negativeKeywords.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedProfileForEdit by remember { mutableStateOf<com.example.data.model.RoutingProfile?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header intro
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Profil Rute Notifikasi (Routing Profile)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gunakan rute cerdas untuk mengarahkan notifikasi dari aplikasi tertentu secara spesifik ke webhook yang dikehendaki, lengkap dengan penyaringan keyword, template payload khusus, dan kontrol pengeras suara individual.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Conflict Resolution option
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Opsi Benturan Rute (Mode Konflik)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Tentukan alur kerja jika suatu notifikasi cocok dengan lebih dari satu profil rute.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isRoutingModeOnlyFirst) "Hanya Kirim Rute Pertama (Priority)" else "Kirim ke Semua Rute yang Cocok",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isRoutingModeOnlyFirst) "Notifikasi hanya diproses oleh profil dengan prioritas tertinggi yang cocok." else "Notifikasi akan dikirim ke semua profil rute yang kriteria penyaringannya cocok.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isRoutingModeOnlyFirst,
                        onCheckedChange = { viewModel.setRoutingModeOnlyFirst(it) },
                        modifier = Modifier.testTag("switch_routing_mode_only_first")
                    )
                }
            }
        }

        // Section header and fab-like row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Daftar Profil Rute (${profiles.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Button(
                onClick = {
                    selectedProfileForEdit = null
                    showEditDialog = true
                },
                modifier = Modifier.testTag("btn_add_routing_profile")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah Rute")
            }
        }

        if (profiles.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Belum Ada Profil Rute",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Tambahkan profil rute pertama Anda agar notifikasi dipilah dengan cerdas. Jika tidak ada profil rute yang dibuat, sistem akan otomatis menggunakan filter warisan/global di bawah.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            profiles.forEach { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profile.enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (profile.enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title and priority and controls row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = profile.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (profile.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Pri: ${profile.priority}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = profile.enabled,
                                    onCheckedChange = { viewModel.updateProfile(profile.copy(enabled = it)) }
                                )
                                IconButton(
                                    onClick = {
                                        selectedProfileForEdit = profile
                                        showEditDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.deleteProfile(profile) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Apps packages list
                        Text("Aplikasi Dipantau:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        if (profile.packages.isEmpty()) {
                            Text("Semua aplikasi (Tanpa Filter)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                profile.packages.forEach { pkg ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(pkg, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), maxLines = 1)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Positive keywords list
                        if (profile.positiveKeywords.isNotEmpty()) {
                            Text("Kata Kunci Positif:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                profile.positiveKeywords.forEach { kw ->
                                    Surface(
                                        color = Color(0xFF1B3D2F),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(kw, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF81C784))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Negative keywords list
                        if (profile.negativeKeywords.isNotEmpty()) {
                            Text("Kata Kunci Negatif:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                profile.negativeKeywords.forEach { kw ->
                                    Surface(
                                        color = Color(0xFF4C1D1D),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(kw, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFE57373))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Linked Webhooks list
                        Text("Kirim ke Webhook Target:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        val connectedWebhooks = webhooks.filter { profile.webhookTargetIds.contains(it.id) }
                        if (connectedWebhooks.isEmpty()) {
                            Text("Peringatan: Belum ada webhook terhubung dengan rute ini!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                connectedWebhooks.forEach { wb ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(wb.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        }

                        // Custom TTS or custom template indicators
                        if (profile.ttsEnabled || !profile.customTemplate.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (profile.ttsEnabled) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Kustom TTS Suara", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (!profile.customTemplate.isNullOrBlank()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Kustom JSON Template", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Expanded legacy global fallback display
        Spacer(modifier = Modifier.height(8.dp))
        var isLegacyExpanded by remember { mutableStateOf(false) }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Filter Warisan/Global (Hanya jika rute kosong)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { isLegacyExpanded = !isLegacyExpanded }) {
                        Text(if (isLegacyExpanded) "Sembunyikan" else "Lihat Detail")
                    }
                }
                AnimatedVisibility(visible = isLegacyExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Fasilitas filter global jika Anda tidak mendefinisikan rute cerdas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Text("Aplikasi Dipantau Global: ${allowedPackages.joinToString(", ").ifEmpty { "Semua" }}", style = MaterialTheme.typography.bodySmall)
                        Text("Positive Keywords Global: ${positiveKeywords.joinToString(", ").ifEmpty { "Tidak ada" }}", style = MaterialTheme.typography.bodySmall)
                        Text("Negative Keywords Global: ${negativeKeywords.joinToString(", ").ifEmpty { "Tidak ada" }}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        RoutingProfileEditDialog(
            profile = selectedProfileForEdit,
            webhooks = webhooks,
            onDismiss = { showEditDialog = false },
            onSave = { updatedProfile ->
                if (updatedProfile.id == 0) {
                    viewModel.insertProfile(updatedProfile)
                } else {
                    viewModel.updateProfile(updatedProfile)
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun RoutingProfileEditDialog(
    profile: com.example.data.model.RoutingProfile?,
    webhooks: List<WebhookTarget>,
    onDismiss: () -> Unit,
    onSave: (com.example.data.model.RoutingProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var enabled by remember { mutableStateOf(profile?.enabled ?: true) }
    var priority by remember { mutableStateOf(profile?.priority?.toString() ?: "0") }
    var packagesRaw by remember { mutableStateOf(profile?.packagesRaw ?: "") }
    var positiveKeywordsRaw by remember { mutableStateOf(profile?.positiveKeywordsRaw ?: "") }
    var negativeKeywordsRaw by remember { mutableStateOf(profile?.negativeKeywordsRaw ?: "") }
    var customTemplate by remember { mutableStateOf(profile?.customTemplate ?: "") }
    var ttsEnabled by remember { mutableStateOf(profile?.ttsEnabled ?: false) }
    var ttsTemplate by remember { mutableStateOf(profile?.ttsTemplate ?: "") }
    
    var selectedWebhookIds by remember { 
        mutableStateOf(profile?.webhookTargetIds?.toSet() ?: emptySet()) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            com.example.data.model.RoutingProfile(
                                id = profile?.id ?: 0,
                                name = name.trim(),
                                enabled = enabled,
                                packagesRaw = packagesRaw.trim(),
                                positiveKeywordsRaw = positiveKeywordsRaw.trim(),
                                negativeKeywordsRaw = negativeKeywordsRaw.trim(),
                                webhookTargetIdsRaw = selectedWebhookIds.joinToString(","),
                                customTemplate = customTemplate.trim().ifEmpty { null },
                                ttsEnabled = ttsEnabled,
                                ttsTemplate = ttsTemplate.trim().ifEmpty { null },
                                priority = priority.toIntOrNull() ?: 0
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("btn_save_routing_profile")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        title = {
            Text(if (profile == null) "Tambah Profil Rute" else "Edit Profil Rute")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Rute (contoh: 'Keuangan Shopee')") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_routing_profile_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        modifier = Modifier.testTag("checkbox_routing_profile_enabled")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aktifkan rute ini")
                }

                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Prioritas Rute (Angka)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_routing_profile_priority")
                )

                OutlinedTextField(
                    value = packagesRaw,
                    onValueChange = { packagesRaw = it },
                    label = { Text("Package Dipantau (pisahkan dengan koma)") },
                    placeholder = { Text("com.gojek.app, id.dana") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = positiveKeywordsRaw,
                    onValueChange = { positiveKeywordsRaw = it },
                    label = { Text("Kata Kunci Positif (pisahkan dengan koma)") },
                    placeholder = { Text("berhasil, transfer masuk") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = negativeKeywordsRaw,
                    onValueChange = { negativeKeywordsRaw = it },
                    label = { Text("Kata Kunci Negatif (pisahkan dengan koma)") },
                    placeholder = { Text("promo, diskon") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("Target Webhook Rute Ini:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                if (webhooks.isEmpty()) {
                    Text("Belum ada target webhook global. Tambahkan pada tab Webhook dahulu.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else {
                    webhooks.forEach { wb ->
                        val isChecked = selectedWebhookIds.contains(wb.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedWebhookIds = if (isChecked) {
                                        selectedWebhookIds - wb.id
                                    } else {
                                        selectedWebhookIds + wb.id
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { chk ->
                                    selectedWebhookIds = if (isChecked) {
                                        selectedWebhookIds - wb.id
                                    } else {
                                        selectedWebhookIds + wb.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${wb.name} (${wb.url})", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Custom TTS Settings Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = ttsEnabled,
                        onCheckedChange = { ttsEnabled = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kustom Pengeras Suara TTS Rute Ini", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                if (ttsEnabled) {
                    OutlinedTextField(
                        value = ttsTemplate,
                        onValueChange = { ttsTemplate = it },
                        label = { Text("Template Suara Khusus Rute") },
                        placeholder = { Text("Duit masuk {amount} dari {app_name}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Custom JSON Template Section
                Text("Kustom JSON Payload (Opsional):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = customTemplate,
                    onValueChange = { customTemplate = it },
                    label = { Text("Template JSON") },
                    placeholder = { Text("{\n  \"nominal\": {amount},\n  \"app\": \"{app_name}\"\n}") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        modifier = Modifier.testTag("dialog_routing_profile_editor")
    )
}

@Composable
fun SpeakerSettingsTab(viewModel: QrisViewModel) {
    val ttsEnabled by viewModel.isSpeakerEnabled.collectAsState()
    val repeatCount by viewModel.speakerRepeat.collectAsState()
    val volume by viewModel.speakerVolume.collectAsState()
    val template by viewModel.speakerTemplate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pengeras Suara Pembayaran", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("Bacakan nominal pembayaran masuk layaknya mesin kasir speaker otomatis.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = ttsEnabled,
                    onCheckedChange = { viewModel.setSpeakerEnabled(it) },
                    modifier = Modifier.testTag("switch_speaker")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedVisibility(
            visible = ttsEnabled,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Template TTS
                Text("Format Suara Pengumuman", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = template,
                    onValueChange = { viewModel.setSpeakerTemplate(it) },
                    label = { Text("Template Teks Pembacaan") },
                    modifier = Modifier.fillMaxWidth().testTag("input_speaker_template")
                )
                Text(
                    "Variabel: {amount} (nominal rupiah), {app_name} (nama aplikasi pengirim).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Repetitions
                Text("Ulangi Suara", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(1, 2, 3).forEach { count ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.setSpeakerRepeat(count) }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = repeatCount == count,
                                onClick = { viewModel.setSpeakerRepeat(count) },
                                modifier = Modifier.testTag("radio_repeat_$count")
                            )
                            Text("$count Kali", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Volume slider
                Text("Volume Speaker: ${(volume * 100).toInt()}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = volume,
                    onValueChange = { viewModel.setSpeakerVolume(it) },
                    valueRange = 0.0f..1.0f,
                    modifier = Modifier.fillMaxWidth().testTag("slider_volume")
                )

                // Tester section
                Button(
                    onClick = { viewModel.testSpeak() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_test_speaker_tab"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tes Putar Pengumuman")
                }
            }
        }
    }
}

@Composable
fun HistoryTab(viewModel: QrisViewModel) {
    val notificationLogs by viewModel.logs.collectAsState()
    var selectedLog by remember { mutableStateOf<NotificationLog?>(null) }
    var targetDeliveries by remember { mutableStateOf<List<WebhookLog>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Riwayat Monitor QRIS", 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (notificationLogs.isNotEmpty()) {
                TextButton(
                    onClick = { showClearConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_clear_logs")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus Semua")
                }
            }
        }

        if (notificationLogs.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📭", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Belum Ada Mutasi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Menunggu notifikasi pembayaran masuk dari aplikasi m-banking atau e-wallet terpilih.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notificationLogs) { log ->
                    NotificationLogItem(
                        log = log,
                        onClick = {
                            selectedLog = log
                            coroutineScope.launch {
                                targetDeliveries = viewModel.getLogsForNotification(log.id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Hapus Semua Riwayat?") },
            text = { Text("Tindakan ini akan menghapus semua riwayat transaksi penangkapan notifikasi serta log pengiriman webhook lokal.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLogs()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Batal")
                }
            },
            modifier = Modifier.testTag("dialog_confirm_clear")
        )
    }

    if (selectedLog != null) {
        DetailLogDialog(
            log = selectedLog!!,
            deliveries = targetDeliveries,
            onResendWebhook = { delivery ->
                viewModel.resendWebhook(delivery) {
                    coroutineScope.launch {
                        targetDeliveries = viewModel.getLogsForNotification(selectedLog!!.id)
                    }
                }
            },
            onDismiss = { selectedLog = null }
        )
    }
}

@Composable
fun NotificationLogItem(
    log: NotificationLog,
    onClick: () -> Unit
) {
    val rupiahFormatter = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle icon fallback app branding
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                val letter = if (log.appName.isNotEmpty()) log.appName[0].uppercaseChar().toString() else "Q"
                Text(
                    text = letter,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(log.appName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = sdf.format(Date(log.timestamp)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = log.text, 
                    style = MaterialTheme.typography.bodySmall, 
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (log.amount != null) {
                    Text(
                        text = "Rp " + NumberFormat.getNumberInstance(Locale("id", "ID")).format(log.amount),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF81C784),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "Nominal tidak terbaca",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF938F99),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailLogDialog(
    log: NotificationLog,
    deliveries: List<WebhookLog>,
    onResendWebhook: (WebhookLog) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val sdf = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale("id", "ID")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detail Notifikasi Pembayaran") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Application and metadata
                DetailRow("Aplikasi", log.appName)
                DetailRow("Package", log.packageName)
                DetailRow("Waktu Deteksi", sdf.format(Date(log.timestamp)))
                
                DetailRow(
                    label = "Nominal Terdeteksi",
                    value = if (log.amount != null) "Rp " + NumberFormat.getNumberInstance(Locale("id", "ID")).format(log.amount) else "Tidak ditemukan"
                )

                DetailRow(
                    label = "Nama Pengirim",
                    value = log.sender ?: "Tidak terdeteksi (Umum)"
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("Isi Notifikasi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Title: ${log.title}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Body: ${log.text}", style = MaterialTheme.typography.bodySmall)
                        if (!log.bigText.isNullOrEmpty()) {
                             Spacer(modifier = Modifier.height(4.dp))
                             Text("BigText: ${log.bigText}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Log Pengiriman Webhook", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                if (deliveries.isEmpty()) {
                    Text("Belum dikirim ke webhook mana pun (atau webhook dinonaktifkan).", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    deliveries.forEach { delivery ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(delivery.webhookTargetName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    val color = when (delivery.status) {
                                        "sent" -> Color(0xFF81C784)
                                        "failed" -> Color(0xFFE57373)
                                        else -> Color(0xFFFFB74D)
                                    }
                                    Text(
                                        text = delivery.status.uppercase(),
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Text("URL: ${delivery.webhookTargetUrl}", fontSize = 10.sp, color = Color(0xFF938F99), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Response Code: ${delivery.responseCode ?: "N/A"}", fontSize = 10.sp, color = Color(0xFF938F99))
                                if (!delivery.errorMessage.isNullOrEmpty()) {
                                    Text("Errors: ${delivery.errorMessage}", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                }
                                Text("Jumlah Kirim Ulang: ${delivery.retryCount}", fontSize = 10.sp, color = Color(0xFF938F99))

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(delivery.payload))
                                            Toast.makeText(context, "Payload disalin!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Salin Payload", fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { onResendWebhook(delivery) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (delivery.status == "sent") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kirim Ulang", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        },
        modifier = Modifier.testTag("dialog_detail_log")
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsDialog(
    viewModel: QrisViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val deviceId by viewModel.deviceId.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfigurasi Sistem") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Section
                Column {
                    Text("Kode ID Perangkat (Device UUID):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = deviceId, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        val clipboard = LocalClipboardManager.current
                        TextButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(deviceId))
                                Toast.makeText(context, "UUID disalin!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Salin", fontSize = 11.sp)
                        }
                    }
                }

                // Autostart instructions again
                Column {
                    Text("Mode Background Sempurna:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Untuk hasil memantau terbaik 24 jam nonstop, pastikan layanan foreground aktif, nonaktifkan optimasi baterai via Dashboard, dan hidupkan autostart.",
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // App version parameters
                Column {
                    Text("Tentang Aplikasi:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text("Nama: QRIS Notification Bridge", style = MaterialTheme.typography.bodySmall)
                    Text("Versi: 1.0 (Native Release)", style = MaterialTheme.typography.bodySmall)
                    Text("Bahasa: Indonesia (100% Native Kotlin)", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        },
        modifier = Modifier.testTag("dialog_system_setting")
    )
}
