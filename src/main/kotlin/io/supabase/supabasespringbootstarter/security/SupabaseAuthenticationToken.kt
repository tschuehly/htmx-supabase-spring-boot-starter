package io.supabase.supabasespringbootstarter.security

import io.supabase.supabasespringbootstarter.types.SupabaseUser
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.util.*

class SupabaseAuthenticationToken(
    supabaseUser: SupabaseUser
) : PreAuthenticatedAuthenticationToken(
    supabaseUser, null,
    AuthorityUtils.createAuthorityList(*supabaseUser.roles.map { it -> "ROLE_${it.uppercase()}" }.toTypedArray())
) {
}

