package de.tschuehly.htmx.spring.supabase.auth.security

import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class SupabaseAuthenticationEntryPoint(
    private val supabaseProperties: SupabaseProperties
) : AuthenticationEntryPoint {
    private val logger: Logger = LoggerFactory.getLogger(SupabaseAuthenticationEntryPoint::class.java)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.debug("An unauthenticated User tried to access the path ${request.requestURI}")
        if (request.getHeader("HX-Request") == "true") {
            response.setHeader("HX-Redirect", supabaseProperties.unauthenticatedPage)
            return
        }
        response.sendRedirect(supabaseProperties.unauthenticatedPage)
    }

}
