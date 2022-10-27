package io.supabase.supabasespringbootstarter.security

import io.supabase.supabasespringbootstarter.config.SupabaseProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.ExceptionTranslationFilter

@Configuration
@EnableWebSecurity(debug = false)
class SupabaseSecurityConfig(
    val accessDeniedHandler: SupabaseAccessDeniedHandler,
    val supabaseProperties: SupabaseProperties
) {
    @Bean
    @ConditionalOnMissingBean
    fun filterChain(http: HttpSecurity, supabaseJwtFilter: SupabaseJwtFilter): SecurityFilterChain {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, *supabaseProperties.public.get).permitAll()
            .antMatchers(HttpMethod.POST, *supabaseProperties.public.post).permitAll()
            .anyRequest().authenticated()
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler).and()
            .addFilterAfter(supabaseJwtFilter, ExceptionTranslationFilter::class.java)
            .exceptionHandling {

            }
        return http.build()
    }

    @Bean
    @ConditionalOnMissingBean
    fun registration(supabaseJwtFilter: SupabaseJwtFilter): FilterRegistrationBean<SupabaseJwtFilter> {
        val registration = FilterRegistrationBean(supabaseJwtFilter);
        registration.isEnabled = false;
        return registration;
    }
}
