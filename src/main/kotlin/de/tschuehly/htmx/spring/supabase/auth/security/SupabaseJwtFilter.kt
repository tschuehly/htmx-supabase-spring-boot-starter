package de.tschuehly.htmx.spring.supabase.auth.security

import com.auth0.jwt.exceptions.IncorrectClaimException
import com.auth0.jwt.exceptions.TokenExpiredException
import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.filter.OncePerRequestFilter


class SupabaseJwtFilter(
    private val authenticationManager: AuthenticationManager,
    private val supabaseProperties: SupabaseProperties
) : OncePerRequestFilter() {
    private val securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy()

    private val securityContextRepository: SecurityContextRepository = RequestAttributeSecurityContextRepository()
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cookie = request.cookies?.find { it.name == "JWT" }

        val header: String? = request.getHeader("HX-Current-URL")
        val jwtString = getJwtString(header, cookie)
        jwtString?.let { jwt ->
            try {
                authenticate(jwt, request, response)
            } catch (e: TokenExpiredException) {
                response.setJWTCookie(jwtString, supabaseProperties, 0)
            } catch (e: IncorrectClaimException) {
                if (e.message?.contains("The Token can't be used before") == true) {
                    // Wait for one second on login if the jwt is not active yet
                    logger.debug(e.message)
                    Thread.sleep(1000L)
                    authenticate(jwt, request, response)
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun authenticate(
        jwt: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val authResult = authenticationManager.authenticate(JwtAuthenticationToken(jwt))
        val context: SecurityContext = securityContextHolderStrategy.createEmptyContext()
        context.authentication = authResult
        response.setJWTCookie(jwt, supabaseProperties)
        securityContextRepository.saveContext(context, request, response)
    }


    private fun getJwtString(header: String?, cookie: Cookie?): String? {
        return if (header?.contains("#access_token=") == true) {
            header.substringBefore("&").substringAfter("#access_token=")
        } else {
            cookie?.value
        }
    }

    companion object {
        fun HttpServletResponse.setJWTCookie(
            accessToken: String,
            supabaseProperties: SupabaseProperties,
            maxAge: Int = 6000
        ) {
            this.addCookie(Cookie("JWT", accessToken).also {
                it.secure = supabaseProperties.sslOnly
                it.isHttpOnly = true
                it.path = "/"
                it.maxAge = maxAge
            })
        }
    }
}
