package de.tschuehly.htmx.spring.supabase.auth.security

import de.tschuehly.htmx.spring.supabase.auth.exception.UnknownSupabaseException
import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication

class SupabaseAuthenticationProvider(
    private val supabaseJwtVerifier: SupabaseJwtVerifier
) : AuthenticationProvider {
    override fun authenticate(token: Authentication): SupabaseAuthenticationToken {
        token is JwtAuthenticationToken
        if (token !is JwtAuthenticationToken) {
            throw UnknownSupabaseException("Something went wrong when trying to authenticate with the jwt")
        }
        val jwt = supabaseJwtVerifier.verify(token.jwtString)
        return SupabaseAuthenticationToken.authenticated(SupabaseUser.createFromJWT(jwt))
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == JwtAuthenticationToken::class.java
    }
}