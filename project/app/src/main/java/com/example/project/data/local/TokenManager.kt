package com.example.project.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.project.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages secure storage of authentication tokens using EncryptedSharedPreferences
 */
class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _authTokensFlow = MutableStateFlow<AuthTokens?>(getTokens())
    val authTokensFlow: Flow<AuthTokens?> = _authTokensFlow.asStateFlow()

    /**
     * Save authentication tokens
     */
    fun saveTokens(tokens: AuthTokens) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            putString(KEY_TOKEN_TYPE, tokens.tokenType)
            apply()
        }
        _authTokensFlow.value = tokens
    }

    /**
     * Get stored authentication tokens
     */
    fun getTokens(): AuthTokens? {
        val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        val tokenType = sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer")

        return if (accessToken != null && refreshToken != null) {
            AuthTokens(accessToken, refreshToken, tokenType ?: "Bearer")
        } else {
            null
        }
    }

    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * Update only the access token (used after refresh)
     */
    fun updateAccessToken(accessToken: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            apply()
        }
        val currentTokens = getTokens()
        if (currentTokens != null) {
            _authTokensFlow.value = currentTokens.copy(accessToken = accessToken)
        }
    }

    /**
     * Clear all tokens (logout)
     */
    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_TYPE)
            apply()
        }
        _authTokensFlow.value = null
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }

    companion object {
        private const val PREFS_NAME = "cgm_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_TYPE = "token_type"
    }
}
