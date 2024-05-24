package io.rebble.libpebblecommon.util
import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy
import platform.posix.size_t

actual fun runBlocking(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking{block()}
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal fun isPlatformBigEndian(): Boolean {
    memScoped {
        val i = alloc<IntVar>()
        i.value = 1
        val bytes = i.reinterpret<ByteVar>()
        return bytes.value == 0.toByte()
    }
}

internal fun reverseOrd(varr: UShort): UShort = (((varr.toInt() and 0xff) shl 8) or ((varr.toInt() and 0xffff) ushr 8)).toUShort()

internal fun reverseOrd(varr: UInt): UInt = ((reverseOrd((varr and 0xffffu).toUShort()).toInt() shl 16) or (reverseOrd((varr shr 16).toUShort()).toInt() and 0xffff)).toUInt()

internal fun reverseOrd(varr: ULong): ULong = ((reverseOrd((varr and 0xffffffffu).toUInt()).toLong() shl 32) or (reverseOrd((varr shr 32).toUInt()).toLong() and 0xffffffff)).toULong()

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
fun ByteArray.toNative(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNative), length = castToNativeSize(this@toNative.size))
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
fun KUtil.byteArrayFromNative(arr: NSData): ByteArray = ByteArray(arr.length.toInt()).apply {
    if (this.isNotEmpty()) {
        usePinned {
            memcpy(it.addressOf(0), arr.bytes, arr.length)
        }
    }
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal fun castToNativeSize(v: Int): size_t {
    @Suppress("CAST_NEVER_SUCCEEDS", "USELESS_CAST") // Depending on the platform different side of if will trigger, lets ignore
    return if (size_t.SIZE_BITS == 32) {
        v.toUInt() as size_t
    }else {
        v.toULong() as size_t
    }
}