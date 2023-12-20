package de.tschuehly.htmx.spring.supabase.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import de.tschuehly.htmx.spring.supabase.auth.config.DefaultExceptionHandlerConfig
import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import de.tschuehly.htmx.spring.supabase.auth.controller.SupabaseUserController
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseAuthenticationProvider
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseSecurityConfig
import de.tschuehly.htmx.spring.supabase.auth.service.SupabaseUserService
import de.tschuehly.htmx.spring.supabase.auth.service.SupabaseUserServiceGoTrueImpl
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

@Configuration
@ConditionalOnProperty(prefix = "supabase", name = ["projectId"])
@EnableConfigurationProperties(SupabaseProperties::class)
@Import(SupabaseSecurityConfig::class, DefaultExceptionHandlerConfig::class)
@PropertySource("classpath:application-supabase.properties")
class SupabaseAutoConfiguration(
    val supabaseProperties: SupabaseProperties,
) {
    val logger: Logger =
        LoggerFactory.getLogger(de.tschuehly.htmx.spring.supabase.auth.SupabaseAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun supabaseService(
        goTrueClient: Auth,
        supabaseAuthenticationProvider: SupabaseAuthenticationProvider
    ): SupabaseUserService {
        logger.debug("Registering the SupabaseUserService")
        return SupabaseUserServiceGoTrueImpl(supabaseProperties, goTrueClient)
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
            supabaseUrl = "https://${supabaseProperties.projectId}.supabase.co/auth/v1",
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
    fun supabaseJwtVerifier(supabaseProperties: SupabaseProperties): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(supabaseProperties.jwtSecret)).build()

    }
}
