package com.koflox.strava.impl.data.source.local

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.di.IoDispatcher
import com.koflox.di.StravaTokenDaoFactory
import com.koflox.strava.impl.data.source.local.dao.StravaTokenDao
import com.koflox.strava.impl.data.source.local.entity.StravaTokenEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal interface StravaTokenLocalDataSource {
    suspend fun get(): StravaTokenEntity?
    fun observe(): Flow<StravaTokenEntity?>
    suspend fun upsert(entity: StravaTokenEntity)
    suspend fun delete()
}

internal class StravaTokenLocalDataSourceImpl @Inject constructor(
    @param:StravaTokenDaoFactory private val daoFactory: ConcurrentFactory<StravaTokenDao>,
    @param:IoDispatcher private val dispatcherIo: CoroutineDispatcher,
) : StravaTokenLocalDataSource {

    override suspend fun get(): StravaTokenEntity? = withContext(dispatcherIo) {
        daoFactory.get().get()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<StravaTokenEntity?> = flow {
        emit(daoFactory.get())
    }.flatMapLatest { it.observe() }.flowOn(dispatcherIo)

    override suspend fun upsert(entity: StravaTokenEntity) = withContext(dispatcherIo) {
        daoFactory.get().upsert(entity)
    }

    override suspend fun delete() = withContext(dispatcherIo) {
        daoFactory.get().delete()
    }
}
