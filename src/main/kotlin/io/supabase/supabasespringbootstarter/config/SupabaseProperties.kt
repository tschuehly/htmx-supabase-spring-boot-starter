package io.supabase.supabasespringbootstarter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ConfigurationProperties("supabase")
@PropertySource("classpath:application-supabase.properties")
class SupabaseProperties {
    lateinit var projectId: String
    lateinit var databasePassword: String
    lateinit var anonKey: String
    lateinit var jwtSecret: String
    lateinit var successfulLoginRedirectPage: String
    lateinit var passwordRecoveryPage: String
    var sslOnly: Boolean = true
    val public = Public()

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
