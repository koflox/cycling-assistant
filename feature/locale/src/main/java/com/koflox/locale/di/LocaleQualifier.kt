package com.koflox.locale.di

import com.koflox.di.ClassNameQualifier
import org.koin.core.qualifier.Qualifier

sealed class LocaleQualifier : ClassNameQualifier(), Qualifier {
    data object DaoFactory : LocaleQualifier()
}
