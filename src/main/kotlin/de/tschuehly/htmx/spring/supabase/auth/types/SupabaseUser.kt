package de.tschuehly.htmx.spring.supabase.auth.types

import com.auth0.jwt.interfaces.Claim
import de.tschuehly.htmx.spring.supabase.auth.exception.ClaimsCannotBeNullException
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseJwtVerifier
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import java.util.*


data class SupabaseUser(
    val id: UUID,
    val email: String?,
    val phone: String?,
    val isAnonymous: Boolean,
    val userMetadata: MutableMap<String, Any>,
    val roles: List<String>,
    val provider: String?,
    val verifiedJwt: String
) {
    companion object {

        fun createFromJWT(verifiedJwt: SupabaseJwtVerifier.VerificationResult): SupabaseUser {
            val claimsMap = verifiedJwt.claims
            val metadata = claimsMap["user_metadata"]?.asMap()?.toMutableMap() ?: mutableMapOf()
            return SupabaseUser(
                id = claimsMap["sub"]?.let {
                    UUID.fromString(it.asString())
                } ?: throw ClaimsCannotBeNullException("sub claim is null"),
                email = claimsMap["email"]?.asString(),
                phone = claimsMap["phone"]?.asString(),
                isAnonymous = claimsMap["is_anonymous"]?.asBoolean() ?: true,
                userMetadata = metadata,
                roles = getRolesFromAppMetadata(claimsMap),
                provider = getProviderFromAppMetadata(claimsMap),
                verifiedJwt = verifiedJwt.token
            )
        }

        private fun getRolesFromAppMetadata(claimsMap: Map<String, Claim>): List<String> {
            val roles = claimsMap["app_metadata"]?.asMap()?.get("roles")
            if (roles is List<*> && roles.firstOrNull() is String) {
                return roles as List<String>
            }
            return listOf()
        }

        private fun getProviderFromAppMetadata(claimsMap: Map<String, Claim>): String {
            return claimsMap["app_metadata"]?.asMap()?.get("provider").toString() ?: ""
        }
    }

    fun getAuthorities(): MutableList<GrantedAuthority>? {
        val roleList = this.roles.map { "ROLE_${it.uppercase()}" }.toTypedArray()
        return AuthorityUtils.createAuthorityList(*roleList)
    }

}
