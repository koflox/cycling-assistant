package com.koflox.session.presentation.share

import android.content.Context
import com.koflox.session.R

interface ShareErrorMapper {
    fun map(result: ShareResult): String?
}

internal class ShareErrorMapperImpl(
    private val context: Context,
) : ShareErrorMapper {

    override fun map(result: ShareResult): String? = when (result) {
        is ShareResult.Success -> null
        ShareResult.CannotProcessTheImage -> context.getString(R.string.share_image_processing_error)
        ShareResult.NoAppAvailable -> context.getString(R.string.share_no_app_available)
    }
}
