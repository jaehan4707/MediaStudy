package com.ggang.rtmp

import java.nio.ByteBuffer

interface TcpSocket {
    interface Listener {
        fun onDataReceived(buffer: ByteBuffer)

        fun onConnect()

        fun onClose(disconnected: Boolean)

        fun onError()
    }

    var listener: Listener?

    fun connect(host: String, port: Int, isSecure: Boolean)

    fun close(disconnected: Boolean)

    fun enqueueWrite(buffer: ByteBuffer)

    fun createByteBuffer(capacity: Int): ByteBuffer
}