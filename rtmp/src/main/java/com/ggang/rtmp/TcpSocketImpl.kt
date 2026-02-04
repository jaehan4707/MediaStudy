package com.ggang.rtmp

import java.nio.ByteBuffer

class TcpSocketImpl: TcpSocket {
    override var listener: TcpSocket.Listener? = null
    override fun connect(host: String, port: Int, isSecure: Boolean) {
        TODO("Not yet implemented")
    }

    override fun close(disconnected: Boolean) {
        TODO("Not yet implemented")
    }

    override fun enqueueWrite(buffer: ByteBuffer) {
        TODO("Not yet implemented")
    }

    override fun createByteBuffer(capacity: Int): ByteBuffer {
        TODO("Not yet implemented")
    }

}