package com.ggang.rtmp

import java.nio.ByteBuffer
import kotlin.random.Random

internal class RtmpHandshake {

    var c0Packet: ByteBuffer = ByteBuffer.allocate(1)
        get() {
            if (field.isInitialization()) {
                field.put(0x03) //RTMP 버전을 보냄
            }
            return field
        }

    var c1Packet: ByteBuffer = ByteBuffer.allocate(SIGNAL_SIZE)
        get() {
            if (field.isInitialization()) {
                val random = Random(System.currentTimeMillis())
                field.position(4) // TIME
                field.position(8) // ZERO
                repeat(RANDOM_DATA_BUFFER_SIZE) { //RANDOM
                    field.put(random.nextInt().toByte())
                }
            }
            return field
        }

    var c2Packet: ByteBuffer = ByteBuffer.allocate(SIGNAL_SIZE)
        set(value) {
            //C2는 S1 그대로 복사
            field = ByteBuffer.wrap(value.array(), 0, SIGNAL_SIZE)
            val s1Array = s1Packet.array()
            val c2Array = value.array()
            require(s1Array.contentEquals(c2Array)) {
                "C2 패킷은 S1과 같아야 합니다."
            }
        }

    var s0Packet: ByteBuffer = ByteBuffer.allocate(1)
        set(value) {
            // 서버에서 RTMP 버전을 명시해준다.
            field = ByteBuffer.wrap(value.array(), 0, 1)
        }
    var s1Packet: ByteBuffer = ByteBuffer.allocate(SIGNAL_SIZE)
        set(value) {
            field = ByteBuffer.wrap(value.array(), 0, SIGNAL_SIZE)
            // S1을 받으면 C2를 생성 (S1의 echo)
            c2Packet = value
        }

    var s2Packet: ByteBuffer = ByteBuffer.allocate(SIGNAL_SIZE)
        set(value) {
            field = ByteBuffer.wrap(value.array(), 0, SIGNAL_SIZE)
            //S2는 C1 그대로 복사
            val c1Array = c1Packet.array()
            val s2Array = value.array()
            require(c1Array.contentEquals(s2Array)) {
                "S2 패킷은 C1과 같아야 합니다."
            }
        }

    fun clear() {
        s0Packet.clear()
        s1Packet.clear()
        s2Packet.clear()
        c0Packet.clear()
        c1Packet.clear()
        c2Packet.clear()
    }

    companion object {
        const val SIGNAL_SIZE = 1536
        const val RANDOM_DATA_BUFFER_SIZE = 1528
    }
}

internal fun ByteBuffer.isInitialization() = this.position() == 0