package io.supabase.supabasespringbootstarter.security

import io.supabase.supabasespringbootstarter.exception.SupabasePrincipalException
import io.supabase.supabasespringbootstarter.types.SupabaseUser
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

