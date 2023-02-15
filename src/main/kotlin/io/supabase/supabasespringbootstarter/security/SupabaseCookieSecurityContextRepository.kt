package io.supabase.supabasespringbootstarter.security

import com.auth0.jwt.exceptions.TokenExpiredException
import io.supabase.supabasespringbootstarter.service.SupabaseUserService
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpRequestResponseHolder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SupabaseCookieSecurityContextRepository(
    val supabaseUserService: SupabaseUserService
) : SecurityContextRepository {

    override fun loadContext(requestResponseHolder: HttpRequestResponseHolder): SecurityContext {
        val jwtCookie = requestResponseHolder.request.cookies?.find { it.name == "JWT" }
        val context = SecurityContextHolder.createEmptyContext()
        if(jwtCookie != null){
            try {
                context.authentication = supabaseUserService.getAuthenticationToken(jwtCookie.value)
            } catch (e: TokenExpiredException) {

                jwtCookie.maxAge = 0
                requestResponseHolder.response.addCookie(jwtCookie)
            }
        }
        return context
    }

    override fun saveContext(context: SecurityContext, request: HttpServletRequest, response: HttpServletResponse) {
        // We do not save the context
    }

    override fun containsContext(request: HttpServletRequest): Boolean {
        return request.cookies?.find { it.name == "JWT" } != null
    }

}
