package com.koflox.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.theme.Spacing
import com.koflox.settings.R
import com.koflox.settings.domain.model.AppTheme
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SettingsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    SettingsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge),
        ) {
            SettingDropdown(
                label = stringResource(R.string.settings_theme),
                selectedValue = uiState.selectedTheme.displayName(),
                isExpanded = uiState.isThemeDropdownExpanded,
                onExpandToggle = { onEvent(SettingsUiEvent.ThemeDropdownToggled) },
                onDismiss = { onEvent(SettingsUiEvent.DropdownsDismissed) },
                items = uiState.availableThemes,
                itemLabel = { it.displayName() },
                onItemSelected = { onEvent(SettingsUiEvent.ThemeSelected(it)) },
            )
            SettingDropdown(
                label = stringResource(R.string.settings_language),
                selectedValue = uiState.selectedLanguage.displayName,
                isExpanded = uiState.isLanguageDropdownExpanded,
                onExpandToggle = { onEvent(SettingsUiEvent.LanguageDropdownToggled) },
                onDismiss = { onEvent(SettingsUiEvent.DropdownsDismissed) },
                items = uiState.availableLanguages,
                itemLabel = { it.displayName },
                onItemSelected = { onEvent(SettingsUiEvent.LanguageSelected(it)) },
            )
        }
    }
}

@Composable
private fun AppTheme.displayName(): String = when (this) {
    AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
    AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
    AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingDropdown(
    label: String,
    selectedValue: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onDismiss: () -> Unit,
    items: List<T>,
    itemLabel: @Composable (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { onExpandToggle() },
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = onDismiss,
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemLabel(item)) },
                        onClick = { onItemSelected(item) },
                    )
                }
            }
        }
    }
}
