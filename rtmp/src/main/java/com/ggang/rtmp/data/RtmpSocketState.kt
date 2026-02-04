package com.ggang.rtmp.data

sealed interface HandshakeState {
    data object Uninitialized: HandshakeState   // 연결 전
    data object VersionSent: HandshakeState     // C0/C1 send + S0/S1 wait
    data object AckSent: HandshakeState         // S0/S1 receive + C2 send + S2 wait
    data object HandshakeDone: HandshakeState   // S2 receive
    data object Closing: HandshakeState         // 종료 요청
    data object Closed: HandshakeState          // 종료
}