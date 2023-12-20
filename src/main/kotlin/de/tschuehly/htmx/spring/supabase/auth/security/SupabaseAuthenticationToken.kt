package de.tschuehly.htmx.spring.supabase.auth.security

import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.util.Assert

class SupabaseAuthenticationToken(
    private val supabaseUser: SupabaseUser
) : AbstractAuthenticationToken(supabaseUser.getAuthorities()) {

    init {
        super.setAuthenticated(true)
    }

    companion object {
        fun authenticated(supabaseUser: SupabaseUser) = SupabaseAuthenticationToken(supabaseUser)
    }

    override fun getCredentials() = null
    override fun getPrincipal() = supabaseUser

    override fun setAuthenticated(authenticated: Boolean) {
        Assert.isTrue(
            !isAuthenticated,
            "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead"
        )
        super.setAuthenticated(false)
    }
}

