package io.supabase.supabasespringbootstarter.security

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
class SpringSecurityConfig(
    val accessDeniedHandler: SupabaseAccessDeniedHandler
) {
    @Bean
    @ConditionalOnMissingBean
    fun filterChain(http: HttpSecurity, supabaseJwtFilter: SupabaseJwtFilter): SecurityFilterChain {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/", "/logout", "/login", "/error").permitAll()
            .antMatchers(HttpMethod.POST, "/api/user/register", "/api/user/login", "/api/user/jwt").permitAll()
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
