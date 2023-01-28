package io.supabase.supabasespringbootstarter.security

import io.supabase.supabasespringbootstarter.types.SupabaseUser
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class SupabaseAuthenticationToken(
    supabaseUser: SupabaseUser
) : PreAuthenticatedAuthenticationToken(
    supabaseUser, null,
    AuthorityUtils.createAuthorityList(*supabaseUser.roles.map { it -> "ROLE_${it.uppercase()}" }.toTypedArray())
) {
    fun getSupabaseUser(): SupabaseUser {
        if (this.principal is SupabaseUser) {
            return this.principal as SupabaseUser
        }
        throw Error("Principal is not of type Supabase User")
    }
}

