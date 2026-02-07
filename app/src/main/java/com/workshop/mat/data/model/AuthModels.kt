package com.workshop.mat.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String
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
    val name: String = "",
    val email: String = "",
    val currentOrganizationId: Int? = null,
    val currentOrganizationName: String? = null,
    val role: String? = null
)

data class OrganizationMembershipDto(
    val organizationId: Int = 0,
    val organizationName: String = "",
    val role: String = "",
    val isPersonal: Boolean = false,
    val joinCode: String? = null
)
