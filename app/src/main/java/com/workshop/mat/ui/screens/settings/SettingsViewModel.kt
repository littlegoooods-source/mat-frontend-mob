package com.workshop.mat.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workshop.mat.data.api.ApiService
import com.workshop.mat.data.api.TokenManager
import com.workshop.mat.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val user: UserDto? = null,
    val organizations: List<OrganizationMembershipDto> = emptyList(),
    val currentOrgDetail: OrganizationDetailDto? = null,
    val invitations: List<InvitationDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Create org
    val showCreateOrgDialog: Boolean = false,
    val newOrgName: String = "",
    val newOrgDescription: String = "",
    // Join org
    val showJoinDialog: Boolean = false,
    val joinCode: String = "",
    // Invite
    val showInviteDialog: Boolean = false,
    val inviteEmail: String = "",
    // Transfer
    val showTransferDialog: Boolean = false,
    val transferMemberId: String = "",
    // Confirm dialogs
    val showLeaveConfirm: Boolean = false,
    val showDeleteOrgConfirm: Boolean = false,
    val showRemoveMemberConfirm: OrganizationMemberDto? = null,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun showSnackbar(msg: String) {
        _uiState.value = _uiState.value.copy(snackbarMessage = msg)
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = tokenManager.user
                val orgs = tokenManager.organizations
                _uiState.value = _uiState.value.copy(user = user, organizations = orgs)

                // Load current org details
                if (user?.currentOrganizationId != null) {
                    val orgResponse = apiService.getOrganizationById(user.currentOrganizationId)
                    if (orgResponse.isSuccessful) {
                        _uiState.value = _uiState.value.copy(currentOrgDetail = orgResponse.body())
                    }
                }

                // Load invitations
                val invResponse = apiService.getMyInvitations()
                if (invResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(invitations = invResponse.body() ?: emptyList())
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun switchOrganization(orgId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.switchOrganization(SwitchOrganizationRequest(orgId))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    tokenManager.setTokens(body.accessToken, body.refreshToken)
                    tokenManager.user = body.user
                    tokenManager.organizations = body.organizations ?: emptyList()
                    loadData()
                }
            } catch (e: Exception) {
                showSnackbar(e.localizedMessage ?: "Ошибка переключения организации")
            }
        }
    }

    // Create org
    fun openCreateOrgDialog() { _uiState.value = _uiState.value.copy(showCreateOrgDialog = true, newOrgName = "", newOrgDescription = "") }
    fun closeCreateOrgDialog() { _uiState.value = _uiState.value.copy(showCreateOrgDialog = false) }
    fun updateNewOrgName(v: String) { _uiState.value = _uiState.value.copy(newOrgName = v) }
    fun updateNewOrgDescription(v: String) { _uiState.value = _uiState.value.copy(newOrgDescription = v) }

    fun createOrganization() {
        val s = _uiState.value
        if (s.newOrgName.isBlank()) return
        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            try {
                val response = apiService.createOrganization(
                    CreateOrganizationRequest(s.newOrgName, s.newOrgDescription.ifBlank { null })
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, showCreateOrgDialog = false)
                    // Refresh orgs
                    val orgsResponse = apiService.getAuthOrganizations()
                    if (orgsResponse.isSuccessful) {
                        tokenManager.organizations = orgsResponse.body() ?: emptyList()
                    }
                    loadData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage)
            }
        }
    }

    // Join by code
    fun openJoinDialog() { _uiState.value = _uiState.value.copy(showJoinDialog = true, joinCode = "") }
    fun closeJoinDialog() { _uiState.value = _uiState.value.copy(showJoinDialog = false) }
    fun updateJoinCode(v: String) { _uiState.value = _uiState.value.copy(joinCode = v) }

    fun joinByCode() {
        val code = _uiState.value.joinCode
        if (code.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                apiService.joinByCode(JoinOrganizationRequest(code))
                _uiState.value = _uiState.value.copy(isSaving = false, showJoinDialog = false)
                val orgsResponse = apiService.getAuthOrganizations()
                if (orgsResponse.isSuccessful) {
                    tokenManager.organizations = orgsResponse.body() ?: emptyList()
                }
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage)
            }
        }
    }

    // Invite
    fun openInviteDialog() { _uiState.value = _uiState.value.copy(showInviteDialog = true, inviteEmail = "") }
    fun closeInviteDialog() { _uiState.value = _uiState.value.copy(showInviteDialog = false) }
    fun updateInviteEmail(v: String) { _uiState.value = _uiState.value.copy(inviteEmail = v) }

    fun sendInvite() {
        val orgId = _uiState.value.user?.currentOrganizationId ?: return
        val email = _uiState.value.inviteEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                apiService.invite(orgId, InviteRequest(email))
                _uiState.value = _uiState.value.copy(isSaving = false, showInviteDialog = false)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage)
            }
        }
    }

    // Accept/Reject invitation
    fun acceptInvitation(token: String) {
        viewModelScope.launch {
            try {
                apiService.acceptInvitation(token)
                val orgsResponse = apiService.getAuthOrganizations()
                if (orgsResponse.isSuccessful) {
                    tokenManager.organizations = orgsResponse.body() ?: emptyList()
                }
                loadData()
            } catch (e: Exception) {
                showSnackbar(e.localizedMessage ?: "Ошибка принятия приглашения")
            }
        }
    }

    fun rejectInvitation(token: String) {
        viewModelScope.launch {
            try {
                apiService.rejectInvitation(token)
                loadData()
            } catch (e: Exception) {
                showSnackbar(e.localizedMessage ?: "Ошибка отклонения приглашения")
            }
        }
    }

    // Leave organization
    fun showLeaveConfirm() { _uiState.value = _uiState.value.copy(showLeaveConfirm = true) }
    fun dismissLeaveConfirm() { _uiState.value = _uiState.value.copy(showLeaveConfirm = false) }

    fun leaveOrganization() {
        val orgId = _uiState.value.user?.currentOrganizationId ?: return
        viewModelScope.launch {
            try {
                apiService.leaveOrganization(orgId)
                _uiState.value = _uiState.value.copy(showLeaveConfirm = false)
                val orgsResponse = apiService.getAuthOrganizations()
                if (orgsResponse.isSuccessful) {
                    val orgs = orgsResponse.body() ?: emptyList()
                    tokenManager.organizations = orgs
                    val personal = orgs.find { it.isPersonal }
                    if (personal != null) switchOrganization(personal.organizationId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(showLeaveConfirm = false)
                showSnackbar(e.localizedMessage ?: "Ошибка выхода из организации")
            }
        }
    }

    // Remove member
    fun showRemoveMemberConfirm(member: OrganizationMemberDto) { _uiState.value = _uiState.value.copy(showRemoveMemberConfirm = member) }
    fun dismissRemoveMemberConfirm() { _uiState.value = _uiState.value.copy(showRemoveMemberConfirm = null) }

    fun removeMember() {
        val member = _uiState.value.showRemoveMemberConfirm ?: return
        val orgId = _uiState.value.user?.currentOrganizationId ?: return
        viewModelScope.launch {
            try {
                apiService.removeMember(orgId, member.id)
                _uiState.value = _uiState.value.copy(showRemoveMemberConfirm = null)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(showRemoveMemberConfirm = null)
                showSnackbar(e.localizedMessage ?: "Ошибка удаления участника")
            }
        }
    }

    // Delete org
    fun showDeleteOrgConfirm() { _uiState.value = _uiState.value.copy(showDeleteOrgConfirm = true) }
    fun dismissDeleteOrgConfirm() { _uiState.value = _uiState.value.copy(showDeleteOrgConfirm = false) }

    fun deleteOrganization() {
        val orgId = _uiState.value.user?.currentOrganizationId ?: return
        viewModelScope.launch {
            try {
                apiService.deleteOrganization(orgId)
                _uiState.value = _uiState.value.copy(showDeleteOrgConfirm = false)
                val orgsResponse = apiService.getAuthOrganizations()
                if (orgsResponse.isSuccessful) {
                    val orgs = orgsResponse.body() ?: emptyList()
                    tokenManager.organizations = orgs
                    val personal = orgs.find { it.isPersonal }
                    if (personal != null) switchOrganization(personal.organizationId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(showDeleteOrgConfirm = false)
                showSnackbar(e.localizedMessage ?: "Ошибка удаления организации")
            }
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            try { apiService.logout(RefreshTokenRequest(tokenManager.refreshToken ?: "")) } catch (_: Exception) {}
            tokenManager.clearAll()
        }
    }
}
