package de.tschuehly.supabasesecurityspringbootstarter.security

import com.auth0.jwt.interfaces.Claim
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.util.Assert

class SupabaseAuthenticationToken : AbstractAuthenticationToken {
    val claims: Map<String, Claim>?
    private val principal: SupabaseUser?

    constructor(claims: Map<String, Claim>) : super(null) {
        this.claims = claims
        this.principal = null
        isAuthenticated = false
    }

    constructor(supabaseUser: SupabaseUser) :
            super(AuthorityUtils.createAuthorityList(*supabaseUser.roles.map { "ROLE_${it.uppercase()}" }
                .toTypedArray())) {
        this.claims = null
        this.principal = supabaseUser
        super.setAuthenticated(true)
    }

    companion object {
        fun unauthenticated(claims: Map<String, Claim>) = SupabaseAuthenticationToken(claims)
        fun authenticated(supabaseUser: SupabaseUser) = SupabaseAuthenticationToken(supabaseUser)
    }

    override fun getCredentials() = null
    override fun getPrincipal() = principal

    override fun setAuthenticated(authenticated: Boolean) {
        Assert.isTrue(
            !isAuthenticated,
            "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead"
        )
        super.setAuthenticated(false)
    }
}

