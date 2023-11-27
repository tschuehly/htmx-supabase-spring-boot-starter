package de.tschuehly.htmx.spring.supabase.auth.security

import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

class SupabaseAccessDeniedHandler(
    private val supabaseProperties: SupabaseProperties
) : AccessDeniedHandler {
    private val logger: Logger = LoggerFactory.getLogger(SupabaseAccessDeniedHandler::class.java)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        logger.debug(accessDeniedException.message)
        response.sendRedirect(supabaseProperties.unauthorizedPage)
    }

}
