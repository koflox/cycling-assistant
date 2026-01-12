package com.koflox.di

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

open class ClassNameQualifier : Qualifier {
    override val value: QualifierValue
        get() = javaClass.name

}
