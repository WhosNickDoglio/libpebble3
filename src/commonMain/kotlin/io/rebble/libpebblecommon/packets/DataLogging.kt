package io.rebble.libpebblecommon.packets

import io.rebble.libpebblecommon.protocolhelpers.PacketRegistry
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import io.rebble.libpebblecommon.structmapper.SBytes
import io.rebble.libpebblecommon.structmapper.SUByte
import io.rebble.libpebblecommon.structmapper.SUInt
import io.rebble.libpebblecommon.structmapper.SUShort
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.SUnboundBytes
import io.rebble.libpebblecommon.util.Endian

/**
 * Data logging packet. Little endian.
 */
sealed class DataLoggingIncomingPacket : PebblePacket(ProtocolEndpoint.DATA_LOG) {
    val command = SUByte(m)

    class OpenSession : DataLoggingIncomingPacket() {
        val sessionId = SUByte(m)
        val applicationUUID = SUUID(m)
        val timestamp = SUInt(m, endianness = endianness)
        val tag = SUInt(m, endianness = endianness)
        val dataItemTypeId = SUByte(m)
        val dataItemType: DataItemType get() = DataItemType.fromValue(dataItemTypeId.get())
        val dataItemSize = SUShort(m, endianness = endianness)
    }

    class SendDataItems : DataLoggingIncomingPacket() {
        val sessionId = SUByte(m)
        val itemsLeftAfterThis = SUInt(m, endianness = endianness)
        val crc = SUInt(m, endianness = endianness)
        val payload = SUnboundBytes(m)
    }

    class CloseSession : DataLoggingIncomingPacket() {
        val sessionId = SUByte(m)
    }

    class Timeout : DataLoggingIncomingPacket() {
        val sessionId = SUByte(m)
    }

    class SendEnabledResponse : DataLoggingIncomingPacket() {
        val sendEnabledValue = SUByte(m)
        val sendEnabled: Boolean get() = sendEnabledValue.get() != 0u.toUByte()
    }
}

/**
 * Data logging packet. Little endian.
 */
sealed class DataLoggingOutgoingPacket(command: DataLoggingCommand) : PebblePacket(ProtocolEndpoint.DATA_LOG) {
    val command = SUByte(m, command.value)

    class ReportOpenSessions(sessionIds: List<Byte>) : DataLoggingOutgoingPacket(DataLoggingCommand.ReportOpenSessions) {
        val sessions = SBytes(m, sessionIds.size, sessionIds.toByteArray().asUByteArray())
    }

    class ACK(sessionId: UByte) : DataLoggingOutgoingPacket(DataLoggingCommand.ACK) {
        val sessionId = SUByte(m, sessionId)
    }

    class NACK(sessionId: UByte) : DataLoggingOutgoingPacket(DataLoggingCommand.NACK) {
        val sessionId = SUByte(m, sessionId)
    }

    class DumpAllData : DataLoggingOutgoingPacket(DataLoggingCommand.DumpAllData) {
        val sessionId = SUByte(m)
    }

    class GetSendEnabled : DataLoggingOutgoingPacket(DataLoggingCommand.GetSendEnabled)

    class SetSendEnabled(enabled: Boolean) : DataLoggingOutgoingPacket(DataLoggingCommand.SetSendEnabled) {
        val sendEnabled = SUByte(m, if (enabled) 1u else 0u)
    }
}

private val endianness = Endian.Little

enum class DataLoggingCommand(val value: UByte) {
    OpenSession(0x01u),
    SendDataItems(0x02u),
    CloseSession(0x03u),
    ReportOpenSessions(0x84u),
    ACK(0x85u),
    NACK(0x86u),
    Timeout(0x07u),
    DumpAllData(0x88u),
    GetSendEnabled(0x89u),
    SendEnabledResponse(0x0Au),
    SetSendEnabled(0x8Bu),
}

enum class DataItemType(val value: UByte) {
    ByteArray(0x00u),
    UInt(0x01u),
    Int(0x02u),
    Invalid(0xFFu);

    companion object {
        fun fromValue(value: UByte): DataItemType {
            return entries.find { it.value == value } ?: Invalid
        }
    }
}

fun dataLoggingPacketsRegister() {
    PacketRegistry.register(ProtocolEndpoint.DATA_LOG, DataLoggingCommand.OpenSession.value) {
        DataLoggingIncomingPacket.OpenSession()
    }
    PacketRegistry.register(ProtocolEndpoint.DATA_LOG, DataLoggingCommand.SendDataItems.value) {
        DataLoggingIncomingPacket.SendDataItems()
    }
    PacketRegistry.register(ProtocolEndpoint.DATA_LOG, DataLoggingCommand.CloseSession.value) {
        DataLoggingIncomingPacket.CloseSession()
    }
    PacketRegistry.register(ProtocolEndpoint.DATA_LOG, DataLoggingCommand.Timeout.value) {
        DataLoggingIncomingPacket.Timeout()
    }
    PacketRegistry.register(ProtocolEndpoint.DATA_LOG, DataLoggingCommand.SendEnabledResponse.value) {
        DataLoggingIncomingPacket.SendEnabledResponse()
    }
}