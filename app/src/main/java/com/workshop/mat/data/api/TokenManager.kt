package com.workshop.mat.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.workshop.mat.data.model.OrganizationMembershipDto
import com.workshop.mat.data.model.UserDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val gson = Gson()

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "workshop_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to regular shared prefs if encryption fails
        context.getSharedPreferences("workshop_prefs", Context.MODE_PRIVATE)
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var user: UserDto?
        get() {
            val json = prefs.getString(KEY_USER, null) ?: return null
            return try { gson.fromJson(json, UserDto::class.java) } catch (e: Exception) { null }
        }
        set(value) = prefs.edit().putString(KEY_USER, if (value != null) gson.toJson(value) else null).apply()

    var organizations: List<OrganizationMembershipDto>
        get() {
            val json = prefs.getString(KEY_ORGANIZATIONS, null) ?: return emptyList()
            return try {
                gson.fromJson(json, Array<OrganizationMembershipDto>::class.java).toList()
            } catch (e: Exception) { emptyList() }
        }
        set(value) = prefs.edit().putString(KEY_ORGANIZATIONS, gson.toJson(value)).apply()

    fun setTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    fun clearAll() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER)
            .remove(KEY_ORGANIZATIONS)
            .apply()
    }

    fun isLoggedIn(): Boolean = accessToken != null

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user"
        private const val KEY_ORGANIZATIONS = "organizations"
    }
}
