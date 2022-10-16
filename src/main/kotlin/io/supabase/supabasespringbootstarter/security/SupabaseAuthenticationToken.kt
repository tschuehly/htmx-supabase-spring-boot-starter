package io.supabase.supabasespringbootstarter.security

import io.supabase.supabasespringbootstarter.types.SupabaseUser
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class SupabaseAuthenticationToken(
    supabaseUser: SupabaseUser
) : PreAuthenticatedAuthenticationToken(
    supabaseUser, null,
    AuthorityUtils.createAuthorityList("ROLE_USER")
) {
}

