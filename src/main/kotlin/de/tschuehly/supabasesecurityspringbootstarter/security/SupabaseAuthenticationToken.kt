package de.tschuehly.supabasesecurityspringbootstarter.security

import de.tschuehly.supabasesecurityspringbootstarter.exception.SupabasePrincipalException
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class SupabaseAuthenticationToken(
    supabaseUser: SupabaseUser
) : PreAuthenticatedAuthenticationToken(
    supabaseUser, null,
    AuthorityUtils.createAuthorityList(*supabaseUser.roles.map { "ROLE_${it.uppercase()}" }.toTypedArray())
) {
    fun getSupabaseUser(): SupabaseUser {
        if (this.principal is SupabaseUser) {
            return this.principal as SupabaseUser
        }
        throw SupabasePrincipalException("Principal is not of type SupabaseUser::class")
    }
}

