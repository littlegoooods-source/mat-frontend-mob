package com.workshop.mat.data.model

data class OrganizationDto(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val isPersonal: Boolean = false,
    val joinCode: String? = null,
    val memberCount: Int = 0,
    val createdAt: String = ""
)

data class OrganizationDetailDto(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val isPersonal: Boolean = false,
    val joinCode: String? = null,
    val ownerId: Int = 0,
    val ownerName: String? = null,
    val members: List<OrganizationMemberDto> = emptyList(),
    val createdAt: String = ""
)

data class OrganizationMemberDto(
    val id: Int = 0,
    val userId: Int = 0,
    val email: String = "",
    val username: String = "",
    val fullName: String = "",
    val role: String = "",
    val joinedAt: String = ""
) {
    val userName: String get() = fullName.takeIf { it.isNotBlank() } ?: username.takeIf { it.isNotBlank() } ?: email
    val userEmail: String get() = email
}

data class CreateOrganizationRequest(
    val name: String,
    val description: String? = null
)

data class UpdateOrganizationRequest(
    val name: String,
    val description: String? = null
)

data class JoinOrganizationRequest(
    val joinCode: String
)

data class InviteRequest(
    val email: String
)

data class TransferOwnershipRequest(
    val newOwnerId: Int
)

data class InvitationDto(
    val id: Int = 0,
    val token: String = "",
    val organizationId: Int = 0,
    val organizationName: String = "",
    val invitedByName: String? = null,
    val email: String = "",
    val status: String = "",
    val createdAt: String = "",
    val expiresAt: String? = null
)
