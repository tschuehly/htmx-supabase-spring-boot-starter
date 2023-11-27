package de.tschuehly.htmx.spring.supabase.auth.security

import com.auth0.jwt.JWTVerifier
import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity(debug = false)
class SupabaseSecurityConfig(
    val supabaseProperties: SupabaseProperties
) {
    val logger: Logger = LoggerFactory.getLogger(SupabaseSecurityConfig::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun filterChain(
        http: HttpSecurity,
        supabaseJwtFilter: SupabaseJwtFilter,
        supabaseAuthenticationManager: AuthenticationManager
    ): SecurityFilterChain {
        supabaseProperties.roles.forEach { (role, paths) ->
            http.invoke {
                authorizeHttpRequests {
                    paths.get.forEach { path ->
                        logger.info("Path: $path with Method GET is secured with Expression: hasRole('$role')")
                        authorize(path, hasRole(role.uppercase()))
                    }
                    paths.post.forEach { path ->
                        logger.info("Path: $path with Method POST is secured with Expression: hasRole('$role')")
                        authorize(path, hasRole(role.uppercase()))
                    }
                    paths.delete.forEach { path ->
                        logger.info("Path: $path with Method DELETE is secured with Expression: hasRole('$role')")
                        authorize(path, hasRole(role.uppercase()))
                    }
                    paths.put.forEach { path ->
                        logger.info("Path: $path with Method PUT is secured with Expression: hasRole('$role')")
                        authorize(path, hasRole(role.uppercase()))
                    }
                }
            }
        }

        http.invoke {
            authorizeHttpRequests {
                supabaseProperties.public.get.forEach { path ->
                    logger.info("Path: $path with Method GET is public")
                    authorize(HttpMethod.GET, path, permitAll)
                }
                supabaseProperties.public.post.forEach { path ->
                    logger.info("Path: $path with Method POST is public")
                    authorize(HttpMethod.POST, path, permitAll)
                }
                supabaseProperties.public.delete.forEach { path ->
                    logger.info("Path: $path with Method DELETE is public")
                    authorize(HttpMethod.DELETE, path, permitAll)
                }
                supabaseProperties.public.put.forEach { path ->
                    logger.info("Path: $path with Method PUT is public")
                    authorize(HttpMethod.PUT, path, permitAll)
                }

                authorize(anyRequest, authenticated)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            authenticationManager = supabaseAuthenticationManager
            httpBasic {
            }
            csrf { disable() }
            headers {
                frameOptions {
                    sameOrigin
                }
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(supabaseJwtFilter)
            exceptionHandling {
                authenticationEntryPoint = supabaseAuthenticationEntryPoint()
                accessDeniedHandler = supabaseAccessDeniedHandler()
            }
        }

        return http.build()
    }

    @Bean
    fun supabaseAccessDeniedHandler(): AccessDeniedHandler {
        return SupabaseAccessDeniedHandler(supabaseProperties)
    }

    @Bean
    fun supabaseAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return SupabaseAuthenticationEntryPoint(supabaseProperties)
    }

    @Bean
    fun supabaseAuthenticationProvider(): SupabaseAuthenticationProvider {
        return SupabaseAuthenticationProvider()
    }

    @Bean
    fun supabaseJwtFilter(
        authenticationManager: AuthenticationManager,
        supabaseProperties: SupabaseProperties,
        jwtVerifier: JWTVerifier
    ): SupabaseJwtFilter {
        return SupabaseJwtFilter(authenticationManager, supabaseProperties, jwtVerifier)
    }

    @Bean
    fun authManager(
        http: HttpSecurity,
        supabaseAuthenticationProvider: SupabaseAuthenticationProvider,
        supabaseProperties: SupabaseProperties
    ): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(
            AuthenticationManagerBuilder::class.java
        )
        authenticationManagerBuilder.authenticationProvider(supabaseAuthenticationProvider)
        if (supabaseProperties.basicAuth.enabled) {
            authenticationManagerBuilder.inMemoryAuthentication()
                .withUser(supabaseProperties.basicAuth.username)
                .password(supabaseProperties.basicAuth.password)
                .roles(*supabaseProperties.basicAuth.roles.toTypedArray())

        }
        return authenticationManagerBuilder.build()
    }

}
