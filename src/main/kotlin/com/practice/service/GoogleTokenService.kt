package com.practice.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GoogleTokenService(
    @Value("\${application.security.google.client-id}")
    private val clientId: String
) {
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(listOf(clientId))
        .build()

    fun verify(tokenString: String): GoogleUserInfo? {
        val idToken = try {
            verifier.verify(tokenString)
        } catch (e: Exception) {
            null
        } ?: return null

        val payload = idToken.payload
        val email = payload.email
        val name = payload["name"] as? String ?: ""
        
        return GoogleUserInfo(email, name)
    }
}

data class GoogleUserInfo(val email: String, val name: String)
