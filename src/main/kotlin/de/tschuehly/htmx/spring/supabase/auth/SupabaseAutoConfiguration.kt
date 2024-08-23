package de.tschuehly.htmx.spring.supabase.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zaxxer.hikari.HikariDataSource
import de.tschuehly.htmx.spring.supabase.auth.config.DefaultExceptionHandlerConfig
import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import de.tschuehly.htmx.spring.supabase.auth.controller.SupabaseUserController
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseAuthenticationProvider
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseJwtVerifier
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseSecurityConfig
import de.tschuehly.htmx.spring.supabase.auth.service.SupabaseUserService
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ConditionalOnProperty(prefix = "supabase", name = ["projectId"])
@EnableConfigurationProperties(SupabaseProperties::class)
@Import(SupabaseSecurityConfig::class, DefaultExceptionHandlerConfig::class)
@AutoConfigureBefore(DataSourceAutoConfiguration::class)
class SupabaseAutoConfiguration(
    val supabaseProperties: SupabaseProperties,
) {
    val logger: Logger =
        LoggerFactory.getLogger(SupabaseAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun supabaseService(
        goTrueClient: Auth,
        supabaseAuthenticationProvider: SupabaseAuthenticationProvider,
        applicationEventPublisher: ApplicationEventPublisher
    ): SupabaseUserService {
        logger.debug("Registering the SupabaseUserService")
        return SupabaseUserService(
            supabaseProperties,
            goTrueClient,
            applicationEventPublisher,
            supabaseAuthenticationProvider
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun supabaseController(supabaseUserService: SupabaseUserService): SupabaseUserController {
        logger.debug("Registering the SupabaseUserController")
        return SupabaseUserController(supabaseUserService)
    }

    @Bean
    @ConditionalOnMissingBean
    fun supabaseClient(supabaseProperties: SupabaseProperties): Auth {
        val supabase = createSupabaseClient(
            supabaseUrl = supabaseProperties.url?: "https://${supabaseProperties.projectId}.supabase.co",
            supabaseKey = supabaseProperties.anonKey
        ) {
            install(Auth) {
                autoSaveToStorage = false
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
        }
        return supabase.auth
    }

    @Bean
    fun supabaseJwtVerifier(supabaseProperties: SupabaseProperties): SupabaseJwtVerifier {
        val jwtVerifier = JWT.require(Algorithm.HMAC256(supabaseProperties.jwtSecret)).build()
        return SupabaseJwtVerifier(jwtVerifier)
    }

    @Bean
    @ConfigurationProperties("supabase.datasource")
    @ConditionalOnProperty(prefix = "supabase.database", name = ["host"])
    fun dataSource(
        supabaseProperties: SupabaseProperties
    ): HikariDataSource {
        val dataSourceBuilder = DataSourceBuilder.create().type(HikariDataSource::class.java)
        dataSourceBuilder.driverClassName("org.postgresql.Driver")
        supabaseProperties.database?.let { db ->
            dataSourceBuilder.url("jdbc:postgresql://${db.host}:${db.port}/${db.name}")
            db.username?.let {
                dataSourceBuilder.username(it)
            } ?: let { dataSourceBuilder.username("postgres.${supabaseProperties.projectId}") }
            dataSourceBuilder.password(db.password)
        }
        return dataSourceBuilder.build()
    }
}
