package com.koflox.gpx

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

interface GpxMapper {
    fun map(input: GpxInput): String
}

internal class GpxMapperImpl : GpxMapper {

    companion object {
        private val GPX_HEADER = """
            |<?xml version="1.0" encoding="UTF-8"?>
            |<gpx version="1.1" creator="CyclingAssistant"
            |  xmlns="http://www.topografix.com/GPX/1/1"
            |  xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1"
            |  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            |  xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
        """.trimMargin()
        private const val GPX_FOOTER = "</gpx>"
        private const val TRKSEG_OPEN = "    <trkseg>"
        private const val TRKSEG_CLOSE = "    </trkseg>"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun map(input: GpxInput): String = buildString {
        appendLine(GPX_HEADER)
        appendMetadata(input)
        appendTrack(input)
        appendLine(GPX_FOOTER)
    }

    private fun StringBuilder.appendMetadata(input: GpxInput) {
        appendLine("  <metadata>")
        appendLine(formatNameTag(input.name))
        appendLine("    <time>${formatTimestamp(input.startTimeMs)}</time>")
        appendLine("  </metadata>")
    }

    private fun StringBuilder.appendTrack(input: GpxInput) {
        appendLine("  <trk>")
        appendLine(formatNameTag(input.name))
        appendTrackSegments(input.trackPoints)
        appendLine("  </trk>")
    }

    private fun formatNameTag(name: String): String = "    <name>${escapeXml(name)}</name>"

    private fun StringBuilder.appendTrackSegments(trackPoints: List<GpxTrackPoint>) {
        if (trackPoints.isEmpty()) return
        var segmentOpen = false
        for (point in trackPoints) {
            if (point.isSegmentStart) {
                if (segmentOpen) {
                    appendLine(TRKSEG_CLOSE)
                }
                appendLine(TRKSEG_OPEN)
                segmentOpen = true
            } else if (!segmentOpen) {
                appendLine(TRKSEG_OPEN)
                segmentOpen = true
            }
            appendTrackPoint(point)
        }
        if (segmentOpen) {
            appendLine(TRKSEG_CLOSE)
        }
    }

    private fun StringBuilder.appendTrackPoint(point: GpxTrackPoint) {
        appendLine("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">")
        point.altitudeMeters?.let { appendLine("        <ele>$it</ele>") }
        appendLine("        <time>${formatTimestamp(point.timestampMs)}</time>")
        point.powerWatts?.let {
            appendLine("        <extensions>")
            appendLine("          <gpxtpx:TrackPointExtension>")
            appendLine("            <gpxtpx:power>$it</gpxtpx:power>")
            appendLine("          </gpxtpx:TrackPointExtension>")
            appendLine("        </extensions>")
        }
        appendLine("      </trkpt>")
    }

    private fun formatTimestamp(timestampMs: Long): String = dateFormat.format(timestampMs)

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
