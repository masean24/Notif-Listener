package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

object OemAutoStartHelper {

    fun getDeviceBrand(): String {
        return Build.BRAND.lowercase()
    }

    fun isOemDevice(): Boolean {
        val brand = getDeviceBrand()
        return brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") ||
                brand.contains("oppo") || brand.contains("realme") || brand.contains("vivo") ||
                brand.contains("huawei") || brand.contains("honor") || brand.contains("samsung") ||
                brand.contains("infinix")
    }

    fun getOemName(): String {
        val brand = getDeviceBrand()
        return when {
            brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> "Xiaomi / Redmi / POCO"
            brand.contains("oppo") -> "OPPO"
            brand.contains("realme") -> "Realme"
            brand.contains("vivo") -> "VIVO"
            brand.contains("huawei") || brand.contains("honor") -> "Huawei / Honor"
            brand.contains("samsung") -> "Samsung"
            brand.contains("infinix") -> "Infinix"
            else -> Build.MANUFACTURER
        }
    }

    fun getAutoStartInstructions(): String {
        val brand = getDeviceBrand()
        return when {
            brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> {
                "Untuk HP Xiaomi/Redmi/POCO, Anda harus mengizinkan Autostart agar sistem tidak mematikan monitor pembayaran:\n" +
                        "1. Klik tombol di bawah untuk membuka Kelola Autostart.\n" +
                        "2. Cari 'QRIS Notification Bridge' dan aktifkan saklarnya.\n" +
                        "3. Disarankan juga untuk mengatur Penghemat Baterai ke 'Tidak Ada Batasan'."
            }
            brand.contains("oppo") || brand.contains("realme") -> {
                "Untuk HP OPPO/Realme, Anda harus mengaktifkan Autostart:\n" +
                        "1. Klik tombol di bawah untuk membuka Startup Manager.\n" +
                        "2. Berikan izin Mulai Otomatis ke 'QRIS Notification Bridge'."
            }
            brand.contains("vivo") -> {
                "Untuk HP VIVO, Anda harus mengizinkan mulai otomatis secara manual:\n" +
                        "1. Klik tombol di bawah untuk membuka Pengelola Izin.\n" +
                        "2. Cari Autostart / Mulai Otomatis dan aktifkan izin untuk aplikasi ini."
            }
            brand.contains("huawei") -> {
                "Untuk HP Huawei, kelola aplikasi secara manual:\n" +
                        "1. Klik tombol di bawah untuk membuka Pengaturan Peluncuran.\n" +
                        "2. Nonaktifkan 'Kelola Secara Otomatis' untuk QRIS Notification Bridge, lalu aktifkan 'Mulai Otomatis' dan 'Jalankan di Latar Belakang'."
            }
            else -> {
                "Buka pengaturan optimasi sistem ponsel Anda:\n" +
                        "1. Masuk ke Pengaturan -> Aplikasi -> QRIS Notification Bridge.\n" +
                        "2. Aktifkan 'Mulai Otomatis' atau 'Autostart' jika tersedia.\n" +
                        "3. Ubah Optimasi Baterai menjadi 'Tidak Dibatasi' atau whitelist aplikasi."
            }
        }
    }

    fun openAutoStartSettings(context: Context): Boolean {
        val brand = getDeviceBrand()
        val intents = mutableListOf<Intent>()

        when {
            brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> {
                intents.add(Intent().apply {
                    component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                })
            }
            brand.contains("oppo") -> {
                intents.add(Intent().apply {
                    component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                })
                intents.add(Intent().apply {
                    component = ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")
                })
                intents.add(Intent().apply {
                    component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")
                })
            }
            brand.contains("realme") -> {
                intents.add(Intent().apply {
                    action = "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                })
            }
            brand.contains("vivo") -> {
                intents.add(Intent().apply {
                    component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                })
                intents.add(Intent().apply {
                    component = ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
                })
                intents.add(Intent().apply {
                    component = ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")
                })
            }
            brand.contains("huawei") -> {
                intents.add(Intent().apply {
                    component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
                })
                intents.add(Intent().apply {
                    component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
                })
            }
        }

        // Fallback to launch main settings
        intents.add(Intent(android.provider.Settings.ACTION_SETTINGS))

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                // Try next pattern
            }
        }
        return false
    }
}
