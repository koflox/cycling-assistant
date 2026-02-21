package com.koflox.session.di

import com.koflox.di.ClassNameQualifier
import org.koin.core.qualifier.Qualifier

sealed class SessionQualifier : ClassNameQualifier(), Qualifier {
    data object DaoFactory : SessionQualifier()
    data object SessionMutex : SessionQualifier()
}
