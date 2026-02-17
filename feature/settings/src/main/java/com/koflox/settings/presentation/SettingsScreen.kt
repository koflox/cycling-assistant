package com.koflox.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.koflox.designsystem.component.LocalizedExposedDropdownMenu
import com.koflox.designsystem.theme.Spacing
import com.koflox.nutritionsettings.bridge.navigator.NutritionSettingsUiNavigator
import com.koflox.settings.R
import com.koflox.theme.domain.model.AppTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun SettingsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    nutritionSettingsUiNavigator: NutritionSettingsUiNavigator = koinInject(),
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    SettingsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEvent = viewModel::onEvent,
        nutritionSettingsUiNavigator = nutritionSettingsUiNavigator,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onEvent: (SettingsUiEvent) -> Unit,
    nutritionSettingsUiNavigator: NutritionSettingsUiNavigator,
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
        SettingsBody(
            uiState = uiState,
            onEvent = onEvent,
            nutritionSettingsUiNavigator = nutritionSettingsUiNavigator,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.Large),
        )
    }
}

@Composable
private fun SettingsBody(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    nutritionSettingsUiNavigator: NutritionSettingsUiNavigator,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge),
    ) {
        SettingsSection(title = stringResource(R.string.settings_section_app)) {
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
        SettingsSection(title = stringResource(R.string.settings_section_profile)) {
            SettingTextField(
                label = stringResource(R.string.settings_rider_weight),
                value = uiState.riderWeightKg,
                onValueChange = { onEvent(SettingsUiEvent.RiderWeightChanged(it)) },
                keyboardType = KeyboardType.Decimal,
                placeholder = stringResource(R.string.settings_rider_weight_hint),
                isError = uiState.isRiderWeightError,
                errorText = uiState.riderWeightError?.let {
                    stringResource(R.string.settings_rider_weight_error, it.minWeightKg, it.maxWeightKg)
                },
            )
        }
        SettingsSection(title = stringResource(R.string.settings_section_nutrition)) {
            nutritionSettingsUiNavigator.NutritionSettingsSection(modifier = Modifier)
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.Large),
            content = content,
        )
    }
}

@Composable
private fun AppTheme.displayName(): String = when (this) {
    AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
    AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
    AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
}

@Composable
private fun SettingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = isError,
            placeholder = placeholder?.let { { Text(text = it) } },
            supportingText = errorText?.let {
                {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
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
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            LocalizedExposedDropdownMenu(
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
