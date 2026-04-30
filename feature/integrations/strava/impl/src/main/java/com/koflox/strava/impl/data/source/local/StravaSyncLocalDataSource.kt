package com.koflox.strava.impl.data.source.local

import com.koflox.concurrent.ConcurrentFactory
import com.koflox.di.IoDispatcher
import com.koflox.di.StravaSyncDaoFactory
import com.koflox.strava.impl.data.source.local.dao.StravaSyncDao
import com.koflox.strava.impl.data.source.local.entity.StravaSyncEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal interface StravaSyncLocalDataSource {
    fun observe(sessionId: String): Flow<StravaSyncEntity?>
    fun observeAll(): Flow<List<StravaSyncEntity>>
    suspend fun get(sessionId: String): StravaSyncEntity?
    suspend fun upsert(entity: StravaSyncEntity)
    suspend fun delete(sessionId: String)
}

internal class StravaSyncLocalDataSourceImpl @Inject constructor(
    @param:StravaSyncDaoFactory private val daoFactory: ConcurrentFactory<StravaSyncDao>,
    @param:IoDispatcher private val dispatcherIo: CoroutineDispatcher,
) : StravaSyncLocalDataSource {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(sessionId: String): Flow<StravaSyncEntity?> = flow {
        emit(daoFactory.get())
    }.flatMapLatest { it.observe(sessionId) }.flowOn(dispatcherIo)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAll(): Flow<List<StravaSyncEntity>> = flow {
        emit(daoFactory.get())
    }.flatMapLatest { it.observeAll() }.flowOn(dispatcherIo)

    override suspend fun get(sessionId: String): StravaSyncEntity? = withContext(dispatcherIo) {
        daoFactory.get().get(sessionId)
    }

    override suspend fun upsert(entity: StravaSyncEntity) = withContext(dispatcherIo) {
        daoFactory.get().upsert(entity)
    }

    override suspend fun delete(sessionId: String) = withContext(dispatcherIo) {
        daoFactory.get().delete(sessionId)
    }
}
