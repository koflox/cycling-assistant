package com.koflox.session.presentation.share

import com.koflox.designsystem.text.UiText
import com.koflox.session.R

interface ShareErrorMapper {
    fun map(result: ShareResult): UiText?
}

internal class ShareErrorMapperImpl : ShareErrorMapper {

    override fun map(result: ShareResult): UiText? = when (result) {
        is ShareResult.Success -> null
        ShareResult.CannotProcessTheImage -> UiText.Resource(R.string.share_image_processing_error)
        ShareResult.NoAppAvailable -> UiText.Resource(R.string.share_no_app_available)
    }
}
