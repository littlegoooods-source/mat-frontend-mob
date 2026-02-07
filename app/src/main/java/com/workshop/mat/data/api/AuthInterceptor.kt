package com.workshop.mat.data.api

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Don't add auth header to login/register/refresh requests
        val path = originalRequest.url.encodedPath
        if (path.contains("/auth/login") || path.contains("/auth/register") || path.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        val token = tokenManager.accessToken
        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        // Handle 401 - try to refresh token
        if (response.code == 401 && token != null) {
            response.close()
            val newToken = runBlocking { refreshToken() }
            if (newToken != null) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            } else {
                // Refresh failed, clear tokens
                tokenManager.clearAll()
            }
        }

        return response
    }

    private suspend fun refreshToken(): String? {
        val refreshToken = tokenManager.refreshToken ?: return null
        try {
            val client = okhttp3.OkHttpClient()
            val json = """{"refreshToken":"$refreshToken"}"""
            val body = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("application/json"),
                json
            )
            val request = okhttp3.Request.Builder()
                .url("https://mat-backend-r9iw.onrender.com/api/auth/refresh")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return null
                val gson = com.google.gson.Gson()
                val authResponse = gson.fromJson(responseBody, com.workshop.mat.data.model.AuthResponse::class.java)
                tokenManager.setTokens(authResponse.accessToken, authResponse.refreshToken)
                return authResponse.accessToken
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
