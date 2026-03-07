package com.workshop.mat.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.ui.components.AppTextField
import com.workshop.mat.ui.theme.*

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Мастерская",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Создание аккаунта",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Регистрация",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )

                    if (uiState.error != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorBg
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = Error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppTextField(
                            value = uiState.firstName,
                            onValueChange = viewModel::updateFirstName,
                            label = "Имя",
                            placeholder = "Иван",
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        )
                        AppTextField(
                            value = uiState.lastName,
                            onValueChange = viewModel::updateLastName,
                            label = "Фамилия",
                            placeholder = "Иванов",
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AppTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        label = "Email *",
                        placeholder = "Введите email",
                        keyboardType = KeyboardType.Email,
                        enabled = !uiState.isLoading
                    )

                    AppTextField(
                        value = uiState.username,
                        onValueChange = viewModel::updateUsername,
                        label = "Имя пользователя *",
                        placeholder = "Введите имя пользователя",
                        enabled = !uiState.isLoading
                    )

                    AppTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = "Пароль *",
                        placeholder = "Минимум 6 символов",
                        isPassword = true,
                        enabled = !uiState.isLoading
                    )

                    AppTextField(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::updateConfirmPassword,
                        label = "Подтверждение пароля *",
                        placeholder = "Повторите пароль",
                        isPassword = true,
                        enabled = !uiState.isLoading
                    )

                    AppTextField(
                        value = uiState.joinCode,
                        onValueChange = viewModel::updateJoinCode,
                        label = "Код организации",
                        placeholder = "Опционально",
                        enabled = !uiState.isLoading
                    )

                    Button(
                        onClick = viewModel::register,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Зарегистрироваться", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Уже есть аккаунт? ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        "Войти",
                        color = Primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
