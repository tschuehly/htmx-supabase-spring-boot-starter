package io.supabase.supabasespringbootstarter.security

import io.supabase.supabasespringbootstarter.config.SupabaseProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity(debug = false)
class SupabaseSecurityConfig(
    val supabaseJwtFilter: SupabaseJwtFilter,
    val supabaseProperties: SupabaseProperties,
    val cookieSecurityContextRepository: SupabaseCookieSecurityContextRepository
) {
    val logger: Logger = LoggerFactory.getLogger(SupabaseSecurityConfig::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        supabaseProperties.roles.forEach { (role, paths) ->
            paths.get.forEach { logger.info("Path: $it with Method GET is secured with Expression: hasRole('$role')") }
            paths.post.forEach { logger.info("Path: $it with Method POST is secured with Expression: hasRole('$role')") }
            paths.delete.forEach { logger.info("Path: $it with Method DELETE is secured with Expression: hasRole('$role')") }
            paths.put.forEach { logger.info("Path: $it with Method PUT is secured with Expression: hasRole('$role')") }

            http.authorizeRequests()
                .antMatchers(HttpMethod.GET, *paths.get).access("hasRole('${role.uppercase()}')")
                .antMatchers(HttpMethod.POST, *paths.post).access("hasRole('${role.uppercase()}')")
                .antMatchers(HttpMethod.DELETE, *paths.delete).access("hasRole('${role.uppercase()}')")
                .antMatchers(HttpMethod.PUT, *paths.put).access("hasRole('${role.uppercase()}')")
        }
        http.authorizeRequests()
            .antMatchers(HttpMethod.GET, *supabaseProperties.public.get).permitAll()
            .antMatchers(HttpMethod.POST, *supabaseProperties.public.post).permitAll()
            .antMatchers(HttpMethod.DELETE, *supabaseProperties.public.delete).permitAll()
            .antMatchers(HttpMethod.PUT, *supabaseProperties.public.put).permitAll()
            .anyRequest().authenticated()

        http
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .sameOrigin()
            .and()
//            .authenticationProvider(authenticationProvider())
            .addFilterBefore(supabaseJwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .securityContext()
            .securityContextRepository(cookieSecurityContextRepository)
            .and()
            .authorizeRequests()
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(supabaseAuthenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler())
        return http.build()
    }

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler {
        return SupabaseAccessDeniedHandler(supabaseProperties)
    }

    @Bean
    fun supabaseAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return SupabaseAuthenticationEntryPoint(supabaseProperties)
    }
//    @Bean TODO: Is this needed?
//    fun authenticationProvider(): AuthenticationProvider {
//        return SupabaseAuthenticationProvider()
//    }

}
