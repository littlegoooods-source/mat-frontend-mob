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
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val joinCode: String = "",
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

    fun updateFirstName(v: String) { _uiState.value = _uiState.value.copy(firstName = v, error = null) }
    fun updateLastName(v: String) { _uiState.value = _uiState.value.copy(lastName = v, error = null) }
    fun updateUsername(v: String) { _uiState.value = _uiState.value.copy(username = v, error = null) }
    fun updateEmail(v: String) { _uiState.value = _uiState.value.copy(email = v, error = null) }
    fun updatePassword(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }
    fun updateConfirmPassword(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, error = null) }
    fun updateJoinCode(v: String) { _uiState.value = _uiState.value.copy(joinCode = v, error = null) }

    fun register() {
        val state = _uiState.value
        if (state.username.isBlank()) {
            _uiState.value = state.copy(error = "Укажите имя пользователя")
            return
        }
        if (state.email.isBlank()) {
            _uiState.value = state.copy(error = "Укажите email")
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = "Укажите пароль")
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
                    RegisterRequest(
                        username = state.username,
                        email = state.email,
                        password = state.password,
                        firstName = state.firstName.ifBlank { null },
                        lastName = state.lastName.ifBlank { null },
                        joinCode = state.joinCode.ifBlank { null }
                    )
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
                        val errors = json.getAsJsonObject("errors")
                        if (errors != null) {
                            val msgs = mutableListOf<String>()
                            errors.entrySet().forEach { (_, v) ->
                                v.asJsonArray.forEach { msgs.add(it.asString) }
                            }
                            msgs.joinToString("\n")
                        } else {
                            json.get("message")?.asString ?: json.get("title")?.asString ?: "Ошибка регистрации"
                        }
                    } catch (_: Exception) {
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
