package de.tschuehly.htmx.spring.supabase.auth.security

import de.tschuehly.htmx.spring.supabase.auth.exception.ClaimsCannotBeNullException
import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication

class SupabaseAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(token: Authentication): Authentication {
        token as SupabaseAuthenticationToken
        if (token.claims == null) {
            throw ClaimsCannotBeNullException("$token claims are null")
        }
        return SupabaseAuthenticationToken.authenticated(SupabaseUser(token.claims))
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == SupabaseAuthenticationToken::class.java
    }
}