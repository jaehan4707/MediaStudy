package com.ggang.rtmp

import android.util.Log
import androidx.core.util.Pools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketException
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingDeque
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.coroutines.CoroutineContext

class TcpSocketImpl: TcpSocket, CoroutineScope {
    override var listener: TcpSocket.Listener? = null
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    private var socket: Socket? = null
    private var inputBuffer = ByteBuffer.allocate(DEFAULT_WINDOW_SIZE_C)
    private var inputStream: InputStream? = null
        set(value) {
            if (value == null) field?.close()
            field = value
        }
    private var outputStream: OutputStream? = null
        set(value) {
            if (value == null) field?.close()
            field = value
        }
    private var outputQueue = LinkedBlockingDeque<ByteBuffer>()
    private var outputBufferPool = Pools.SimplePool<ByteBuffer>(1024)

    @Volatile
    private var keepAlive = false

    override fun connect(host: String, port: Int, isSecure: Boolean) {
        if (socket?.isConnected == true) {
            return
        }
        keepAlive = true
        launch(coroutineContext) {
            outputQueue.clear()

            try {
                socket = createSocket(host, port, isSecure)
            } catch (e: Exception) {
                Log.e(TAG, "yeorooo $e")
                close(true)
                listener?.onError()
            }

            try {
                val socket = socket ?: return@launch

                inputStream = socket.getInputStream()
                outputStream = socket.getOutputStream()

                launch(Dispatchers.IO) {
                    doOutput(socket)
                }

                listener?.onConnect()
                while (keepAlive) {
                    doInput()
                    try {
                        Thread.sleep(KEEP_ALIVE_SLEEP_INTERVAL)
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "yeorooo $e")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "yeorooo $e")
                close(true)
            }
        }
    }

    private fun createSocket(host: String, port: Int, isSecure: Boolean): Socket {
        if (isSecure) {
            val socket = SSLSocketFactory.getDefault().createSocket(host, port) as SSLSocket
            socket.startHandshake()
            return socket
        }
        return Socket(host, port)
    }

    override fun close(disconnected: Boolean) {
        if (socket?.isClosed == true) {
            return
        }
        keepAlive = false
        inputStream = null
        outputStream = null
        socket?.close()
        outputQueue.add(ByteBuffer.allocate(0))
        listener?.onClose(disconnected)
    }

    override fun enqueueWrite(buffer: ByteBuffer) {
        try {
            val src = buffer.duplicate()
            if(src.limit() == src.capacity() && src.position() > 0) {
                src.flip()
            }

            val copy = ByteBuffer.allocate(src.remaining())
            copy.put(src)
            copy.flip()
            outputQueue.put(copy)
        } catch (e: Exception) {
            Log.e(TAG, "$e")
        }
    }

    override fun createByteBuffer(capacity: Int): ByteBuffer {
        TODO("Not yet implemented")
    }

    private fun doOutput(socket: Socket) {
        while (keepAlive) {
            val buffer = outputQueue.take()
            if(socket.isClosed) {
                break
            }

            try {
                val outputStream = outputStream ?: break
                val remaining = buffer.remaining().toLong()
                Log.d(TAG, "yeorooo $remaining")


                val payload = ByteArray(buffer.remaining())
                buffer.get(payload)
                outputStream.write(payload)
                outputStream.flush()

                synchronized(outputBufferPool) {
                    outputBufferPool.release(buffer)
                }
            } catch(e: Exception) {
                Log.e(TAG, "yeorooo $e")
                close(false)
            }
        }
    }

    private fun doInput() {
        try {
            val inputStream = inputStream ?: return
            val offset = inputBuffer.position()
            val result = inputStream.read(inputBuffer.array(), offset, inputBuffer.remaining())
            if (result > -1) {
                inputBuffer.position(offset + result)
                inputBuffer.flip()
                listener?.onDataReceived(inputBuffer)
                if (inputBuffer.hasRemaining()) {
                    val remaining = inputBuffer.slice()
                    inputBuffer.clear()
                    inputBuffer.put(remaining)
                } else {
                    inputBuffer.clear()
                }
            } else {
                close(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "yeorooo $e")
            close(true)
        }
    }

    companion object {
        private const val DEFAULT_WINDOW_SIZE_C = Short.MAX_VALUE.toInt() * 5
        private const val KEEP_ALIVE_SLEEP_INTERVAL = 10L
        private val TAG = TcpSocketImpl::class.java.simpleName
    }
}
