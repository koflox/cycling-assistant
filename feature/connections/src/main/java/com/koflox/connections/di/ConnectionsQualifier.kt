package com.koflox.connections.di

import com.koflox.di.ClassNameQualifier
import org.koin.core.qualifier.Qualifier

sealed class ConnectionsQualifier : ClassNameQualifier(), Qualifier {
    data object DaoFactory : ConnectionsQualifier()
}
