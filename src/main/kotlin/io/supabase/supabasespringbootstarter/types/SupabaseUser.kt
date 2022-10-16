package io.supabase.supabasespringbootstarter.types

import com.auth0.jwt.interfaces.Claim
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*


data class SupabaseUser(
    val id: UUID,
    val email: String,
    val phone: String,
    val role: String,
    private val userMetadataString: String?,
    private val appMetadataString: String?
) {
    var userMetadata: MutableMap<String, String> = mutableMapOf()
        get() = getMapper().readValue(
            userMetadataString, object : TypeReference<MutableMap<String, String>>() {}
        )
    val provider: String
        get() = getMapper().readTree(appMetadataString).get("provider").textValue()

    private fun getMapper(): ObjectMapper {
        return jacksonObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    }

    constructor(claimsMap: Map<String, Claim>) : this(
        UUID.fromString(claimsMap["sub"]?.asString() ?: throw Error("Invalid Sub")),
        claimsMap["email"].toString(),
        claimsMap["phone"].toString(),
        claimsMap["role"].toString(),
        claimsMap["user_metadata"].toString(),
        claimsMap["app_metadata"].toString(),
    )
}
