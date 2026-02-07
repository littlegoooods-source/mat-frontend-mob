package com.workshop.mat.data.api

import com.workshop.mat.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ====================
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Unit>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun me(): Response<UserDto>

    @GET("auth/organizations")
    suspend fun getAuthOrganizations(): Response<List<OrganizationMembershipDto>>

    @POST("auth/switch-organization")
    suspend fun switchOrganization(@Body request: SwitchOrganizationRequest): Response<AuthResponse>

    // ==================== ORGANIZATIONS ====================
    @GET("organizations")
    suspend fun getOrganizations(): Response<List<OrganizationDto>>

    @GET("organizations/{id}")
    suspend fun getOrganizationById(@Path("id") id: Int): Response<OrganizationDetailDto>

    @POST("organizations")
    suspend fun createOrganization(@Body request: CreateOrganizationRequest): Response<OrganizationDto>

    @PUT("organizations/{id}")
    suspend fun updateOrganization(@Path("id") id: Int, @Body request: UpdateOrganizationRequest): Response<OrganizationDto>

    @DELETE("organizations/{id}")
    suspend fun deleteOrganization(@Path("id") id: Int): Response<Unit>

    @POST("organizations/{id}/regenerate-code")
    suspend fun regenerateCode(@Path("id") id: Int): Response<OrganizationDto>

    @POST("organizations/join")
    suspend fun joinByCode(@Body request: JoinOrganizationRequest): Response<OrganizationMembershipDto>

    @GET("organizations/{id}/members")
    suspend fun getMembers(@Path("id") id: Int): Response<List<OrganizationMemberDto>>

    @DELETE("organizations/{orgId}/members/{memberId}")
    suspend fun removeMember(@Path("orgId") orgId: Int, @Path("memberId") memberId: Int): Response<Unit>

    @POST("organizations/{id}/leave")
    suspend fun leaveOrganization(@Path("id") id: Int): Response<Unit>

    @POST("organizations/{id}/transfer-ownership")
    suspend fun transferOwnership(@Path("id") id: Int, @Body request: TransferOwnershipRequest): Response<Unit>

    @POST("organizations/{id}/invite")
    suspend fun invite(@Path("id") id: Int, @Body request: InviteRequest): Response<InvitationDto>

    @GET("organizations/{id}/invitations")
    suspend fun getOrgInvitations(@Path("id") id: Int): Response<List<InvitationDto>>

    // ==================== INVITATIONS ====================
    @GET("invitations")
    suspend fun getMyInvitations(): Response<List<InvitationDto>>

    @POST("invitations/{token}/accept")
    suspend fun acceptInvitation(@Path("token") token: String): Response<OrganizationMembershipDto>

    @POST("invitations/{token}/reject")
    suspend fun rejectInvitation(@Path("token") token: String): Response<Unit>

    @DELETE("invitations/{id}")
    suspend fun cancelInvitation(@Path("id") id: Int): Response<Unit>

    // ==================== MATERIALS ====================
    @GET("materials")
    suspend fun getMaterials(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("includeArchived") includeArchived: Boolean = false
    ): Response<List<MaterialListItemDto>>

    @GET("materials/{id}")
    suspend fun getMaterialById(@Path("id") id: Int): Response<MaterialResponseDto>

    @POST("materials")
    suspend fun createMaterial(@Body data: MaterialCreateDto): Response<MaterialResponseDto>

    @PUT("materials/{id}")
    suspend fun updateMaterial(@Path("id") id: Int, @Body data: MaterialUpdateDto): Response<MaterialResponseDto>

    @DELETE("materials/{id}")
    suspend fun deleteMaterial(@Path("id") id: Int): Response<Unit>

    @POST("materials/{id}/archive")
    suspend fun archiveMaterial(@Path("id") id: Int): Response<Unit>

    @POST("materials/{id}/unarchive")
    suspend fun unarchiveMaterial(@Path("id") id: Int): Response<Unit>

    @GET("materials/categories")
    suspend fun getMaterialCategories(): Response<List<String>>

    @GET("materials/balances")
    suspend fun getMaterialBalances(@Query("includeZeroStock") includeZeroStock: Boolean = false): Response<List<MaterialBalanceDto>>

    @GET("materials/{id}/balance")
    suspend fun getMaterialBalance(@Path("id") id: Int): Response<MaterialBalanceDto>

    @GET("materials/{id}/products")
    suspend fun getMaterialProducts(@Path("id") id: Int): Response<List<ProductListItemDto>>

    // ==================== MATERIAL RECEIPTS ====================
    @GET("materialreceipts")
    suspend fun getReceipts(
        @Query("materialId") materialId: Int? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<List<MaterialReceiptListItemDto>>

    @GET("materialreceipts/{id}")
    suspend fun getReceiptById(@Path("id") id: Int): Response<MaterialReceiptResponseDto>

    @POST("materialreceipts")
    suspend fun createReceipt(@Body data: MaterialReceiptCreateDto): Response<MaterialReceiptResponseDto>

    @PUT("materialreceipts/{id}")
    suspend fun updateReceipt(@Path("id") id: Int, @Body data: MaterialReceiptUpdateDto): Response<MaterialReceiptResponseDto>

    @DELETE("materialreceipts/{id}")
    suspend fun deleteReceipt(@Path("id") id: Int, @Query("force") force: Boolean = false): Response<Unit>

    // ==================== PRODUCTS ====================
    @GET("products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("includeArchived") includeArchived: Boolean = false
    ): Response<List<ProductListItemDto>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<ProductResponseDto>

    @POST("products")
    suspend fun createProduct(@Body data: ProductCreateDto): Response<ProductResponseDto>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body data: ProductUpdateDto): Response<ProductResponseDto>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>

    @POST("products/{id}/copy")
    suspend fun copyProduct(@Path("id") id: Int, @Body data: ProductCopyDto): Response<ProductResponseDto>

    @POST("products/{id}/archive")
    suspend fun archiveProduct(@Path("id") id: Int): Response<Unit>

    @POST("products/{id}/unarchive")
    suspend fun unarchiveProduct(@Path("id") id: Int): Response<Unit>

    @GET("products/categories")
    suspend fun getProductCategories(): Response<List<String>>

    @POST("products/{id}/recalculate-weight")
    suspend fun recalculateWeight(@Path("id") id: Int): Response<ProductResponseDto>

    // ==================== PRODUCTIONS ====================
    @GET("productions")
    suspend fun getProductions(
        @Query("status") status: String? = null,
        @Query("productId") productId: Int? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<List<ProductionListItemDto>>

    @GET("productions/{id}")
    suspend fun getProductionById(@Path("id") id: Int): Response<ProductionResponseDto>

    @GET("productions/check-availability")
    suspend fun checkAvailability(
        @Query("productId") productId: Int,
        @Query("quantity") quantity: Int
    ): Response<ProductionAvailabilityDto>

    @POST("productions")
    suspend fun createProduction(@Body data: ProductionCreateDto): Response<ProductionResponseDto>

    @POST("productions/{id}/cancel")
    suspend fun cancelProduction(@Path("id") id: Int): Response<ProductionResponseDto>

    @DELETE("productions/{id}")
    suspend fun deleteProduction(@Path("id") id: Int): Response<Unit>

    // ==================== FINISHED PRODUCTS ====================
    @GET("finishedproducts")
    suspend fun getFinishedProducts(
        @Query("status") status: String? = null,
        @Query("productId") productId: Int? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<List<FinishedProductListItemDto>>

    @GET("finishedproducts/{id}")
    suspend fun getFinishedProductById(@Path("id") id: Int): Response<FinishedProductResponseDto>

    @POST("finishedproducts/{id}/sell")
    suspend fun sellProduct(@Path("id") id: Int, @Body data: SellProductDto): Response<FinishedProductResponseDto>

    @POST("finishedproducts/{id}/write-off")
    suspend fun writeOffProduct(@Path("id") id: Int, @Body data: WriteOffProductDto): Response<FinishedProductResponseDto>

    @POST("finishedproducts/{id}/return-to-stock")
    suspend fun returnToStock(@Path("id") id: Int): Response<FinishedProductResponseDto>

    @PUT("finishedproducts/{id}")
    suspend fun updateFinishedProduct(@Path("id") id: Int, @Body data: FinishedProductUpdateDto): Response<FinishedProductResponseDto>

    @DELETE("finishedproducts/{id}")
    suspend fun deleteFinishedProduct(@Path("id") id: Int): Response<Unit>

    @GET("finishedproducts/summary")
    suspend fun getFinishedProductsSummary(): Response<FinishedProductSummaryDto>

    // ==================== REPORTS ====================
    @GET("reports/dashboard")
    suspend fun getDashboard(): Response<DashboardDto>

    // ==================== HISTORY ====================
    @GET("history")
    suspend fun getHistory(
        @Query("operationType") operationType: String? = null,
        @Query("entityType") entityType: String? = null,
        @Query("entityId") entityId: Int? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("includeCancelled") includeCancelled: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<PagedResultDto<OperationHistoryItemDto>>

    @GET("history/recent")
    suspend fun getRecentHistory(@Query("count") count: Int = 10): Response<List<OperationHistoryItemDto>>
}
