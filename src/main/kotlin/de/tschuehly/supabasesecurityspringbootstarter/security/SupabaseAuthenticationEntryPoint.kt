package de.tschuehly.supabasesecurityspringbootstarter.security

import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
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
        response.sendRedirect(supabaseProperties.unauthenticatedPage)
    }

}
