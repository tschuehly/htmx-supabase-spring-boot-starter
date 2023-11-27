package de.tschuehly.htmx.spring.supabase.auth.security

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.exceptions.IncorrectClaimException
import com.auth0.jwt.exceptions.TokenExpiredException
import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter


class SupabaseJwtFilter(
    private val authenticationManager: AuthenticationManager,
    private val supabaseProperties: SupabaseProperties,
    private val jwtVerifier: JWTVerifier
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cookie = request.cookies?.find { it.name == "JWT" }

        val header: String? = request.getHeader("HX-Current-URL")
        val jwtValue = if (header?.contains("#access_token=") == true) {
            header.substringBefore("&").substringAfter("#access_token=")
        } else {
            cookie?.value
        }
        jwtValue?.let { jwt ->
            try {
                setContext(jwt, response)
            } catch (e: TokenExpiredException) {
                response.setJWTCookie(jwtValue, supabaseProperties, 0)
            } catch (e: IncorrectClaimException) {
                if (e.message?.contains("The Token can't be used before") == true) {
                    // Wait for one second on login if the jwt is not active yet
                    Thread.sleep(1000L)
                    setContext(jwt, response)
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun setContext(
        jwt: String,
        response: HttpServletResponse
    ) {
        val claims = jwtVerifier.verify(jwt).claims
        val authentication =
            authenticationManager.authenticate(SupabaseAuthenticationToken.unauthenticated(claims))
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
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
