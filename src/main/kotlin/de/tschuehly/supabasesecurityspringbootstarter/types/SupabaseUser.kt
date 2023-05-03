package de.tschuehly.supabasesecurityspringbootstarter.types

import com.auth0.jwt.interfaces.Claim
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*


class SupabaseUser(
    val id: UUID?,
    val email: String?,
    val phone: String?,
    val userMetadata: MutableMap<String, String>
) {

    var roles: Array<String> = arrayOf()
    var provider: String = ""

    @JsonSetter("roles")
    fun setRoles(claimsMap: Map<String, Claim>) {
        claimsMap["app_metadata"].toString()?.let { appMetadata ->

            mapper.readTree(appMetadata).get("roles")?.toString()?.let {
                this.roles = mapper.readValue(
                    it, object : TypeReference<Array<String>>() {}
                )
            }
        }

    }

    @JsonSetter("provider")
    fun setProviderFromAppMetadata(claimsMap: Map<String, Claim>) {
        claimsMap["app_metadata"].toString()?.let { appMetadata ->
            this.provider = mapper.readTree(appMetadata).get("provider")?.toString() ?: ""
        }

    }

    override fun toString(): String {
        return "SupabaseUser(id=$id, email='$email', phone='$phone', userMetadata=$userMetadata, roles=${roles.contentToString()}, provider='$provider')"
    }

    companion object {
        val mapper: ObjectMapper = jacksonObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    }

    constructor(claimsMap: Map<String, Claim>) : this(
        id = claimsMap["sub"]?.let {
            UUID.fromString(it.asString())
        },
        email = claimsMap["email"]?.toString(),
        phone = claimsMap["phone"]?.toString(),
        userMetadata = claimsMap["user_metadata"]?.let {
            mapper.readValue(
                claimsMap["user_metadata"].toString(), object : TypeReference<MutableMap<String, String>>() {}
            )
        } ?: mutableMapOf()

    ) {
        setRoles(claimsMap)
        setProviderFromAppMetadata(claimsMap)

    }
}
