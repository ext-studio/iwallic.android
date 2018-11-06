package com.iwallic.neon.utils

import android.util.Log
import java.security.MessageDigest
import kotlin.experimental.xor
import kotlin.math.pow

object HEX {
    fun hash256(src: String): String {
        val enc1 = MessageDigest
                .getInstance("SHA-256")
                .digest(src.toByteArray())
        val enc2 = MessageDigest
                .getInstance("SHA-256")
                .digest(enc1)
        return enc2.reversedArray().toString()
    }
    fun fromString(src: String): String {
        var rs = ""
        src.forEach {
            rs += fromInt(it.toLong(), 1)
        }
        return rs
    }
    fun int2HexInt(src: Long): String {
        if (src < 0) {
            throw Throwable("Not support negative value")
        }
        val rs = src.toString(16)
        return if (rs.length%2 == 1) "0$rs" else rs
    }
    fun toFixedNum(src: Double, decimal: Int = 8): String {
        if (src > 100000000) {
            throw Throwable("Not support big value")
        }
        val target = int2HexInt((src * 10.0.pow(decimal)).toLong())
        return reverse("0".repeat(16-target.length)+target)
    }
    fun fromVarInt(src: Long): String {
        return when {
            src < 0xfd -> {
                fromInt(src, 1)
            }
            src <= 0xffff -> {
                "fd" + fromInt(src, 2, true)
            }
            src <= 0xffffffff -> {
                "fe" + fromInt(src, 4, true)
            }
            else -> {
                "ff" + fromInt(src, 8, true)
            }
        }
    }
    fun xor(hex1: String, hex2: String): String {
        val byte1 = hex1.toByte(16)
        val byte2 = hex2.toByte(16)
        return byte1.xor(byte2).toString(16)
    }
    fun fromInt(src: Long, _size: Int, littleEnd: Boolean = false): String {
        if (src < 0) {
            return ""
        }
        val size = _size * 2
        var hex = src.toString(16)
        if (hex.length%size != 0) {
            hex = ("0".repeat(size) + hex).substring(hex.length)
        }
        return if (littleEnd) {
            reverse(hex)
        } else {
            hex
        }
    }
    fun reverse(_src: String): String {
        var src = _src
        if (src.startsWith("0x")) {
            src = src.substring(2)
        }
        return src.chunked(2).joinToString("")
    }

    private fun String.chunked(size: Int): List<String> {
        val nChunks = length / size
        return (0 until nChunks).map { substring(it * size, (it + 1) * size) }
    }
}
