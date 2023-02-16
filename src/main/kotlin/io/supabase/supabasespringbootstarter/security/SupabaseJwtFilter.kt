package io.supabase.supabasespringbootstarter.security

import com.auth0.jwt.exceptions.TokenExpiredException
import io.supabase.supabasespringbootstarter.service.SupabaseUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SupabaseJwtFilter(
    val supabaseUserService: SupabaseUserService
) : OncePerRequestFilter() {
    val logger: Logger = LoggerFactory.getLogger(SupabaseJwtFilter::class.java)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val jwtCookie = request.cookies?.find { it.name == "JWT" }
        if (jwtCookie != null) {
            try {
                SecurityContextHolder.getContext().authentication =
                    supabaseUserService.getAuthenticationToken(jwtCookie.value).also {
                        logger.debug("Set authentication to $it")
                    }
            } catch (e: TokenExpiredException) {

                jwtCookie.maxAge = 0
                response.addCookie(jwtCookie)
            }
        }
        filterChain.doFilter(request, response)
    }

}
