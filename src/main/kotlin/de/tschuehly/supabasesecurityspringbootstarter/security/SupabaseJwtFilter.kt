package de.tschuehly.supabasesecurityspringbootstarter.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
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
        val jwtValue = if(header?.contains("#access_token=") == true){
            header.substringBefore("&").substringAfter("#access_token=")
        }else{
            cookie?.value
        }
        jwtValue?.let { jwt ->
            try {
                val claims =
                    JWT.require(Algorithm.HMAC256(supabaseProperties.jwtSecret)).build().verify(jwt).claims
                val authentication =
                    authenticationManager.authenticate(SupabaseAuthenticationToken.unauthenticated(claims))
                val context = SecurityContextHolder.createEmptyContext()
                context.authentication = authentication
                SecurityContextHolder.setContext(context)
                response.setJWTCookie(jwtValue,supabaseProperties)
            } catch (e: TokenExpiredException) {
                response.setJWTCookie(jwtValue, supabaseProperties,0)
            }
        }
        filterChain.doFilter(request, response)
    }


    companion object{
        public fun HttpServletResponse.setJWTCookie(

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
            this.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
        }

    }


}
