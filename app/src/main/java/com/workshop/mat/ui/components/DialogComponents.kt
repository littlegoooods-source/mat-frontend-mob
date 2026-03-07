package com.workshop.mat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.workshop.mat.ui.theme.*

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Подтвердить",
    dismissText: String = "Отмена",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary) },
        text = { Text(message, color = TextSecondary) },
        containerColor = DarkSurface,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) Error else Primary
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = TextSecondary)
            }
        }
    )
}

@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    notificationMessage: String? = null,
    onDismissNotification: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        content = content
                    )
                }
            }

            if (onDismissNotification != null) {
                NotificationBanner(
                    message = notificationMessage,
                    onDismiss = onDismissNotification,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun SmallDialog(
    title: String,
    onDismiss: () -> Unit,
    notificationMessage: String? = null,
    onDismissNotification: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }
            }

            if (onDismissNotification != null) {
                NotificationBanner(
                    message = notificationMessage,
                    onDismiss = onDismissNotification,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
