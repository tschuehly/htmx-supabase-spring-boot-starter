package de.tschuehly.htmx.spring.supabase.auth.security

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.JWTVerifier.BaseVerification
import de.tschuehly.htmx.spring.supabase.auth.exception.UnknownSupabaseException
import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import java.util.*

class SupabaseAuthenticationProvider(
    private val jwtVerifier: JWTVerifier
) : AuthenticationProvider {
    override fun authenticate(token: Authentication): SupabaseAuthenticationToken {
        token is JwtAuthenticationToken
        if (token !is JwtAuthenticationToken) {
            throw UnknownSupabaseException("Something went wrong when trying to authenticate with the jwt")
        }
        val jwt = jwtVerifier.verify(token.jwtString)
        return SupabaseAuthenticationToken.authenticated(SupabaseUser.createFromJWT(jwt ))
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == JwtAuthenticationToken::class.java
    }
}