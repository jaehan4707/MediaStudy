package com.ggang.rtmp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.ggang.rtmp.ui.theme.GgangTMPTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

class MainActivity : ComponentActivity() {
    private val rtmpSocket: RtmpSocket by lazy {
        RtmpSocket()
    }

    private val streamKey = "rtmp://stream.soop.live/app/yeo2507-1083619274"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GgangTMPTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        onClick = { requestConnection() }
                    )
                }
            }
        }
    }

    private fun requestConnection() {
        val uri = runCatching { URI.create(streamKey) }
            .getOrElse {
                Log.e(TAG, "Invalid stream key: $streamKey", it)
                return
            }

        val host = uri.host
        if (host.isNullOrBlank()) {
            Log.e(TAG, "Missing host in stream key: $streamKey")
            return
        }

        val port = if (uri.port == -1) DEFAULT_RTMP_PORT else uri.port
        val isSecure = uri.scheme.equals("rtmps", ignoreCase = true)

        lifecycleScope.launch(Dispatchers.IO) {
            runCatching {
                rtmpSocket.connect(host, port, isSecure)
            }.onFailure { error ->
                // rtmp 모듈 미구현(TODO) 상태에서도 app이 즉시 죽지 않도록 방어
                Log.e(TAG, "RTMP connection request failed", error)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val DEFAULT_RTMP_PORT = 1935
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello $name!")
        androidx.compose.material3.Button(onClick = onClick) {
            Text(text = "Click to Connect")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GgangTMPTheme {
        Greeting("Android")
    }
}
