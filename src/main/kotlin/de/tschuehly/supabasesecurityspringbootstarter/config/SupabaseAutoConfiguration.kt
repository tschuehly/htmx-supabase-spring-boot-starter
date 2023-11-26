package de.tschuehly.supabasesecurityspringbootstarter.config

import de.tschuehly.supabasesecurityspringbootstarter.controller.SupabaseUserController
import de.tschuehly.supabasesecurityspringbootstarter.exception.DefaultSupabaseExceptionHandler
import de.tschuehly.supabasesecurityspringbootstarter.exception.SupabaseExceptionHandler
import de.tschuehly.supabasesecurityspringbootstarter.exception.SupabaseExceptionHandlerNotDefinedException
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseAuthenticationProvider
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseSecurityConfig
import de.tschuehly.supabasesecurityspringbootstarter.service.ISupabaseUserService
import de.tschuehly.supabasesecurityspringbootstarter.service.SupabaseUserServiceGoTrueImpl
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.plugins.standaloneSupabaseModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.web.bind.annotation.ControllerAdvice

@Configuration
@ConditionalOnProperty(prefix = "supabase", name = ["projectId"])
@ComponentScan(
    "de.tschuehly.supabasesecurityspringbootstarter", excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [DefaultSupabaseExceptionHandler::class])]
)
@EnableConfigurationProperties(SupabaseProperties::class)
@Import(SupabaseSecurityConfig::class)
@PropertySource("classpath:application-supabase.properties")
class SupabaseAutoConfiguration(
    val supabaseProperties: SupabaseProperties
) {
    val logger: Logger =
        LoggerFactory.getLogger(SupabaseAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean(SupabaseExceptionHandler::class)
    fun default(): SupabaseExceptionHandler {
        return DefaultSupabaseExceptionHandler()
    }

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
