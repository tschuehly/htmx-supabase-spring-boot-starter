package io.supabase.supabasespringbootstarter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConstructorBinding
@ConfigurationProperties("supabase")
class SupabaseProperties(
    val projectId: String?,
    val databasePassword: String?,
    val anonKey: String?,
    val jwtSecret: String?,
    val successfulLoginRedirectPage: String?,
    val passwordRecoveryPage: String?,
    val unauthenticatedPage: String?,
    val unauthorizedPage: String?,
    val sslOnly: Boolean = true,
    val public: Public = Public()
) {


    // TODO: How to do @NotNull Validation in Kotlin
    init {
        require(projectId != null) { "You need to specify the property: projectId in your application.yaml" }
        require(databasePassword != null) { "You need to specify the property: databasePassword in your application.yaml" }
        require(anonKey != null) { "You need to specify the property: anonKey in your application.yaml" }
        require(jwtSecret != null) { "You need to specify the property: jwtSecret in your application.yaml" }
        require(successfulLoginRedirectPage != null) { "You need to specify the property: successfulLoginRedirectPage in your application.yaml" }
        require(passwordRecoveryPage != null) { "You need to specify the property: passwordRecoveryPage in your application.yaml" }
        require(unauthenticatedPage != null) { "You need to specify the property: unauthenticatedPage in your application.yaml" }
        require(unauthorizedPage != null) { "You need to specify the property: unauthorizedPage in your application.yaml" }
    }

    class Public {
        var get: Array<String> = arrayOf()
        var post: Array<String> = arrayOf()
        var delete: Array<String> = arrayOf()
        var put: Array<String> = arrayOf()
    }

    val roles: MutableMap<String, Role> = mutableMapOf()

    class Role {
        var get: Array<String> = arrayOf()
        var post: Array<String> = arrayOf()
        var delete: Array<String> = arrayOf()
        var put: Array<String> = arrayOf()

    }
}
