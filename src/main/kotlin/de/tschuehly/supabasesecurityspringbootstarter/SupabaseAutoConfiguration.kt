package de.tschuehly.supabasesecurityspringbootstarter

import de.tschuehly.supabasesecurityspringbootstarter.config.DefaultExceptionHandlerConfig
import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
import de.tschuehly.supabasesecurityspringbootstarter.controller.SupabaseUserController
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseAuthenticationProvider
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseSecurityConfig
import de.tschuehly.supabasesecurityspringbootstarter.service.ISupabaseUserService
import de.tschuehly.supabasesecurityspringbootstarter.service.SupabaseUserServiceGoTrueImpl
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.plugins.standaloneSupabaseModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*

@Configuration
@ConditionalOnProperty(prefix = "supabase", name = ["projectId"])
@EnableConfigurationProperties(SupabaseProperties::class)
@Import(SupabaseSecurityConfig::class,DefaultExceptionHandlerConfig::class)
@PropertySource("classpath:application-supabase.properties")
class SupabaseAutoConfiguration(
    val supabaseProperties: SupabaseProperties,
) {
    val logger: Logger =
        LoggerFactory.getLogger(SupabaseAutoConfiguration::class.java)
    @Bean
    @ConditionalOnMissingBean
    fun supabaseService(
        goTrueClient: GoTrue,
        supabaseAuthenticationProvider: SupabaseAuthenticationProvider
    ): ISupabaseUserService {
        logger.debug("Registering the SupabaseUserService")
        return SupabaseUserServiceGoTrueImpl(supabaseProperties, goTrueClient)
    }

    @Bean
    @ConditionalOnMissingBean
    fun supabaseController(supabaseUserService: ISupabaseUserService): SupabaseUserController {
        logger.debug("Registering the SupabaseUserController")
        return SupabaseUserController(supabaseUserService)
    }

    @Bean
    @ConditionalOnMissingBean
    fun supabaseClient(supabaseProperties: SupabaseProperties): GoTrue {
        return standaloneSupabaseModule(
            GoTrue,
            url = "https://${supabaseProperties.projectId}.supabase.co/auth/v1",
            apiKey = supabaseProperties.anonKey
        )
    }
}
