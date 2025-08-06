package io.rebble.libpebblecommon.connection.bt.ble.transport.impl

import com.juul.kable.Filter
import com.juul.kable.Identifier
import com.juul.kable.Scanner
import io.rebble.libpebblecommon.connection.BleScanResult
import io.rebble.libpebblecommon.connection.PebbleBleIdentifier
import io.rebble.libpebblecommon.connection.bt.ble.transport.BleScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

fun kableBleScanner(): BleScanner = KableBleScanner()

class KableBleScanner : BleScanner {
    override fun scan(namePrefix: String?): Flow<BleScanResult> {
        return Scanner {
            filters {
                match {
//                    if (namePrefix != null) {
//                        name = Filter.Name.Prefix(namePrefix)
//                    }
                }
            }
        }.advertisements
            .mapNotNull {
                val name = it.name ?: return@mapNotNull null
                val manufacturerData = it.manufacturerData ?: return@mapNotNull null
                BleScanResult(
                    identifier = it.identifier.asPebbleBleIdentifier(),
                    name = name,
                    rssi = it.rssi,
                    manufacturerData = manufacturerData
                )
            }
    }
}

expect fun Identifier.asPebbleBleIdentifier(): PebbleBleIdentifier
