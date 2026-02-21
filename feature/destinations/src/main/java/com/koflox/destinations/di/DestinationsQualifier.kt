package com.koflox.destinations.di

import com.koflox.di.ClassNameQualifier
import org.koin.core.qualifier.Qualifier

sealed class DestinationsQualifier : ClassNameQualifier(), Qualifier {
    data object DaoFactory : DestinationsQualifier()
}
