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

}
