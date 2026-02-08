package com.ggang.rtmp

import android.util.Log
import com.ggang.rtmp.data.HandshakeState
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

    private fun setSessionState(state: Boolean) {
        _sessionState.value = state
    }

    fun connect(host: String, port: Int, isSecure: Boolean) {
        clearSocket()

        socket = TcpSocketImpl()
        socket?.listener = this
        socket?.connect(host, port, isSecure)
    }

    fun enqueueWrite(buffer: ByteBuffer) {
        socket?.enqueueWrite(buffer)
    }

    fun createByBuffer(capacity: Int): ByteBuffer = socket?.createByteBuffer(capacity) ?: ByteBuffer.allocate(capacity)

    fun close(disconnected: Boolean) {
        if (!isConnected) return
        socket?.close(disconnected)
        setReadyState(HandshakeState.Closing)

        // TODO connection 종료 보내기

        setReadyState(HandshakeState.Closed)
        setSessionState(false)
    }

    /**
    * -----------------------
    * */

    override fun onConnect() {
        handShake.clear()
        setReadyState(HandshakeState.VersionSent)
        socket?.enqueueWrite(handShake.c0Packet)
        socket?.enqueueWrite(handShake.c1Packet)
    }

    override fun onDataReceived(buffer: ByteBuffer) {
        when (readyState.value) {
            HandshakeState.VersionSent -> {
                Log.d("HandshakeState.VersionSent", "yeorooo 버퍼 내용 ${buffer}")

                if (buffer.limit() < RtmpHandshake.SIGNAL_SIZE + 1) {
                    return
                }
                handShake.s0Packet = buffer
                handShake.s1Packet = buffer
                socket?.enqueueWrite(handShake.c2Packet)
                buffer.position(RtmpHandshake.SIGNAL_SIZE + 1)
                setReadyState(HandshakeState.AckSent)

                if (buffer.limit() - buffer.position() == RtmpHandshake.SIGNAL_SIZE) {
                    onDataReceived(buffer.slice())
                    buffer.position(3073)
                }
            }
            HandshakeState.AckSent -> {
                Log.d("HandshakeState.AckSent", "yeorooo 버퍼 내용 ${buffer}")

                if (buffer.limit() < RtmpHandshake.SIGNAL_SIZE) {
                    return
                }
                handShake.s2Packet = buffer
                buffer.position(RtmpHandshake.SIGNAL_SIZE)
                setReadyState(HandshakeState.HandshakeDone)
                setSessionState(true)

                // TODO connection 연결 보내기
            }
            HandshakeState.HandshakeDone -> {
                Log.d("HandshakeState.HandshakeDone", "yeorooo 버퍼 내용 ${buffer}")
            }
            else -> {}
        }
    }

    override fun onClose(disconnected: Boolean) {
        close(disconnected)
    }

    override fun onError() {
        close(false)

        // TODO connection 에러 보내기
    }

    private fun clearSocket() {
        socket?.listener = null
        socket = null
    }

    companion object {
        private val DEFAULT_SIZE = 128
    }
}
