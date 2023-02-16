package io.supabase.supabasespringbootstarter.security

import io.supabase.supabasespringbootstarter.config.SupabaseProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
