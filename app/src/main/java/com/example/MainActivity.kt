package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.service.ListenerForegroundService
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.view.QrisMainScreen
import com.example.ui.viewmodel.QrisViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: QrisViewModel by viewModels {
        QrisViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Request notifications permission on Android 13+ to cleanly render status controls.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        // Initialize active tracking state
        ListenerForegroundService.startService(this)

        setContent {
            MyApplicationTheme {
                QrisMainScreen(viewModel = viewModel)
            }
        }
    }
}
