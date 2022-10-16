package io.supabase.supabasespringbootstarter.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.http.GoTrueHttpException
import io.supabase.gotrue.types.GoTrueTokenResponse
import io.supabase.supabasespringbootstarter.config.SupabaseProperties
import io.supabase.supabasespringbootstarter.exception.InvalidLoginCredentials
import io.supabase.supabasespringbootstarter.exception.UserAlreadyRegisteredException
import io.supabase.supabasespringbootstarter.security.SupabaseAuthenticationToken
import io.supabase.supabasespringbootstarter.types.SupabaseUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class SupabaseUserService(
    val supabaseProperties: SupabaseProperties,
    val supabaseGoTrueClient: GoTrueClient<SupabaseUser, GoTrueTokenResponse>
) {
    val logger: Logger = LoggerFactory.getLogger(SupabaseUserService::class.java)
    fun registerWithEmail(email: String, password: String, response: HttpServletResponse): HttpServletResponse {
        try {
            supabaseGoTrueClient.signUpWithEmail(email, password)
        } catch (e: GoTrueHttpException) {
            if (e.data?.contains("User already registered") == true) {
                throw UserAlreadyRegisteredException("User: ${email} already registered", e)
            } else {
                logger.error(e.data)
                throw e
            }
        }
        return response
    }

    fun login(
        username: String, password: String, response: HttpServletResponse
    ): HttpServletResponse {
        try {
            val resp = supabaseGoTrueClient.signInWithEmail(username, password)
            setCookies(response, resp.accessToken)
        } catch (e: GoTrueHttpException) {
            if (e.data?.contains("Invalid login credentials") == true) {
                throw InvalidLoginCredentials("User: ${username} already registered", e)
            } else {
                logger.error(e.data)
                throw e
            }
        }
        return response
    }

    fun authorizeWithJWT(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): HttpServletResponse {
        if (request.getHeader("HX-Current-URL") != null) {
            val accessToken = request.getHeader("HX-Current-URL")
                .substringBefore("&").substringAfter("#access_token=")
            setAuthentication(accessToken)
            setCookies(response, accessToken)
        }
        return response
    }

    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        SecurityContextHolder.getContext().authentication = null
        request.cookies?.find { it.name == "JWT" }?.let {
            response.setHeader("Set-Cookie", "JWT=${it}; Secure; HttpOnly; Path=/;Max-Age=0;")
            response.setHeader("HX-Redirect", "/")
        }
    }

    private fun setCookies(
        response: HttpServletResponse,
        accessToken: String
    ) {
        response.addCookie(Cookie("JWT", accessToken).also {
            it.secure = true
            it.isHttpOnly = true
            it.path = "/"
            it.maxAge = 6000
        })
        response.setHeader("HX-Redirect", "/account")
    }

    fun setAuthentication(jwt: String) {
        val decodedJWT = JWT
            .require(Algorithm.HMAC256(supabaseProperties.jwtSecret)).build().verify(jwt).claims
        SecurityContextHolder.getContext().authentication = SupabaseAuthenticationToken(
            SupabaseUser(decodedJWT)
        )
    }
}
