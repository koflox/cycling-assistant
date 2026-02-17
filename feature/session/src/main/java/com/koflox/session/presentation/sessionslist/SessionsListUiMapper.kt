package com.koflox.session.presentation.sessionslist

import com.koflox.concurrent.lazyUnsafe
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface SessionsListUiMapper {
    fun toUiModel(session: Session): SessionListItemUiModel
}

internal class SessionsListUiMapperImpl : SessionsListUiMapper {

    private val dateFormat by lazyUnsafe {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }

    override fun toUiModel(session: Session): SessionListItemUiModel {
        return SessionListItemUiModel(
            id = session.id,
            destinationName = session.destinationName,
            dateFormatted = dateFormat.format(Date(session.startTimeMs)),
            distanceFormatted = String.format(Locale.getDefault(), "%.2f", session.traveledDistanceKm),
            status = when (session.status) {
                SessionStatus.RUNNING -> SessionListItemStatus.RUNNING
                SessionStatus.PAUSED -> SessionListItemStatus.PAUSED
                SessionStatus.COMPLETED -> SessionListItemStatus.COMPLETED
            },
            isShareButtonVisible = session.status == SessionStatus.COMPLETED,
        )
    }
}
