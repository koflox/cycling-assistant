package com.koflox.profile.di

import com.koflox.di.ClassNameQualifier
import org.koin.core.qualifier.Qualifier

sealed class ProfileQualifier : ClassNameQualifier(), Qualifier {
    data object DaoFactory : ProfileQualifier()
}
