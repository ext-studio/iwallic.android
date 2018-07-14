package com.iwallic.app.models

import com.iwallic.neon.wallet.Wallet
import com.iwallic.neon.hex.Hex

class SmartContract {
    var scriptHash: String = ""
    var method: String = ""
    var args: ArrayList<Any> = arrayListOf()
    private var result: String = ""
    fun serielize(useTailCall: Boolean = false): String {
        result = ""
        args.forEach {
            when {
                it is Double -> {
                    addDouble(it)
                }
                it is Float -> {
                    addFloat(it)
                }
                it is Int -> {
                    addLong(it.toLong())
                }
                it is Long -> {
                    addLong(it)
                }
                it is String -> {
                    addString(it)
                }
                it is Boolean -> {
                    if (it) {
                        add(PUSHT)
                    } else {
                        add(PUSHF)
                    }
                }
                else -> {
                    return ""
                }
            }
        }
        return result
    }
    companion object {
        fun forNEP5(hash: String, from: String, to: String, value: Double): SmartContract {
            val sc = SmartContract()
            sc.scriptHash = hash
            sc.method = "transfer"
            sc.args = arrayListOf(value, Wallet.addr2Script(from), Wallet.addr2Script(to))
            return sc
        }
    }

    private fun add(op: Long, arg: String = "") {
        result += addLong(op)
        if (arg.isNotEmpty()) {
            addString(arg)
        }
    }
    private fun add(op: Int, arg: String = "") {
        result += addLong(op.toLong())
        if (arg.isNotEmpty()) {
            addString(arg)
        }
    }
    private fun addLong(value: Long) {
        when {
            value == (-1).toLong() -> {
                return add(PUSHM1)
            }
            value == (0).toLong() -> {
                return add(PUSH0)
            }
            value in 1..16 -> {
                return add((PUSH1 - 1).toLong() + value)
            }
            else -> {
                val hex = Hex.fromVarInt(value.toLong())
                return addString(Hex.reverse("0".repeat(16 - hex.length)) + hex)
            }
        }
    }
    private fun addDouble(value: Double) {
        addLong((value * 100000000).toLong())
    }
    private fun addFloat(value: Float) {
        addLong((value * 100000000).toLong())
    }
    private fun addString(value: String) {
        val size = value.length / 2
        when {
            size < PUSHBYTES75 -> {
                result += Hex.fromInt(size.toLong(), 1, false)
                result += value
                return
            }
            size < 0x100 -> {
                add(PUSHDATA1)
                result += Hex.fromInt(size.toLong(), 1, true)
                result += value
                return
            }
            size < 0x10000 -> {
                add(PUSHDATA2)
                result += Hex.fromInt(size.toLong(), 2, true)
                result += value
                return
            }
            size < 0x100000000 -> {
                add(PUSHDATA4)
                result += Hex.fromInt(size.toLong(), 4, true)
                result += value
                return
            }
            else -> {
                return
            }
        }
    }
}

const val PUSH0 = 0x00


const val PUSHF = 0x00 // PUSHF False
const val PUSHBYTES1 = 0x01  // 0x01-0x4B The next  bytes is data to be pushed onto the stack
const val PUSHBYTES75 = 0x4B
const val PUSHDATA1 = 0x4C // The next byte contains the number of bytes to be pushed onto the stack.
const val PUSHDATA2 = 0x4D // The next two bytes contain the number of bytes to be pushed onto the stack.
const val PUSHDATA4 = 0x4E // The next four bytes contain the number of bytes to be pushed onto the stack.
const val PUSHM1 = 0x4F // The number -1 is pushed onto the stack.
const val PUSH1 = 0x51 // The number 1 is pushed onto the stack.
const val PUSHT = 0x51
const val PUSH2 = 0x52 // The number 2 is pushed onto the stack.
const val PUSH3 = 0x53 // The number 3 is pushed onto the stack.
const val PUSH4 = 0x54 // The number 4 is pushed onto the stack.
const val PUSH5 = 0x55 // The number 5 is pushed onto the stack.
const val PUSH6 = 0x56 // The number 6 is pushed onto the stack.
const val PUSH7 = 0x57 // The number 7 is pushed onto the stack.
const val PUSH8 = 0x58 // The number 8 is pushed onto the stack.
const val PUSH9 = 0x59 // The number 9 is pushed onto the stack.
const val PUSH10 = 0x5A // The number 10 is pushed onto the stack.
const val PUSH11 = 0x5B // The number 11 is pushed onto the stack.
const val PUSH12 = 0x5C // The number 12 is pushed onto the stack.
const val PUSH13 = 0x5D // The number 13 is pushed onto the stack.
const val PUSH14 = 0x5E // The number 14 is pushed onto the stack.
const val PUSH15 = 0x5F // The number 15 is pushed onto the stack.
const val PUSH16 = 0x60 // The number 16 is pushed onto the stack.

    // Flow control
const val NOP = 0x61 // Does nothing.
const val JMP = 0x62
const val JMPIF = 0x63
const val JMPIFNOT = 0x64
const val CALL = 0x65
const val RET =0x66
const val APPCALL = 0x67
const val SYSCALL = 0x68
const val TAILCALL = 0x69

    // Stack
const val DUPFROMALTSTACK = 0x6A
const val TOALTSTACK = 0x6B // Puts the input onto the top of the alt stack. Removes it from the main stack.
const val FROMALTSTACK = 0x6C // Puts the input onto the top of the main stack. Removes it from the alt stack.
const val XDROP   = 0x6D
const val XSWAP  =  0x72
const val XTUCK  =  0x73
const val DEPTH  =   0x74 // Puts the number of stack items onto the stack.
const val DROP   =   0x75 // Removes the top stack item.
const val DUP   =    0x76 // Duplicates the top stack item.
const val NIP  =     0x77 // Removes the second-to-top stack item.
const val OVER = 0x78 // Copies the second-to-top stack item to the top.
const val ROLL = 0x7A // The item n back in the stack is moved to the top.
const val PICK = 0x79 // The item n back in the stack is copied to the top.
const val ROT = 0x7B // The top three items on the stack are rotated to the left.
const val SWAP = 0x7C // The top two items on the stack are swapped.
const val TUCK = 0x7D // The item at the top of the stack is copied and inserted before the second-to-top item.

    // Splice
const val CAT = 0x7E // Concatenates two strings.
const val SUBSTR = 0x7F // Returns a section of a string.
const val LEFT = 0x80 // Keeps only characters left of the specified point in a string.
const val RIGHT = 0x81 // Keeps only characters right of the specified point in a string.
const val SIZE = 0x82 // Returns the length of the input string.

    // Bitwise logic
const val INVERT = 0x83 // Flips all of the bits in the input.
const val AND = 0x84 // Boolean and between each bit in the inputs.
const val OR  = 0x85 // Boolean or between each bit in the inputs.
const val XOR = 0x86 // Boolean exclusive or between each bit in the inputs.
const val EQUAL  = 0x87 // Returns 1 if the inputs are exactly equal 0 otherwise.
    //OP_EQUALVERIFY  = 0x88 // Same as OP_EQUAL but runs OP_VERIFY afterward.
    //OP_RESERVED1  = 0x89 // Transaction is invalid unless occuring in an unexecuted OP_IF branch
    //OP_RESERVED2  = 0x8A // Transaction is invalid unless occuring in an unexecuted OP_IF branch

    // Arithmetic
    // Note: Arithmetic inputs are limited to signed 32-bit integers but may overflow their output.
const val INC   = 0x8B // 1 is added to the input.
const val DEC  = 0x8C // 1 is subtracted from the input.
const val SIGN  = 0x8D
const val NEGATE  = 0x8F // The sign of the input is flipped.
const val ABS   = 0x90 // The input is made positive.
const val NOT  = 0x91 // If the input is 0 or 1 it is flipped. Otherwise the output will be 0.
const val NZ = 0x92 // Returns 0 if the input is 0. 1 otherwise.
const val ADD  = 0x93 // a is added to b.
const val SUB  = 0x94 // b is subtracted from a.
const val MUL   = 0x95 // a is multiplied by b.
const val DIV   = 0x96 // a is divided by b.
const val MOD  = 0x97 // Returns the remainder after dividing a by b.
const val SHL   = 0x98 // Shifts a left b bits preserving sign.
const val SHR   = 0x99 // Shifts a right b bits preserving sign.
const val BOOLAND  = 0x9A // If both a and b are not 0 the output is 1. Otherwise 0.
const val BOOLOR   = 0x9B // If a or b is not 0 the output is 1. Otherwise 0.
const val NUMEQUAL = 0x9C // Returns 1 if the numbers are equal 0 otherwise.
const val NUMNOTEQUAL = 0x9E // Returns 1 if the numbers are not equal 0 otherwise.
const val LT   = 0x9F // Returns 1 if a is less than b 0 otherwise.
const val GT   = 0xA0 // Returns 1 if a is greater than b 0 otherwise.
const val LTE  = 0xA1 // Returns 1 if a is less than or equal to b 0 otherwise.
const val GTE  = 0xA2// Returns 1 if a is greater than or equal to b 0 otherwise.
const val MIN  = 0xA3 // Returns the smaller of a and b.
const val MAX  = 0xA4 // Returns the larger of a and b.
const val WITHIN  = 0xA5 // Returns 1 if x is within the specified rangeleft-inclusive) 0 otherwise.

    // Crypto
    //RIPEMD160  = 0xA6 // The input is hashed using RIPEMD-160.
const val SHA1  = 0xA7 // The input is hashed using SHA-1.
const val SHA256  = 0xA8 // The input is hashed using SHA-256.
const val HASH160  = 0xA9
const val HASH256  = 0xAA
const val CHECKSIG = 0xAC
const val CHECKMULTISIG = 0xAE

    // Array
const val ARRAYSIZE = 0xC0
const val PACK  = 0xC1
const val UNPACK = 0xC2
const val PICKITEM  = 0xC3
const val SETITEM = 0xC4
const val NEWARRAY  = 0xC5 //用作引用類型
const val NEWSTRUCT = 0xC6 //用作值類型
const val APPEND = 0xC8
const val REVERSE = 0xC9
const val REMOVE = 0xCA

    // Exceptions
const val THROW   = 0xF0
const val THROWIFNOT = 0xF1
