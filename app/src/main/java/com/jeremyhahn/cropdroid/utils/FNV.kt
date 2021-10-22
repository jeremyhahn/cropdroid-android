package com.jeremyhahn.cropdroid.utils

import java.math.BigInteger

class FNV {

    companion object {
        private val INIT32 = BigInteger("811c9dc5", 16)
        private val INIT64 = BigInteger("cbf29ce484222325", 16)
        private val PRIME32 = BigInteger("01000193", 16)
        private val PRIME64 = BigInteger("100000001b3", 16)
        private val MOD32 = BigInteger("2").pow(32)
        private val MOD64 = BigInteger("2").pow(64)

        fun fnv1_32(data: ByteArray): BigInteger {
            var hash = INIT32
            for (b in data) {
                hash = hash.multiply(PRIME32).mod(MOD32)
                hash = hash.xor(BigInteger.valueOf((b.toInt() and 0xff).toLong()))
            }
            return hash
        }

        fun fnv1_64(data: ByteArray): BigInteger {
            var hash = INIT64
            for (b in data) {
                hash = hash.multiply(PRIME64).mod(MOD64)
                hash = hash.xor(BigInteger.valueOf((b.toInt() and 0xff).toLong()))
            }
            return hash
        }

        fun fnv1a_32(data: ByteArray): BigInteger {
            var hash = INIT32
            for (b in data) {
                hash = hash.xor(BigInteger.valueOf((b.toInt() and 0xff).toLong()))
                hash = hash.multiply(PRIME32).mod(MOD32)
            }
            return hash
        }

        fun fnv1a_64(data: ByteArray): BigInteger {
            var hash = INIT64
            for (b in data) {
                hash = hash.xor(BigInteger.valueOf((b.toInt() and 0xff).toLong()))
                hash = hash.multiply(PRIME64).mod(MOD64)
            }
            return hash
        }
    }
}