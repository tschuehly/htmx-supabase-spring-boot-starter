package io.supabase.supabasespringbootstarter.config

import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.types.GoTrueTokenResponse
import io.supabase.supabasespringbootstarter.controller.SupabaseUserController
import io.supabase.supabasespringbootstarter.security.SupabaseAccessDeniedHandler
import io.supabase.supabasespringbootstarter.security.SupabaseSecurityConfig
import io.supabase.supabasespringbootstarter.service.SupabaseUserService
import io.supabase.supabasespringbootstarter.types.SupabaseUser
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ConditionalOnProperty(prefix = "supabase", name = ["projectId"])
@ComponentScan("io.supabase.supabasespringbootstarter")
@Import(SupabaseSecurityConfig::class)
class SupabaseAutoConfiguration(
    val supabaseProperties: SupabaseProperties,
) {

    @Bean
    @ConditionalOnMissingBean
    fun supabaseService(supabaseGoTrueClient: GoTrueClient<SupabaseUser, GoTrueTokenResponse>): SupabaseUserService {
        return SupabaseUserService(supabaseProperties, supabaseGoTrueClient)
    }

    @Bean
    @ConditionalOnMissingBean
    fun supabaseController(supabaseUserService: SupabaseUserService): SupabaseUserController {
        return SupabaseUserController(supabaseUserService)
    }

    @Bean
    @ConditionalOnMissingBean
    fun supabaseGoTrueClient(supabaseProperties: SupabaseProperties): GoTrueClient<SupabaseUser, GoTrueTokenResponse> {
        return GoTrueClient.customApacheJacksonGoTrueClient(
            url = "https://${supabaseProperties.projectId}.supabase.co/auth/v1",
            headers = mapOf("apiKey" to supabaseProperties.anonKey)
        )
    }

    @Bean
    fun supabaseAccessDeniedHandler(): SupabaseAccessDeniedHandler {
        return SupabaseAccessDeniedHandler()
    }


}
