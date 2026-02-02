package com.ggang.rtmp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.os.Build
import android.provider.Settings
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ggang.rtmp.ui.theme.GgangTMPTheme

class MainActivity : ComponentActivity() {
    private val logTag = "yeoroooo-MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(logTag, "onCreate")
        enableEdgeToEdge()
        setContent {
            GgangTMPTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        onLaunch = { startServiceWithOverlayPermission() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(logTag, "onDestroy isFinishing=$isFinishing")
        if (isFinishing) {
            stopService(Intent(this, LaunchActivityService::class.java))
        }
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        Log.d(logTag, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag, "onResume")
    }

    override fun onPause() {
        Log.d(logTag, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(logTag, "onStop")
        super.onStop()
    }

    private fun startServiceWithOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }
        LaunchActivityService.start(this)
        moveTaskToBack(true)
    }
}

@Composable
fun MainScreen(onLaunch: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Main Activity")
        Button(onClick = onLaunch) {
            Text(text = "Start service and open Second Activity")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GgangTMPTheme {
        MainScreen(onLaunch = {})
    }
}
