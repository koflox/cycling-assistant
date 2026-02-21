package com.koflox.session.di

import com.koflox.di.ClassNameQualifier
import org.koin.core.qualifier.Qualifier

internal sealed class SessionQualifier : ClassNameQualifier(), Qualifier {
    data object SessionMutex : SessionQualifier()
}
