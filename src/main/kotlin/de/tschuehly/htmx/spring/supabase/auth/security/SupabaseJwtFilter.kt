package de.tschuehly.htmx.spring.supabase.auth.security

import com.auth0.jwt.exceptions.IncorrectClaimException
import com.auth0.jwt.exceptions.TokenExpiredException
import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.web.filter.OncePerRequestFilter


class SupabaseJwtFilter(
    private val authenticationManager: AuthenticationManager,
    private val supabaseProperties: SupabaseProperties
) : OncePerRequestFilter() {

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
                authenticate(jwt,response)
            } catch (e: TokenExpiredException) {
                response.setJWTCookie(jwtString, supabaseProperties, 0)
            } catch (e: IncorrectClaimException) {
                if (e.message?.contains("The Token can't be used before") == true) {
                    // Wait for one second on login if the jwt is not active yet
                    logger.debug(e.message)
                    Thread.sleep(1000L)
                    authenticate(jwt,response)
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun getJwtString(header: String?, cookie: Cookie?): String? {
        return if (header?.contains("#access_token=") == true) {
            header.substringBefore("&").substringAfter("#access_token=")
        } else {
            cookie?.value
        }
    }

    private fun authenticate(
        jwt: String,
        response: HttpServletResponse
    ) {
        authenticationManager.authenticate(JwtAuthenticationToken(jwt))
        response.setJWTCookie(jwt, supabaseProperties)
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
