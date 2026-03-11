package com.koflox.session.presentation.statsdisplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.domain.model.StatsDisplayConfig
import com.koflox.session.domain.usecase.ObserveStatsDisplayConfigUseCase
import com.koflox.session.domain.usecase.UpdateStatsDisplayConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StatsDisplayConfigViewModel @Inject constructor(
    private val observeStatsDisplayConfigUseCase: ObserveStatsDisplayConfigUseCase,
    private val updateStatsDisplayConfigUseCase: UpdateStatsDisplayConfigUseCase,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsDisplayConfigUiState>(StatsDisplayConfigUiState.Loading)
    val uiState: StateFlow<StatsDisplayConfigUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<StatsDisplayConfigNavigation>()
    val navigation = _navigation.receiveAsFlow()

    private val pendingActive = mutableListOf<SessionStatType>()
    private val pendingCompleted = mutableListOf<SessionStatType>()
    private val pendingShare = mutableListOf<SessionStatType>()

    private var savedActive = emptyList<SessionStatType>()
    private var savedCompleted = emptyList<SessionStatType>()
    private var savedShare = emptyList<SessionStatType>()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            val config = observeStatsDisplayConfigUseCase.observeStatsDisplayConfig().first()
            pendingActive.addAll(config.activeSessionStats)
            pendingCompleted.addAll(config.completedSessionStats)
            pendingShare.addAll(config.shareStats)
            savedActive = config.activeSessionStats
            savedCompleted = config.completedSessionStats
            savedShare = config.shareStats
            emitContentState()
        }
    }

    fun onEvent(event: StatsDisplayConfigUiEvent) {
        when (event) {
            is StatsDisplayConfigUiEvent.StatAdded -> onStatAdded(event.section, event.type)
            is StatsDisplayConfigUiEvent.StatRemoved -> onStatRemoved(event.section, event.type)
            is StatsDisplayConfigUiEvent.StatReordered -> {
                onStatReordered(event.section, event.fromIndex, event.toIndex)
            }
            is StatsDisplayConfigUiEvent.ResetSectionClicked -> onResetSection(event.section)
            is StatsDisplayConfigUiEvent.SaveSectionClicked -> viewModelScope.launch(dispatcherDefault) {
                onSaveSection(event.section)
            }
            StatsDisplayConfigUiEvent.SaveAllClicked -> viewModelScope.launch(dispatcherDefault) {
                onSaveAll()
            }
        }
    }

    private fun onResetSection(section: StatsDisplaySection) {
        val pending = getPendingList(section)
        pending.clear()
        pending.addAll(getDefaultStats(section))
        emitContentState()
    }

    private fun onStatAdded(section: StatsDisplaySection, type: SessionStatType) {
        val pending = getPendingList(section)
        val maxCount = getMaxCount(section)
        if (maxCount == null || pending.size < maxCount) {
            pending.add(type)
        }
        emitContentState()
    }

    private fun onStatRemoved(section: StatsDisplaySection, type: SessionStatType) {
        getPendingList(section).remove(type)
        emitContentState()
    }

    private fun onStatReordered(section: StatsDisplaySection, fromIndex: Int, toIndex: Int) {
        val pending = getPendingList(section)
        val item = pending.removeAt(fromIndex)
        pending.add(toIndex, item)
        emitContentState()
    }

    private suspend fun onSaveSection(section: StatsDisplaySection) {
        val pending = getPendingList(section).toList()
        when (section) {
            StatsDisplaySection.ACTIVE_SESSION -> {
                updateStatsDisplayConfigUseCase.updateActiveSessionStats(pending)
                savedActive = pending
            }
            StatsDisplaySection.COMPLETED_SESSION -> {
                updateStatsDisplayConfigUseCase.updateCompletedSessionStats(pending)
                savedCompleted = pending
            }
            StatsDisplaySection.SHARE -> {
                updateStatsDisplayConfigUseCase.updateShareStats(pending)
                savedShare = pending
            }
        }
        emitContentState()
    }

    private suspend fun onSaveAll() {
        val activeList = pendingActive.toList()
        val completedList = pendingCompleted.toList()
        val shareList = pendingShare.toList()
        updateStatsDisplayConfigUseCase.updateActiveSessionStats(activeList)
        updateStatsDisplayConfigUseCase.updateCompletedSessionStats(completedList)
        updateStatsDisplayConfigUseCase.updateShareStats(shareList)
        savedActive = activeList
        savedCompleted = completedList
        savedShare = shareList
        _navigation.send(StatsDisplayConfigNavigation.NavigateBack)
    }

    private fun getPendingList(section: StatsDisplaySection): MutableList<SessionStatType> = when (section) {
        StatsDisplaySection.ACTIVE_SESSION -> pendingActive
        StatsDisplaySection.COMPLETED_SESSION -> pendingCompleted
        StatsDisplaySection.SHARE -> pendingShare
    }

    private fun getMaxCount(section: StatsDisplaySection): Int? = when (section) {
        StatsDisplaySection.ACTIVE_SESSION -> StatsDisplayConfig.ACTIVE_SESSION_STATS_COUNT
        StatsDisplaySection.COMPLETED_SESSION -> null
        StatsDisplaySection.SHARE -> StatsDisplayConfig.SHARE_MAX_STATS
    }

    private fun getDefaultStats(section: StatsDisplaySection): List<SessionStatType> = when (section) {
        StatsDisplaySection.ACTIVE_SESSION -> StatsDisplayConfig.DEFAULT_ACTIVE_SESSION_STATS
        StatsDisplaySection.COMPLETED_SESSION -> StatsDisplayConfig.DEFAULT_COMPLETED_SESSION_STATS
        StatsDisplaySection.SHARE -> StatsDisplayConfig.DEFAULT_SHARE_STATS
    }

    private fun emitContentState() {
        val sections = listOf(
            buildSectionModel(
                StatsDisplaySection.ACTIVE_SESSION, StatsDisplayConfig.ACTIVE_SESSION_POOL, pendingActive, savedActive,
            ),
            buildSectionModel(
                StatsDisplaySection.COMPLETED_SESSION, StatsDisplayConfig.COMPLETED_SHARE_POOL, pendingCompleted, savedCompleted,
            ),
            buildSectionModel(
                StatsDisplaySection.SHARE, StatsDisplayConfig.COMPLETED_SHARE_POOL, pendingShare, savedShare,
            ),
        )
        val isSaveAllEnabled = sections.all { it.isSelectionValid } && sections.any { it.isSaveEnabled }
        _uiState.value = StatsDisplayConfigUiState.Content(
            sections = sections,
            isSaveAllEnabled = isSaveAllEnabled,
        )
    }

    private fun buildSectionModel(
        section: StatsDisplaySection,
        pool: List<SessionStatType>,
        pending: List<SessionStatType>,
        saved: List<SessionStatType>,
    ): SectionUiModel {
        val isValid = isSectionValid(section, pending)
        val isDirty = pending != saved
        val maxCount = getMaxCount(section)
        return SectionUiModel(
            section = section,
            selectedStats = pending.map { StatItemUiModel(type = it) },
            availableStats = pool.filter { it !in pending }.map { StatItemUiModel(type = it) },
            maxSelectionCount = maxCount,
            isAddEnabled = maxCount == null || pending.size < maxCount,
            isSaveEnabled = isValid && isDirty,
            isSelectionValid = isValid,
        )
    }

    private fun isSectionValid(section: StatsDisplaySection, pending: List<SessionStatType>): Boolean = when (section) {
        StatsDisplaySection.ACTIVE_SESSION -> pending.size == StatsDisplayConfig.ACTIVE_SESSION_STATS_COUNT
        StatsDisplaySection.COMPLETED_SESSION -> pending.size >= StatsDisplayConfig.COMPLETED_SESSION_MIN_STATS
        StatsDisplaySection.SHARE -> pending.size in StatsDisplayConfig.SHARE_MIN_STATS..StatsDisplayConfig.SHARE_MAX_STATS
    }
}
