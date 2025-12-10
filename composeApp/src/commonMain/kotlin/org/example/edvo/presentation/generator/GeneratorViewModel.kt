package org.example.edvo.presentation.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.edvo.core.generator.SecurityGenerator

enum class GeneratorMode { PASSWORD, PASSPHRASE, USERNAME }

data class GeneratorState(
    val mode: GeneratorMode = GeneratorMode.PASSWORD,
    val output: String = "",
    val strength: Float = 0f,
    
    // Password Config
    val pwLength: Int = 16,
    val pwUpper: Boolean = true,
    val pwLower: Boolean = true,
    val pwDigits: Boolean = true,
    val pwSymbols: Boolean = true,
    val pwAmbiguous: Boolean = false,

    // Passphrase Config
    val ppWordCount: Int = 4,
    val ppSeparator: String = "-",
    val ppCapitalize: Boolean = true,
    val ppNumber: Boolean = false,

    // Username Config
    val unStyle: SecurityGenerator.UsernameStyle = SecurityGenerator.UsernameStyle.ADJECTIVE_NOUN,
    val unCapitalize: Boolean = true,
    val unNumber: Boolean = true
)

class GeneratorViewModel : ViewModel() {
    private val _state = MutableStateFlow(GeneratorState())
    val state = _state.asStateFlow()

    init {
        generate()
    }

    fun setMode(mode: GeneratorMode) {
        _state.update { it.copy(mode = mode) }
        generate()
    }

    fun generate() {
        val s = _state.value
        val result = when(s.mode) {
            GeneratorMode.PASSWORD -> SecurityGenerator.generatePassword(
                s.pwLength, s.pwUpper, s.pwLower, s.pwDigits, s.pwSymbols, s.pwAmbiguous
            )
            GeneratorMode.PASSPHRASE -> SecurityGenerator.generatePassphrase(
                s.ppWordCount, s.ppSeparator, s.ppCapitalize, s.ppNumber
            )
            GeneratorMode.USERNAME -> SecurityGenerator.generateUsername(
                s.unStyle, s.unCapitalize, s.unNumber
            )
        }
        
        // Calculate Pseudo-Strength
        val strength = calculateStrength(result, s.mode)
        
        _state.update { it.copy(output = result, strength = strength) }
    }

    private fun calculateStrength(text: String, mode: GeneratorMode): Float {
        // Simple heuristic for UI visualization
        return when (mode) {
            GeneratorMode.PASSWORD -> (text.length / 20f).coerceIn(0.2f, 1f)
            GeneratorMode.PASSPHRASE -> (text.length / 30f).coerceIn(0.3f, 1f)
            GeneratorMode.USERNAME -> 0.3f // Usernames aren't really "strong" secrets
        }
    }

    // --- Configuration Updaters ---

    // --- Configuration Updaters ---

    fun updatePasswordConfig(length: Int? = null, upper: Boolean? = null, lower: Boolean? = null, digits: Boolean? = null, symbols: Boolean? = null, ambiguous: Boolean? = null, regenerate: Boolean = true) {
        _state.update { 
            it.copy(
                pwLength = length ?: it.pwLength,
                pwUpper = upper ?: it.pwUpper,
                pwLower = lower ?: it.pwLower,
                pwDigits = digits ?: it.pwDigits,
                pwSymbols = symbols ?: it.pwSymbols,
                pwAmbiguous = ambiguous ?: it.pwAmbiguous
            )
        }
        if (regenerate) generate()
    }

    fun updatePassphraseConfig(count: Int? = null, sep: String? = null, cap: Boolean? = null, num: Boolean? = null, regenerate: Boolean = true) {
        _state.update {
            it.copy(
                ppWordCount = count ?: it.ppWordCount,
                ppSeparator = sep ?: it.ppSeparator,
                ppCapitalize = cap ?: it.ppCapitalize,
                ppNumber = num ?: it.ppNumber
            )
        }
        if (regenerate) generate()
    }

    fun updateUsernameConfig(style: SecurityGenerator.UsernameStyle? = null, cap: Boolean? = null, num: Boolean? = null, regenerate: Boolean = true) {
        _state.update {
            it.copy(
                unStyle = style ?: it.unStyle,
                unCapitalize = cap ?: it.unCapitalize,
                unNumber = num ?: it.unNumber
            )
        }
        if (regenerate) generate()
    }
}
