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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workshop.mat.ui.components.*
import com.workshop.mat.ui.theme.*

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)

        // User info
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(50), color = Primary.copy(alpha = 0.15f)) {
                    Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(uiState.user?.name ?: "", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(uiState.user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    if (uiState.user?.currentOrganizationName != null) {
                        Text(
                            "Организация: ${uiState.user?.currentOrganizationName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                }
            }
        }

        // Organizations
        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Организации", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = viewModel::openCreateOrgDialog) {
                        Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = viewModel::openJoinDialog) {
                        Icon(Icons.Default.GroupAdd, null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            uiState.organizations.forEach { org ->
                val isCurrent = org.organizationId == uiState.user?.currentOrganizationId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { if (!isCurrent) viewModel.switchOrganization(org.organizationId) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCurrent) Primary.copy(alpha = 0.15f) else DarkSurfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(org.organizationName, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(
                                "${org.role}${if (org.isPersonal) " (личная)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        if (isCurrent) {
                            Icon(Icons.Default.Check, null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Current org members
        uiState.currentOrgDetail?.let { org ->
            if (!org.isPersonal) {
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Участники: ${org.name}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                        if (uiState.user?.role == "Owner") {
                            IconButton(onClick = viewModel::openInviteDialog) {
                                Icon(Icons.Default.PersonAdd, null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    if (org.joinCode != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Код присоединения: ${org.joinCode}", style = MaterialTheme.typography.bodySmall, color = Info)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    org.members.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.userName, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                                Text("${member.userEmail} (${member.role})", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            if (uiState.user?.role == "Owner" && member.userId != uiState.user?.id) {
                                IconButton(onClick = { viewModel.showRemoveMemberConfirm(member) }) {
                                    Icon(Icons.Default.PersonRemove, null, tint = Error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Org actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (uiState.user?.role != "Owner") {
                            OutlinedButton(onClick = viewModel::showLeaveConfirm) {
                                Text("Покинуть", color = Warning)
                            }
                        }
                        if (uiState.user?.role == "Owner") {
                            OutlinedButton(onClick = viewModel::showDeleteOrgConfirm) {
                                Text("Удалить организацию", color = Error)
                            }
                        }
                    }
                }
            }
        }

        // Invitations
        if (uiState.invitations.isNotEmpty()) {
            AppCard {
                Text("Приглашения", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                uiState.invitations.forEach { inv ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(inv.organizationName, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text("От: ${inv.invitedByName ?: "?"}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.acceptInvitation(inv.token) }) {
                                Icon(Icons.Default.Check, null, tint = Success, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.rejectInvitation(inv.token) }) {
                                Icon(Icons.Default.Close, null, tint = Error, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Logout
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

    // Dialogs
    if (uiState.showCreateOrgDialog) {
        SmallDialog(title = "Новая организация", onDismiss = viewModel::closeCreateOrgDialog) {
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
        SmallDialog(title = "Присоединиться", onDismiss = viewModel::closeJoinDialog) {
            AppTextField(value = uiState.joinCode, onValueChange = viewModel::updateJoinCode, label = "Код организации")
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::joinByCode, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = !uiState.isSaving) { Text("Присоединиться") }
                OutlinedButton(onClick = viewModel::closeJoinDialog, modifier = Modifier.weight(1f)) { Text("Отмена", color = TextSecondary) }
            }
        }
    }

    if (uiState.showInviteDialog) {
        SmallDialog(title = "Пригласить участника", onDismiss = viewModel::closeInviteDialog) {
            AppTextField(value = uiState.inviteEmail, onValueChange = viewModel::updateInviteEmail, label = "Email", keyboardType = KeyboardType.Email)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::sendInvite, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary), enabled = !uiState.isSaving) { Text("Пригласить") }
                OutlinedButton(onClick = viewModel::closeInviteDialog, modifier = Modifier.weight(1f)) { Text("Отмена", color = TextSecondary) }
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
