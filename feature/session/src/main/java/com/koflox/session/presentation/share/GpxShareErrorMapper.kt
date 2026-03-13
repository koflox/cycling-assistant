package com.koflox.session.presentation.share

import com.koflox.designsystem.text.UiText
import com.koflox.session.R

internal interface GpxShareErrorMapper {
    fun map(result: GpxShareResult): UiText?
}

internal class GpxShareErrorMapperImpl : GpxShareErrorMapper {

    override fun map(result: GpxShareResult): UiText? = when (result) {
        is GpxShareResult.Success -> null
        GpxShareResult.CannotWriteFile -> UiText.Resource(R.string.gpx_export_file_error)
        GpxShareResult.NoAppAvailable -> UiText.Resource(R.string.gpx_export_no_app)
    }
}
