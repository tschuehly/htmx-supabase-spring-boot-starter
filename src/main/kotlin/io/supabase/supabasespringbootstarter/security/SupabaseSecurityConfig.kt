package io.supabase.supabasespringbootstarter.security

import io.supabase.supabasespringbootstarter.config.SupabaseProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity(debug = false)
class SupabaseSecurityConfig(
    val accessDeniedHandler: SupabaseAccessDeniedHandler,
    val supabaseProperties: SupabaseProperties,
    val cookieSecurityContextRepository: SupabaseCookieSecurityContextRepository
) {
    @Bean
    @ConditionalOnMissingBean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        supabaseProperties.roles.forEach { (role, paths) ->
            http.authorizeRequests()
                .antMatchers(HttpMethod.GET, *paths.get).access("hasRole($role)")
                .antMatchers(HttpMethod.POST, *paths.post).access("hasRole($role)")
                .antMatchers(HttpMethod.DELETE, *paths.delete).access("hasRole($role)")
                .antMatchers(HttpMethod.PUT, *paths.put).access("hasRole($role)")
        }
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .csrf().disable()
            .securityContext().securityContextRepository(cookieSecurityContextRepository).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, *supabaseProperties.public.get).permitAll()
            .antMatchers(HttpMethod.POST, *supabaseProperties.public.post).permitAll()
            .antMatchers(HttpMethod.DELETE, *supabaseProperties.public.delete).permitAll()
            .antMatchers(HttpMethod.PUT, *supabaseProperties.public.put).permitAll()
            .anyRequest().authenticated()
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler).and()
            .exceptionHandling {

            }
        return http.build()
    }


}
