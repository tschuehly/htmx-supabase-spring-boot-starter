package de.tschuehly.htmx.spring.supabase.auth.security

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.interfaces.Claim

class SupabaseJwtVerifier(private val jwtVerifier: JWTVerifier) {

    fun verify(jwt: String): VerificationResult {
        val verifiedJwt = jwtVerifier.verify(jwt)
        return VerificationResult(verifiedJwt.claims, verifiedJwt.token)
    }

    data class VerificationResult(val claims: MutableMap<String, Claim>, val token: String)

}