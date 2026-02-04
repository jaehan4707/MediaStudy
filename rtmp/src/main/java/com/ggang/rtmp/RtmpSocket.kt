package com.ggang.rtmp

import com.ggang.rtmp.data.HandshakeState
import com.ggang.rtmp.data.SocketState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer

class RtmpSocket: TcpSocket.Listener {
    private val handShake: RtmpHandshake by lazy {
        RtmpHandshake()
    }

    private var socket: TcpSocket? = null

    private val _readyState = MutableStateFlow<HandshakeState>(HandshakeState.Uninitialized)
    val readyState: StateFlow<HandshakeState> = _readyState.asStateFlow()

    private val _sessionState = MutableStateFlow(false)
    val sessionState: StateFlow<Boolean> = _sessionState.asStateFlow()

    var isConnected: Boolean
        get() = sessionState.value
        set(value) { _sessionState.value = value }

    private fun setReadyState(state: HandshakeState) {
        _readyState.value = state
    }

    fun connect(host: String, port: Int, isSecure: Boolean) {
        clearSocket()

        socket = TcpSocketImpl()
        socket?.listener = this
        socket?.connect(host, port, isSecure)
    }

    fun close(disconnected: Boolean) {
        if (!isConnected) return
    }

    fun enqueueWrite(buffer: ByteBuffer) {
        socket?.enqueueWrite(buffer)
    }

    fun createByBuffer(capacity: Int): ByteBuffer = socket?.createByteBuffer(capacity) ?: ByteBuffer.allocate(capacity)

    override fun onDataReceived(buffer: ByteBuffer) {
        TODO("Not yet implemented")
    }

    override fun onConnect() {
        TODO("Not yet implemented")
    }

    override fun onClose(disconnected: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onError() {
        TODO("Not yet implemented")
    }

    private fun clearSocket() {
        socket?.listener = null
        socket = null
    }

    companion object {
        private val DEFAULT_SIZE = 128
    }
}