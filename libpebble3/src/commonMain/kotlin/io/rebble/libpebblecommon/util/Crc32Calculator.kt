package io.rebble.libpebblecommon.util

/**
 * CRC32 hash Calculator that is compatible with hardware CRC32 on the STM chips.
 */
class Crc32Calculator {
    private var finalized = false
    private var value: UInt = 0xFFFFFFFFu

    private var leftoverBytes = UByteArray(0)

    fun addBytes(bytes: UByteArray) {
        if (finalized) {
            throw IllegalStateException("Cannot add more bytes to finalized CRC calculation")
        }

        val mergedArray = leftoverBytes + bytes
        val buffer = DataBuffer(mergedArray)
        buffer.setEndian(Endian.Little)

        val finalPosition = mergedArray.size - mergedArray.size % 4
        while (buffer.readPosition < finalPosition) {
            addInt(buffer.getUInt())
        }

        leftoverBytes = mergedArray.copyOfRange(finalPosition, mergedArray.size)
    }

    /**
     * Finalizes the calculation and returns the CRC32 result
     */
    fun finalize(): UInt {
        if (finalized) {
            return value
        }

        if (leftoverBytes.isNotEmpty()) {
            leftoverBytes = leftoverBytes.padZerosLeft(4 - leftoverBytes.size).reversedArray()
            addInt(DataBuffer(leftoverBytes).apply { setEndian(Endian.Little) }.getUInt())
        }

        finalized = true
        return value
    }

    private fun addInt(valueToAdd: UInt) {
        this.value = this.value xor valueToAdd

        for (i in 0 until 32) {
            if ((this.value and 0x80000000u) != 0u) {
                this.value = (this.value shl 1) xor 0x04C11DB7u
            } else {
                this.value = this.value shl 1
            }
        }

        this.value = this.value and 0xFFFFFFFFu
    }
}

private fun UByteArray.padZerosLeft(amount: Int): UByteArray {
    return UByteArray(amount) { 0u } + this
}