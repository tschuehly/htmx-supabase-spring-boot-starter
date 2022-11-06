package io.supabase.supabasespringbootstarter.types

import com.auth0.jwt.interfaces.Claim
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*


class SupabaseUser(
    val id: UUID,
    val email: String,
    val phone: String,
    val userMetadata: MutableMap<String, String>
) {

    var roles: Array<String> = arrayOf()
    var provider: String = ""

    @JsonSetter("roles")
    fun setRoles(appMetadata: String){
        val roleArray =  mapper.readTree(appMetadata).get("roles")?.toString()
        roleArray?.let {
            this.roles = mapper.readValue(
                it, object : TypeReference<Array<String>>() {}
            )
        }

    }

    @JsonSetter("provider")
    fun setProviderFromAppMetadata(appMetadata: String){
        this.provider = mapper.readValue(
            mapper.readTree(appMetadata).get("provider").toString(), String::class.java
        )
    }

    override fun toString(): String {
        return "SupabaseUser(id=$id, email='$email', phone='$phone', userMetadata=$userMetadata, roles=${roles.contentToString()}, provider='$provider')"
    }

    companion object {
        val mapper: ObjectMapper = jacksonObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    }

    constructor(claimsMap: Map<String, Claim>) : this(
        UUID.fromString(claimsMap["sub"]?.asString() ?: throw Error("Invalid Sub")),
        claimsMap["email"].toString(),
        claimsMap["phone"].toString(),

        mapper.readValue(
            claimsMap["user_metadata"].toString(), object : TypeReference<MutableMap<String, String>>() {}
        )

    ){
        setRoles(claimsMap["app_metadata"].toString())
        setProviderFromAppMetadata(claimsMap["app_metadata"].toString())

    }
}
