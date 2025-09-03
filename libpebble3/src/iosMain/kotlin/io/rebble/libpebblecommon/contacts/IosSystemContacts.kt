package io.rebble.libpebblecommon.io.rebble.libpebblecommon.contacts

import io.rebble.libpebblecommon.contacts.SystemContact
import io.rebble.libpebblecommon.contacts.SystemContacts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class IosSystemContacts : SystemContacts {
    override fun registerForContactsChanges(): Flow<Unit> {
        return flow {  }
    }

    override suspend fun getContacts(): List<SystemContact> {
        return emptyList()
    }

    override fun hasPermission(): Boolean {
        return false
    }
}