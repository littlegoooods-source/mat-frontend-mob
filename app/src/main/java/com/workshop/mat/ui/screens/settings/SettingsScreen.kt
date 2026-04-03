package com.workshop.mat.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*

private enum class SettingsTab(val label: String) {
    ORGANIZATIONS("Организации"),
    INVITATIONS("Приглашения"),
    PROFILE("Профиль")
}

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(SettingsTab.ORGANIZATIONS) }

    Box(modifier = Modifier.fillMaxSize()) {
        NotificationBanner(
            message = uiState.snackbarMessage,
            onDismiss = viewModel::clearSnackbar
        )

        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = SettingsTab.entries.indexOf(selectedTab),
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f)) }
            ) {
                SettingsTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                tab.label,
                                color = if (selectedTab == tab) SelectionOrange else TextSecondary,
                                fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = SelectionOrange,
                        unselectedContentColor = TextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    SettingsTab.ORGANIZATIONS -> OrganizationsTab(uiState, viewModel, clipboardManager)
                    SettingsTab.INVITATIONS -> InvitationsTab(uiState, viewModel)
                    SettingsTab.PROFILE -> ProfileTab(uiState, viewModel, onLogout)
                }
            }
        }
    }

    if (uiState.showCreateOrgDialog) {
        SmallDialog(
            title = "Новая организация",
            onDismiss = viewModel::closeCreateOrgDialog,
            notificationMessage = uiState.snackbarMessage,
            onDismissNotification = viewModel::clearSnackbar
        ) {
            AppTextField(value = uiState.newOrgName, onValueChange = viewModel::updateNewOrgName, label = "Название")
            Spacer(modifier = Modifier.height(12.dp))
            AppTextField(value = uiState.newOrgDescription, onValueChange = viewModel::updateNewOrgDescription, label = "Описание", singleLine = false)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::createOrganization, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = !uiState.isSaving) { Text("Создать") }
                OutlinedButton(onClick = viewModel::closeCreateOrgDialog, modifier = Modifier.weight(1f)) { Text("Отмена", color = TextSecondary) }
            }
        }
    }

    if (uiState.showJoinDialog) {
        SmallDialog(
            title = "Присоединиться",
            onDismiss = viewModel::closeJoinDialog,
            notificationMessage = uiState.snackbarMessage,
            onDismissNotification = viewModel::clearSnackbar
        ) {
            AppTextField(value = uiState.joinCode, onValueChange = viewModel::updateJoinCode, label = "Код организации")
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::joinByCode, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = !uiState.isSaving) { Text("Присоединиться") }
                OutlinedButton(onClick = viewModel::closeJoinDialog, modifier = Modifier.weight(1f)) { Text("Отмена", color = TextSecondary) }
            }
        }
    }

    if (uiState.showInviteDialog) {
        SmallDialog(
            title = "Пригласить участника",
            onDismiss = viewModel::closeInviteDialog,
            notificationMessage = uiState.snackbarMessage,
            onDismissNotification = viewModel::clearSnackbar
        ) {
            AppTextField(value = uiState.inviteEmail, onValueChange = viewModel::updateInviteEmail, label = "Email", keyboardType = KeyboardType.Email)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::sendInvite, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = !uiState.isSaving) { Text("Пригласить") }
                OutlinedButton(onClick = viewModel::closeInviteDialog, modifier = Modifier.weight(1f)) { Text("Отмена", color = TextSecondary) }
            }
        }
    }

    if (uiState.showRenameOrgDialog) {
        SmallDialog(
            title = "Переименовать организацию",
            onDismiss = viewModel::closeRenameOrgDialog,
            notificationMessage = uiState.snackbarMessage,
            onDismissNotification = viewModel::clearSnackbar
        ) {
            AppTextField(value = uiState.renameOrgName, onValueChange = viewModel::updateRenameOrgName, label = "Новое название")
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::renameOrganization, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = !uiState.isSaving) { Text("Сохранить") }
                OutlinedButton(onClick = viewModel::closeRenameOrgDialog, modifier = Modifier.weight(1f)) { Text("Отмена", color = TextSecondary) }
            }
        }
    }

    if (uiState.showTransferDialog) {
        SmallDialog(
            title = "Передать владение",
            onDismiss = viewModel::closeTransferDialog,
            notificationMessage = uiState.snackbarMessage,
            onDismissNotification = viewModel::clearSnackbar
        ) {
            Text("Выберите нового владельца:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            uiState.currentOrgDetail?.members?.filter { it.userId != uiState.user?.id }?.forEach { member ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.updateTransferMemberId(member.userId.toString()) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (uiState.transferMemberId == member.userId.toString()) SelectionOrangeBg else DarkSurfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.userName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text(member.userEmail, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        if (uiState.transferMemberId == member.userId.toString()) {
                            Icon(Icons.Default.Check, null, tint = SelectionOrange, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::transferOwnership,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Warning),
                    enabled = !uiState.isSaving && uiState.transferMemberId.isNotBlank()
                ) { Text("Передать") }
                OutlinedButton(onClick = viewModel::closeTransferDialog, modifier = Modifier.weight(1f)) { Text("Отмена", color = TextSecondary) }
            }
        }
    }

    if (uiState.showLeaveConfirm) {
        ConfirmDialog(
            title = "Покинуть организацию",
            message = "Вы уверены, что хотите покинуть организацию?",
            confirmText = "Покинуть",
            onConfirm = viewModel::leaveOrganization,
            onDismiss = viewModel::dismissLeaveConfirm,
            isDestructive = true
        )
    }

    if (uiState.showDeleteOrgConfirm) {
        ConfirmDialog(
            title = "Удалить организацию",
            message = "Удалить организацию? Это действие нельзя отменить.",
            confirmText = "Удалить",
            onConfirm = viewModel::deleteOrganization,
            onDismiss = viewModel::dismissDeleteOrgConfirm,
            isDestructive = true
        )
    }

    uiState.showRemoveMemberConfirm?.let { member ->
        ConfirmDialog(
            title = "Удалить участника",
            message = "Удалить \"${member.userName}\" из организации?",
            confirmText = "Удалить",
            onConfirm = viewModel::removeMember,
            onDismiss = viewModel::dismissRemoveMemberConfirm,
            isDestructive = true
        )
    }
}

// ==================== ORGANIZATIONS TAB ====================

@Composable
private fun OrganizationsTab(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = viewModel::openCreateOrgDialog,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Создать")
        }
        OutlinedButton(
            onClick = viewModel::openJoinDialog,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.GroupAdd, null, tint = Primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Присоединиться", color = TextPrimary)
        }
    }

    uiState.organizations.forEach { org ->
        val isCurrent = org.organizationId == uiState.user?.currentOrganizationId
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (!isCurrent) viewModel.switchOrganization(org.organizationId) },
            shape = RoundedCornerShape(12.dp),
            color = if (isCurrent) SelectionOrangeBg else DarkCard
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(50), color = if (isCurrent) SelectionOrange.copy(alpha = 0.2f) else DarkSurfaceVariant) {
                    Icon(
                        if (org.isPersonal) Icons.Default.Person else Icons.Default.Business,
                        null,
                        tint = if (isCurrent) SelectionOrange else TextMuted,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(org.organizationName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusBadge(text = org.role, color = if (org.role == "Owner") SelectionOrange else Primary, bgColor = if (org.role == "Owner") SelectionOrangeBg else Primary.copy(alpha = 0.15f))
                        if (org.isPersonal) {
                            Text("Личная", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
                if (isCurrent) {
                    Icon(Icons.Default.CheckCircle, null, tint = SelectionOrange, modifier = Modifier.size(22.dp))
                }
            }
        }
    }

    uiState.currentOrgDetail?.let { org ->
        if (!org.isPersonal) {
            if (org.joinCode != null) {
                AppCard {
                    Text("Код для приглашения", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                org.joinCode,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Info,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(org.joinCode))
                                viewModel.showSnackbar("Код скопирован")
                            }) {
                                Icon(Icons.Default.ContentCopy, null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            if (uiState.user?.role == "Owner") {
                                IconButton(onClick = viewModel::regenerateCode) {
                                    Icon(Icons.Default.Refresh, null, tint = Warning, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Участники (${org.members.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    if (uiState.user?.role == "Owner") {
                        IconButton(onClick = viewModel::openInviteDialog) {
                            Icon(Icons.Default.PersonAdd, null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                org.members.forEach { member ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(50), color = Primary.copy(alpha = 0.15f)) {
                                Text(
                                    member.userName.take(1).uppercase(),
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.userName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(member.userEmail, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            StatusBadge(
                                text = member.role,
                                color = if (member.role == "Owner") SelectionOrange else Primary,
                                bgColor = if (member.role == "Owner") SelectionOrangeBg else Primary.copy(alpha = 0.15f)
                            )
                            if (uiState.user?.role == "Owner" && member.userId != uiState.user?.id) {
                                IconButton(onClick = { viewModel.showRemoveMemberConfirm(member) }) {
                                    Icon(Icons.Default.PersonRemove, null, tint = Error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.user?.role == "Owner") {
                AppCard {
                    Text("Управление организацией", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = viewModel::openRenameOrgDialog,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Переименовать", color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (org.members.size > 1) {
                        OutlinedButton(
                            onClick = viewModel::openTransferDialog,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, null, tint = Warning, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Передать владение", color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = viewModel::showDeleteOrgConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Удалить организацию")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = viewModel::showLeaveConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, null, tint = Warning, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Покинуть организацию", color = Warning)
                }
            }
        }
    }
}

// ==================== INVITATIONS TAB ====================

@Composable
private fun InvitationsTab(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    Text("Входящие приглашения", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
    if (uiState.invitations.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = DarkCard
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.MailOutline, null, tint = TextMuted, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Нет входящих приглашений", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
    } else {
        uiState.invitations.forEach { inv ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkCard
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(50), color = Primary.copy(alpha = 0.15f)) {
                        Icon(
                            Icons.Default.Business, null, tint = Primary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(inv.organizationName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                        Text("От: ${inv.invitedByName ?: "—"}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    IconButton(onClick = { viewModel.acceptInvitation(inv.token) }) {
                        Icon(Icons.Default.Check, null, tint = Success, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { viewModel.rejectInvitation(inv.token) }) {
                        Icon(Icons.Default.Close, null, tint = Error, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text("Отправленные приглашения", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
    if (uiState.sentInvitations.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = DarkCard
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Send, null, tint = TextMuted, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Нет отправленных приглашений", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
    } else {
        uiState.sentInvitations.forEach { inv ->
            val statusColor = when (inv.status.lowercase()) {
                "pending" -> Warning
                "accepted" -> Success
                "rejected" -> Error
                else -> TextMuted
            }
            val statusText = when (inv.status.lowercase()) {
                "pending" -> "Ожидание"
                "accepted" -> "Принято"
                "rejected" -> "Отклонено"
                "expired" -> "Истекло"
                else -> inv.status
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkCard
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(inv.invitedEmail, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusBadge(text = statusText, color = statusColor, bgColor = statusColor.copy(alpha = 0.15f))
                            Text(inv.createdAt.take(10), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                    if (inv.status.lowercase() == "pending") {
                        IconButton(onClick = { viewModel.cancelSentInvitation(inv.id) }) {
                            Icon(Icons.Default.Cancel, null, tint = Error, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==================== PROFILE TAB ====================

@Composable
private fun ProfileTab(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(50), color = Primary.copy(alpha = 0.15f)) {
                Text(
                    (uiState.user?.name?.take(1) ?: "?").uppercase(),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    uiState.user?.name ?: "",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(uiState.user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }

    AppCard {
        Text("Данные профиля", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        ProfileRow("Имя пользователя", uiState.user?.name ?: "—")
        ProfileRow("Email", uiState.user?.email ?: "—")
        if (uiState.user?.currentOrganizationName != null) {
            ProfileRow("Текущая организация", uiState.user.currentOrganizationName)
        }
        if (uiState.user?.role != null) {
            ProfileRow("Роль", uiState.user.role)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            viewModel.logout()
            onLogout()
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Error),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Выйти из аккаунта")
    }

    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}
