package com.koflox.session.presentation.sessionslist

import com.koflox.designsystem.context.LocalizedContextProvider
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface SessionsListUiMapper {
    fun toUiModel(session: Session): SessionListItemUiModel
}

internal class SessionsListUiMapperImpl(
    private val localizedContextProvider: LocalizedContextProvider,
) : SessionsListUiMapper {

    companion object {
        private const val DATE_FORMAT_PATTERN = "MMM dd, yyyy HH:mm"
    }

    private val appLocale: Locale
        get() = localizedContextProvider.getLocalizedContext().resources.configuration.locales[0]

    override fun toUiModel(session: Session): SessionListItemUiModel = SessionListItemUiModel(
        id = session.id,
        destinationName = session.destinationName,
        dateFormatted = SimpleDateFormat(DATE_FORMAT_PATTERN, appLocale).format(Date(session.startTimeMs)),
        distanceFormatted = String.format(appLocale, "%.2f", session.traveledDistanceKm),
        status = when (session.status) {
            SessionStatus.RUNNING -> SessionListItemStatus.RUNNING
            SessionStatus.PAUSED -> SessionListItemStatus.PAUSED
            SessionStatus.COMPLETED -> SessionListItemStatus.COMPLETED
        },
        isShareButtonVisible = session.status == SessionStatus.COMPLETED,
    )
}
