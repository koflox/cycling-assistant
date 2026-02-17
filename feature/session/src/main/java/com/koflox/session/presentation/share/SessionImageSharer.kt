package com.koflox.session.presentation.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

interface SessionImageSharer {
    suspend fun shareImage(bitmap: Bitmap, shareText: String, chooserTitle: String): ShareResult
}

sealed interface ShareResult {
    data class Success(val intent: Intent) : ShareResult
    data object NoAppAvailable : ShareResult
    data object CannotProcessTheImage : ShareResult
}

internal class SessionImageSharerImpl(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : SessionImageSharer {

    override suspend fun shareImage(bitmap: Bitmap, shareText: String, chooserTitle: String): ShareResult {
        val uri = try {
            saveBitmapToCache(bitmap)
        } catch (_: IOException) {
            return ShareResult.CannotProcessTheImage
        }
        val shareIntent = createShareIntent(uri, shareText)
        return if (canResolveIntent(shareIntent)) {
            ShareResult.Success(Intent.createChooser(shareIntent, chooserTitle))
        } else {
            ShareResult.NoAppAvailable
        }
    }

    private suspend fun saveBitmapToCache(bitmap: Bitmap): Uri = withContext(dispatcherIo) {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "session_share.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun createShareIntent(uri: Uri, shareText: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }
}
