package com.koflox.profile.di

import com.koflox.profile.domain.usecase.GetRiderWeightUseCase
import com.koflox.profile.domain.usecase.GetRiderWeightUseCaseImpl
import com.koflox.profile.domain.usecase.UpdateRiderWeightUseCase
import com.koflox.profile.domain.usecase.UpdateRiderWeightUseCaseImpl
import org.koin.dsl.module

internal val domainModule = module {
    factory<GetRiderWeightUseCase> {
        GetRiderWeightUseCaseImpl(repository = get())
    }
    factory<UpdateRiderWeightUseCase> {
        UpdateRiderWeightUseCaseImpl(repository = get())
    }
}
