package de.tschuehly.supabasesecurityspringbootstarter.security

import de.tschuehly.supabasesecurityspringbootstarter.exception.ClaimsCannotBeNullException
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
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