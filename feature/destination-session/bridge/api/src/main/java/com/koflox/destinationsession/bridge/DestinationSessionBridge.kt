package com.koflox.destinationsession.bridge

/**
 * Combined interface for cycling session functionality.
 * Extends both [CyclingSessionUseCase] for state access and [CyclingSessionUiNavigator] for UI.
 *
 * Prefer depending on the specific interface you need:
 * - [CyclingSessionUseCase] for ViewModels/domain layer
 * - [CyclingSessionUiNavigator] for Composable screens
 */
interface DestinationSessionBridge : CyclingSessionUseCase, CyclingSessionUiNavigator
