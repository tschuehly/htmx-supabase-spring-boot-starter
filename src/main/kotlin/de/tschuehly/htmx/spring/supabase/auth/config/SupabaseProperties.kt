package de.tschuehly.htmx.spring.supabase.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("supabase")
class SupabaseProperties(
    val projectId: String,
    val url: String?,
    val anonKey: String?,
    val jwtSecret: String?,
    val cookieDomain: String?,
    val database: Database? = null,
    val otpCreateUser: Boolean = true,
    val successfulLoginRedirectPage: String?,
    val passwordRecoveryPage: String?,
    val unauthenticatedPage: String?,
    val unauthorizedPage: String?,
    val postLogoutPage: String?,
    val sslOnly: Boolean = true,
    val public: Public = Public(),
    val roles: MutableMap<String, Role> = mutableMapOf(),
    val basicAuth: BasicAuth = BasicAuth()
) {

    init {
        val errorMessage = mutableListOf<String>()
        if (anonKey == null) {
            errorMessage.add("You need to specify the property: supabase.anonKey in your application.yaml")
        }
        if (jwtSecret == null) {
            errorMessage.add("You need to specify the property: supabase.jwtSecret in your application.yaml")
        }
        if (successfulLoginRedirectPage == null) {
            errorMessage.add("You need to specify the property: supabase.successfulLoginRedirectPage in your application.yaml")
        }
        if (passwordRecoveryPage == null) {
            errorMessage.add("You need to specify the property: supabase.passwordRecoveryPage in your application.yaml")
        }
        if (unauthenticatedPage == null) {
            errorMessage.add("You need to specify the property: supabase.unauthenticatedPage in your application.yaml")
        }
        if (unauthorizedPage == null) {
            errorMessage.add("You need to specify the property: supabase.unauthorizedPage in your application.yaml")
        }
        if (errorMessage.isNotEmpty()) {
            throw IllegalArgumentException(errorMessage.joinToString("\n"))
        }
    }

    class Database(
        val host: String?,
        val name: String = "postgres",
        val username: String?,
        val password: String?,
        val port: Int = 5432
    ) {

    }

    class BasicAuth(
        val enabled: Boolean = false,
        val username: String? = null,
        val password: String? = null,
        val roles: List<String> = listOf()

    ) {
        init {
            if (enabled) {
                require(username != null) { "You need to specify the property: supabase.basicAuth.username in you application.yaml" }
                require(password != null) { "You need to specify the property: supabase.basicAuth.password in you application.yaml" }
            }
        }

    }

    class Public {
        var get: Array<String> = arrayOf()
        var post: Array<String> = arrayOf()
        var delete: Array<String> = arrayOf()
        var put: Array<String> = arrayOf()
    }


    class Role {
        var get: Array<String> = arrayOf()
        var post: Array<String> = arrayOf()
        var delete: Array<String> = arrayOf()
        var put: Array<String> = arrayOf()

    }
}
