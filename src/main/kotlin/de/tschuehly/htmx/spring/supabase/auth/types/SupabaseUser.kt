package de.tschuehly.htmx.spring.supabase.auth.types

import com.auth0.jwt.interfaces.Claim
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import java.util.*


data class SupabaseUser(
    val id: UUID?,
    val email: String?,
    val phone: String?,
    val userMetadata: MutableMap<String, Any>,
    val roles: List<String>,
    val provider: String?
) {
    companion object {
        val mapper: ObjectMapper = jacksonObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

        fun getRolesFromAppMetadata(claimsMap: Map<String, Claim>): List<String> {
            val roles = claimsMap["app_metadata"]?.asMap()?.get("roles")
            if (roles is List<*> && roles.firstOrNull() is String) {
                return roles as List<String>
            }
            return listOf()
        }

        fun getProviderFromAppMetadata(claimsMap: Map<String, Claim>): String {
            return claimsMap["app_metadata"]?.asMap()?.get("provider").toString() ?: ""
        }
    }

    fun getAuthorities(): MutableList<GrantedAuthority>? {
        val roleList = this.roles.map { "ROLE_${it.uppercase()}" }.toTypedArray()
        return AuthorityUtils.createAuthorityList(*roleList)
    }

    constructor(claimsMap: Map<String, Claim>) : this(
        id = claimsMap["sub"]?.let {
            UUID.fromString(it.asString())
        },
        email = claimsMap["email"]?.asString(),
        phone = claimsMap["phone"]?.asString(),
        userMetadata = claimsMap["user_metadata"]?.let {
            mapper.readValue(
                claimsMap["user_metadata"]?.toString(), object : TypeReference<MutableMap<String, Any>>() {}
            )
        } ?: mutableMapOf(),
        roles = getRolesFromAppMetadata(claimsMap),
        provider = getProviderFromAppMetadata(claimsMap)
    )
}
