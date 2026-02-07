package com.workshop.mat.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workshop.mat.data.api.ApiService
import com.workshop.mat.data.api.TokenManager
import com.workshop.mat.data.model.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateName(name: String) { _uiState.value = _uiState.value.copy(name = name, error = null) }
    fun updateEmail(email: String) { _uiState.value = _uiState.value.copy(email = email, error = null) }
    fun updatePassword(password: String) { _uiState.value = _uiState.value.copy(password = password, error = null) }
    fun updateConfirmPassword(password: String) { _uiState.value = _uiState.value.copy(confirmPassword = password, error = null) }

    fun register() {
        val state = _uiState.value
        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Заполните все поля")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Пароли не совпадают")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Пароль должен быть не менее 6 символов")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.register(
                    RegisterRequest(state.name, state.email, state.password, state.confirmPassword)
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    tokenManager.setTokens(body.accessToken, body.refreshToken)
                    tokenManager.user = body.user
                    tokenManager.organizations = body.organizations ?: emptyList()
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        json.get("message")?.asString ?: json.get("title")?.asString ?: "Ошибка регистрации"
                    } catch (e: Exception) {
                        "Ошибка регистрации"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка сети: ${e.localizedMessage ?: "проверьте подключение"}"
                )
            }
        }
    }
}
