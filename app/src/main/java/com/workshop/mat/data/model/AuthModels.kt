package com.workshop.mat.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val joinCode: String? = null
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class SwitchOrganizationRequest(
    val organizationId: Int
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
    val organizations: List<OrganizationMembershipDto>? = null
)

data class UserDto(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val fullName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val currentOrganizationId: Int? = null,
    val currentOrganizationName: String? = null,
    val currentOrganizationRole: String? = null
) {
    val name: String get() = fullName?.takeIf { it.isNotBlank() } ?: username
    val role: String? get() = currentOrganizationRole
}

data class OrganizationMembershipDto(
    val organizationId: Int = 0,
    val organizationName: String = "",
    val role: String = "",
    val isPersonal: Boolean = false
)
