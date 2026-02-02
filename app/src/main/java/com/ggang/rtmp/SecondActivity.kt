package com.ggang.rtmp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.ggang.rtmp.ui.theme.GgangTMPTheme

class SecondActivity : ComponentActivity() {
    private val logTag = "yeoroooo-SecondActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(logTag, "onCreate")
        enableEdgeToEdge()
        setContent {
            GgangTMPTheme {
                val open = remember { mutableStateOf(true) }
                val text = remember { mutableStateOf("") }
                val focusRequester = remember { FocusRequester() }
                if (open.value) {
                    AlertDialog(
                        onDismissRequest = {
                            open.value = false
                            finish()
                        },
                        title = { Text(text = "Dialog") },
                        text = {
                            TextField(
                                value = text.value,
                                onValueChange = { text.value = it },
                                singleLine = true,
                                modifier = Modifier.focusRequester(focusRequester)
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    open.value = false
                                    finish()
                                }
                            ) {
                                Text(text = "확인")
                            }
                        }
                    )
                }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
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

    override fun onDestroy() {
        Log.d(logTag, "onDestroy isFinishing=$isFinishing")
        super.onDestroy()
    }
}
