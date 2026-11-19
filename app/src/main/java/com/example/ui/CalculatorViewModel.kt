package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ads.UnityAdsManager
import com.example.data.local.CalculationHistoryEntity
import com.example.data.repository.CalculatorRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.domain.AngleUnit
import com.example.domain.CalculatorEngine
import com.example.domain.CalculatorError
import com.example.domain.EvaluationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val displayResult: String = "0",
    val livePreview: String = "",
    val error: CalculatorError? = null,
    val justEvaluated: Boolean = false,
    val isDarkTheme: Boolean = true,
    val isScientificMode: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val hapticFeedbackEnabled: Boolean = true,
    val isPremiumThemeUnlocked: Boolean = false,
    val usePremiumTheme: Boolean = false,
    val history: List<CalculationHistoryEntity> = emptyList()
)

class CalculatorViewModel(
    private val calculatorRepository: CalculatorRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _inputState = MutableStateFlow(InputState())
    private val _prefsOverride = MutableStateFlow<com.example.data.repository.UserPreferences?>(null)

    private data class InputState(
        val expression: String = "",
        val displayResult: String = "0",
        val livePreview: String = "",
        val error: CalculatorError? = null,
        val justEvaluated: Boolean = false
    )

    val uiState: StateFlow<CalculatorUiState> = combine(
        _inputState,
        preferencesRepository.preferencesFlow,
        _prefsOverride,
        calculatorRepository.history
    ) { input, storedPrefs, overridePrefs, historyList ->
        val prefs = overridePrefs ?: storedPrefs
        CalculatorUiState(
            expression = input.expression,
            displayResult = input.displayResult,
            livePreview = input.livePreview,
            error = input.error,
            justEvaluated = input.justEvaluated,
            isDarkTheme = prefs.isDarkTheme,
            isScientificMode = prefs.isScientificMode,
            angleUnit = prefs.angleUnit,
            hapticFeedbackEnabled = prefs.hapticFeedbackEnabled,
            isPremiumThemeUnlocked = prefs.isPremiumThemeUnlocked,
            usePremiumTheme = prefs.usePremiumTheme,
            history = historyList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalculatorUiState()
    )

    fun onDigit(digit: String) {
        _inputState.update { state ->
            val base = if (state.justEvaluated || state.error != null) "" else state.expression
            val nextExpr = if (base == "0" && digit != "0") digit else if (base == "0" && digit == "0") "0" else base + digit
            buildUpdatedInput(nextExpr, justEvaluated = false)
        }
    }

    fun onDecimal() {
        _inputState.update { state ->
            val base = if (state.justEvaluated || state.error != null) "" else state.expression
            // Prevent duplicate decimal point in current number segment
            var i = base.length - 1
            while (i >= 0 && (base[i].isDigit() || base[i] == '.')) {
                if (base[i] == '.') return@update state
                i--
            }
            val lastChar = base.lastOrNull()
            val nextExpr = when {
                base.isEmpty() -> "0."
                lastChar == null || (!lastChar.isDigit() && lastChar != '.') -> {
                    if (lastChar == ')' || lastChar == '%' || lastChar == '!' || lastChar == '²' || lastChar == 'π' || lastChar == 'e') {
                        "$base×0."
                    } else {
                        "${base}0."
                    }
                }
                else -> "$base."
            }
            buildUpdatedInput(nextExpr, justEvaluated = false)
        }
    }

    fun onOperator(op: String) {
        _inputState.update { state ->
            val base = when {
                state.error != null -> ""
                state.justEvaluated -> state.displayResult
                else -> state.expression
            }
            if (base.isEmpty()) {
                if (op == "−") {
                    return@update buildUpdatedInput("−", justEvaluated = false)
                }
                return@update state
            }
            val binaryOps = setOf('+', '−', '-', '×', '*', '÷', '/', '^')
            val last = base.last()
            val nextExpr = when {
                last == '(' -> if (op == "−") "$base−" else return@update state
                last in binaryOps -> {
                    // Replace trailing binary operator unless base is just "-"
                    if (base.length == 1 && last in setOf('−', '-')) {
                        return@update state
                    }
                    // If previous two chars are e.g. `×−`, replace both when another op is pressed
                    val prev = base.getOrNull(base.length - 2)
                    if ((last == '−' || last == '-') && prev != null && prev in binaryOps) {
                        base.dropLast(2) + op
                    } else {
                        base.dropLast(1) + op
                    }
                }
                else -> base + op
            }
            buildUpdatedInput(nextExpr, justEvaluated = false)
        }
    }

    fun onLeftParen() {
        _inputState.update { state ->
            val base = if (state.justEvaluated || state.error != null) "" else state.expression
            buildUpdatedInput("$base(", justEvaluated = false)
        }
    }

    fun onRightParen() {
        _inputState.update { state ->
            val base = if (state.justEvaluated || state.error != null) return@update state else state.expression
            val openCount = base.count { it == '(' }
            val closeCount = base.count { it == ')' }
            if (openCount > closeCount && base.isNotEmpty() && base.last() != '(') {
                buildUpdatedInput("$base)", justEvaluated = false)
            } else {
                state
            }
        }
    }

    fun onSmartParenthesis() {
        _inputState.update { state ->
            val base = if (state.justEvaluated || state.error != null) "" else state.expression
            val openCount = base.count { it == '(' }
            val closeCount = base.count { it == ')' }
            val last = base.lastOrNull()
            val canClose = openCount > closeCount &&
                last != null &&
                (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '%' || last == '!' || last == '²')
            val nextExpr = if (canClose) "$base)" else "$base("
            buildUpdatedInput(nextExpr, justEvaluated = false)
        }
    }

    fun onPercent() {
        _inputState.update { state ->
            val base = when {
                state.error != null -> return@update state
                state.justEvaluated -> state.displayResult
                else -> state.expression
            }
            val last = base.lastOrNull() ?: return@update state
            if (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '²' || last == '!') {
                buildUpdatedInput("$base%", justEvaluated = false)
            } else {
                state
            }
        }
    }

    fun onToggleSign() {
        _inputState.update { state ->
            val base = when {
                state.error != null -> ""
                state.justEvaluated -> state.displayResult
                else -> state.expression
            }
            val toggled = CalculatorEngine.toggleSign(base)
            buildUpdatedInput(toggled, justEvaluated = false)
        }
    }

    fun onScientificFunction(funcToken: String) {
        _inputState.update { state ->
            val base = if (state.justEvaluated || state.error != null) "" else state.expression
            buildUpdatedInput(base + funcToken, justEvaluated = false)
        }
    }

    fun onConstant(constant: String) {
        _inputState.update { state ->
            val base = if (state.justEvaluated || state.error != null) "" else state.expression
            buildUpdatedInput(base + constant, justEvaluated = false)
        }
    }

    fun onPostfixOp(op: String) {
        _inputState.update { state ->
            val base = when {
                state.error != null -> return@update state
                state.justEvaluated -> state.displayResult
                else -> state.expression
            }
            val last = base.lastOrNull() ?: return@update state
            if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
                buildUpdatedInput(base + op, justEvaluated = false)
            } else {
                state
            }
        }
    }

    fun onReciprocal() {
        _inputState.update { state ->
            val base = when {
                state.error != null -> return@update state
                state.justEvaluated -> state.displayResult
                else -> state.expression
            }
            if (base.isEmpty()) {
                buildUpdatedInput("1÷", justEvaluated = false)
            } else {
                buildUpdatedInput("1÷($base)", justEvaluated = false)
            }
        }
    }

    fun onAllClear() {
        _inputState.value = InputState()
    }

    fun onBackspace() {
        _inputState.update { state ->
            if (state.error != null || state.justEvaluated) {
                return@update InputState()
            }
            val expr = state.expression
            if (expr.isEmpty()) return@update state

            val functionSuffixes = listOf("sqrt(", "sin(", "cos(", "tan(", "log(", "ln(", "√(")
            val matchedSuffix = functionSuffixes.firstOrNull { expr.endsWith(it) }
            val nextExpr = if (matchedSuffix != null) {
                expr.dropLast(matchedSuffix.length)
            } else {
                expr.dropLast(1)
            }
            buildUpdatedInput(nextExpr, justEvaluated = false)
        }
    }

    fun onEquals() {
        val current = _inputState.value
        if (current.expression.isBlank()) return

        val angle = uiState.value.angleUnit
        val isSci = uiState.value.isScientificMode
        when (val result = CalculatorEngine.evaluate(current.expression, angle)) {
            is EvaluationResult.Success -> {
                _inputState.value = InputState(
                    expression = current.expression,
                    displayResult = result.formatted,
                    livePreview = "",
                    error = null,
                    justEvaluated = true
                )
                if (current.expression != result.formatted) {
                    UnityAdsManager.recordCompletedCalculation()
                    viewModelScope.launch {
                        calculatorRepository.saveCalculation(
                            expression = current.expression,
                            result = result.formatted,
                            isScientific = isSci
                        )
                    }
                }
            }
            is EvaluationResult.Error -> {
                _inputState.value = current.copy(
                    livePreview = "",
                    error = result.errorType,
                    justEvaluated = false
                )
            }
        }
    }

    fun onHistoryItemSelected(entry: CalculationHistoryEntity, useResultOnly: Boolean = false) {
        val nextExpr = if (useResultOnly) entry.result else entry.expression
        _inputState.value = buildUpdatedInput(nextExpr, justEvaluated = false)
    }

    fun onDeleteHistoryItem(id: Long) {
        viewModelScope.launch {
            calculatorRepository.deleteEntry(id)
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            calculatorRepository.clearHistory()
        }
    }

    fun onToggleScientificMode() {
        val current = uiState.value
        val next = !current.isScientificMode
        _prefsOverride.value = com.example.data.repository.UserPreferences(
            isDarkTheme = current.isDarkTheme,
            isScientificMode = next,
            angleUnit = current.angleUnit,
            hapticFeedbackEnabled = current.hapticFeedbackEnabled,
            isPremiumThemeUnlocked = current.isPremiumThemeUnlocked,
            usePremiumTheme = current.usePremiumTheme
        )
        viewModelScope.launch {
            preferencesRepository.setScientificMode(next)
        }
    }

    fun onToggleAngleUnit() {
        val current = uiState.value
        val next = if (current.angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
        _prefsOverride.value = com.example.data.repository.UserPreferences(
            isDarkTheme = current.isDarkTheme,
            isScientificMode = current.isScientificMode,
            angleUnit = next,
            hapticFeedbackEnabled = current.hapticFeedbackEnabled,
            isPremiumThemeUnlocked = current.isPremiumThemeUnlocked,
            usePremiumTheme = current.usePremiumTheme
        )
        viewModelScope.launch {
            preferencesRepository.setAngleUnit(next)
        }
        _inputState.update { state ->
            if (!state.justEvaluated && state.error == null && state.expression.isNotEmpty()) {
                buildUpdatedInput(state.expression, justEvaluated = false, overrideAngle = next)
            } else {
                state
            }
        }
    }

    fun onSetDarkTheme(isDark: Boolean) {
        val current = uiState.value
        _prefsOverride.value = com.example.data.repository.UserPreferences(
            isDarkTheme = isDark,
            isScientificMode = current.isScientificMode,
            angleUnit = current.angleUnit,
            hapticFeedbackEnabled = current.hapticFeedbackEnabled,
            isPremiumThemeUnlocked = current.isPremiumThemeUnlocked,
            usePremiumTheme = false
        )
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(isDark)
            if (current.usePremiumTheme) {
                preferencesRepository.setUsePremiumTheme(false)
            }
        }
    }

    fun onSetHapticFeedback(enabled: Boolean) {
        val current = uiState.value
        _prefsOverride.value = com.example.data.repository.UserPreferences(
            isDarkTheme = current.isDarkTheme,
            isScientificMode = current.isScientificMode,
            angleUnit = current.angleUnit,
            hapticFeedbackEnabled = enabled,
            isPremiumThemeUnlocked = current.isPremiumThemeUnlocked,
            usePremiumTheme = current.usePremiumTheme
        )
        viewModelScope.launch {
            preferencesRepository.setHapticFeedback(enabled)
        }
    }

    fun onUnlockPremiumTheme() {
        val current = uiState.value
        _prefsOverride.value = com.example.data.repository.UserPreferences(
            isDarkTheme = current.isDarkTheme,
            isScientificMode = current.isScientificMode,
            angleUnit = current.angleUnit,
            hapticFeedbackEnabled = current.hapticFeedbackEnabled,
            isPremiumThemeUnlocked = true,
            usePremiumTheme = true
        )
        viewModelScope.launch {
            preferencesRepository.unlockAndEnablePremiumTheme()
        }
    }

    fun onSetUsePremiumTheme(enabled: Boolean) {
        val current = uiState.value
        if (!current.isPremiumThemeUnlocked) return
        _prefsOverride.value = com.example.data.repository.UserPreferences(
            isDarkTheme = current.isDarkTheme,
            isScientificMode = current.isScientificMode,
            angleUnit = current.angleUnit,
            hapticFeedbackEnabled = current.hapticFeedbackEnabled,
            isPremiumThemeUnlocked = true,
            usePremiumTheme = enabled
        )
        viewModelScope.launch {
            preferencesRepository.setUsePremiumTheme(enabled)
        }
    }

    fun getCopyableResult(): String? {
        val state = _inputState.value
        if (state.error != null) return null
        return when {
            state.justEvaluated -> state.displayResult
            state.livePreview.isNotEmpty() -> state.livePreview
            state.expression.isNotEmpty() -> state.expression
            else -> null
        }
    }

    private fun buildUpdatedInput(
        expr: String,
        justEvaluated: Boolean,
        overrideAngle: AngleUnit? = null
    ): InputState {
        if (expr.isEmpty()) {
            return InputState()
        }
        val angle = overrideAngle ?: uiState.value.angleUnit
        val hasComputableOps = expr.any {
            it in setOf('+', '−', '-', '×', '*', '÷', '/', '%', '^', '!', '²', '√', 'π', 'e', '(')
        } || expr.contains("sin") || expr.contains("cos") || expr.contains("tan") ||
            expr.contains("ln") || expr.contains("log")

        val preview = if (hasComputableOps) {
            when (val res = CalculatorEngine.evaluate(expr, angle)) {
                is EvaluationResult.Success -> if (res.formatted != expr) res.formatted else ""
                is EvaluationResult.Error -> ""
            }
        } else {
            ""
        }

        return InputState(
            expression = expr,
            displayResult = expr,
            livePreview = preview,
            error = null,
            justEvaluated = justEvaluated
        )
    }

    class Factory(
        private val calculatorRepository: CalculatorRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalculatorViewModel(calculatorRepository, preferencesRepository) as T
        }
    }
}
