package com.workshop.mat.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workshop.mat.data.api.ApiService
import com.workshop.mat.data.api.TokenManager
import com.workshop.mat.data.model.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.login(LoginRequest(state.email, state.password))
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
                        json.get("message")?.asString ?: json.get("title")?.asString ?: "Ошибка при входе"
                    } catch (e: Exception) {
                        "Ошибка при входе"
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

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}
