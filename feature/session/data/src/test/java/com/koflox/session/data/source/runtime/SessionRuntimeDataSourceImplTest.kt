package com.koflox.session.data.source.runtime

import com.koflox.session.testutil.createSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SessionRuntimeDataSourceImplTest {

    private lateinit var dataSource: SessionRuntimeDataSourceImpl

    @Before
    fun setup() {
        dataSource = SessionRuntimeDataSourceImpl()
    }

    @Test
    fun `activeSession initially null`() {
        assertNull(dataSource.activeSession.value)
    }

    @Test
    fun `setActiveSession updates activeSession`() {
        val session = createSession(id = "s1")

        dataSource.setActiveSession(session)

        assertEquals(session, dataSource.activeSession.value)
    }

    @Test
    fun `clearActiveSession resets all state`() {
        val session = createSession(id = "s1")
        dataSource.setActiveSession(session)
        dataSource.setFlushInfo(FlushInfo(trackPointCount = 10, timeMs = 1000L))

        dataSource.clearActiveSession()

        assertNull(dataSource.activeSession.value)
        assertEquals(FlushInfo(), dataSource.getFlushInfo())
    }

    @Test
    fun `flushInfo initially default`() {
        assertEquals(FlushInfo(), dataSource.getFlushInfo())
    }

    @Test
    fun `setFlushInfo updates flush info`() {
        val info = FlushInfo(trackPointCount = 42, timeMs = 123456L)

        dataSource.setFlushInfo(info)

        assertEquals(info, dataSource.getFlushInfo())
    }
}
