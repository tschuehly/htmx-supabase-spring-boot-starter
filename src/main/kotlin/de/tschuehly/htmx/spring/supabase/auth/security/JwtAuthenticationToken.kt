package de.tschuehly.htmx.spring.supabase.auth.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils

class JwtAuthenticationToken(val jwtString: String): AbstractAuthenticationToken(AuthorityUtils.NO_AUTHORITIES) {
    override fun getCredentials(): String {
        return jwtString
    }

    override fun getPrincipal() = null
}