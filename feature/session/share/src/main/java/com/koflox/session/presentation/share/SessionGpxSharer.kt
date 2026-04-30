package com.koflox.session.presentation.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

internal interface SessionGpxSharer {
    fun isGpxSharingAvailable(): Boolean
    suspend fun shareGpx(gpxContent: String, fileName: String, chooserTitle: String): GpxShareResult
}

internal sealed interface GpxShareResult {
    data class Success(val intent: Intent) : GpxShareResult
    data object CannotWriteFile : GpxShareResult
    data object NoAppAvailable : GpxShareResult
}

internal class SessionGpxSharerImpl(
    private val context: Context,
    private val dispatcherIo: CoroutineDispatcher,
) : SessionGpxSharer {

    override fun isGpxSharingAvailable(): Boolean {
        val testIntent = Intent(Intent.ACTION_SEND).apply {
            type = GPX_MIME_TYPE
        }
        return testIntent.resolveActivity(context.packageManager) != null
    }

    override suspend fun shareGpx(gpxContent: String, fileName: String, chooserTitle: String): GpxShareResult {
        if (!isGpxSharingAvailable()) return GpxShareResult.NoAppAvailable
        val uri = writeGpxToCacheOrNull(gpxContent, fileName)
        return if (uri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = GPX_MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            GpxShareResult.Success(Intent.createChooser(shareIntent, chooserTitle))
        } else {
            GpxShareResult.CannotWriteFile
        }
    }

    private suspend fun writeGpxToCacheOrNull(gpxContent: String, fileName: String) = try {
        writeGpxToCache(gpxContent, fileName)
    } catch (_: IOException) {
        null
    }

    private suspend fun writeGpxToCache(gpxContent: String, fileName: String) = withContext(dispatcherIo) {
        val cachePath = File(context.cacheDir, "gpx")
        cachePath.mkdirs()
        val file = File(cachePath, "$fileName.gpx")
        file.writeText(gpxContent)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}

private const val GPX_MIME_TYPE = "application/gpx+xml"
