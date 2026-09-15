package com.example.xuimanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)

    val fontSizeScale: StateFlow<Float> = repository.fontSizeScale.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.0f
    )

    val selectedLanguage: StateFlow<String> = repository.selectedLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Русский"
    )

    val alertCpuThreshold: StateFlow<Float> = repository.alertCpuThreshold.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 85f
    )

    val isBiometricEnabled: StateFlow<Boolean> = repository.isBiometricEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val pinCode: StateFlow<String> = repository.pinCode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val isPinEnabled: StateFlow<Boolean> = repository.isPinEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun updateFontSizeScale(scale: Float) {
        viewModelScope.launch {
            repository.setFontSizeScale(scale)
        }
    }

    fun updateSelectedLanguage(language: String) {
        viewModelScope.launch {
            repository.setSelectedLanguage(language)
        }
    }

    fun updateAlertCpuThreshold(threshold: Float) {
        viewModelScope.launch {
            repository.setAlertCpuThreshold(threshold)
        }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricEnabled(enabled)
        }
    }

    fun savePinCode(pin: String) {
        viewModelScope.launch {
            repository.setPinCode(pin)
            repository.setPinEnabled(pin.isNotBlank())
        }
    }

    fun updatePinEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPinEnabled(enabled)
        }
    }
}