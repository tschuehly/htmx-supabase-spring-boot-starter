package de.tschuehly.htmx.spring.supabase.auth.security

import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

object SupabaseSecurityContextHolder {
    fun getAuthenticatedUser(): SupabaseUser? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication !is AnonymousAuthenticationToken) {
            return (authentication as SupabaseAuthenticationToken).principal
        }
        return null
    }

}