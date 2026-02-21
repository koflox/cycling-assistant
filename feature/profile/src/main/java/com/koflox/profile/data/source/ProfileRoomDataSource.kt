package com.koflox.profile.data.source

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.profile.data.source.local.dao.ProfileDao
import com.koflox.profile.data.source.local.entity.ProfileSettingsEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class ProfileRoomDataSource(
    private val daoFactory: ConcurrentFactory<ProfileDao>,
    private val dispatcherIo: CoroutineDispatcher,
) : ProfileLocalDataSource {

    override suspend fun getRiderWeightKg(): Float? = withContext(dispatcherIo) {
        daoFactory.get().getSettings()?.riderWeightKg?.toFloat()
    }

    override suspend fun setRiderWeightKg(weightKg: Double) {
        withContext(dispatcherIo) {
            daoFactory.get().insertSettings(ProfileSettingsEntity(riderWeightKg = weightKg))
        }
    }
}
