package io.supabase.supabasespringbootstarter.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.http.GoTrueHttpException
import io.supabase.gotrue.types.GoTrueTokenResponse
import io.supabase.gotrue.types.GoTrueUserAttributes
import io.supabase.supabasespringbootstarter.config.SupabaseProperties
import io.supabase.supabasespringbootstarter.exception.InvalidLoginCredentialsException
import io.supabase.supabasespringbootstarter.exception.UserAlreadyRegisteredException
import io.supabase.supabasespringbootstarter.exception.UserNeedsToConfirmEmailBeforeLoginException
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
    fun registerWithEmail(email: String, password: String, response: HttpServletResponse): SupabaseUser {
        try {
            return supabaseGoTrueClient.signUpWithEmail(email, password)
        } catch (e: GoTrueHttpException) {
            if (e.data?.contains("User already registered") == true) {
                throw UserAlreadyRegisteredException("User: ${email} already registered", e)
            } else {
                logger.error(e.data)
                throw e
            }
        }

    }

    fun login(
        username: String, password: String, response: HttpServletResponse
    ): HttpServletResponse {
        try {
            val resp = supabaseGoTrueClient.signInWithEmail(username, password)
            setCookies(response, resp.accessToken)
        } catch (e: GoTrueHttpException) {
            if (e.data?.contains("Invalid login credentials") == true) {
                throw InvalidLoginCredentialsException(
                    "$username either does not exist or has tried to login with the wrong password",
                    e
                )
            } else if(e.data?.contains("Email not confirmed") == true){
                throw UserNeedsToConfirmEmailBeforeLoginException("$username needs to confirm email before he can login")
            } else {
                logger.error(e.data)
                throw e
            }
        }
        return response
    }

    fun authorizeWithJwtOrResetPassword(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): HttpServletResponse {
        val header: String? = request.getHeader("HX-Current-URL")
        if (header != null) {
            val accessToken = header.substringBefore("&").substringAfter("#access_token=")
            setAuthentication(accessToken)
            setCookies(response, accessToken)
            if (header.contains("type=recovery")) {
                response.setHeader("HX-Redirect", "/updatePassword")
            }
        }
        return response
    }

    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        SecurityContextHolder.getContext().authentication = null
        request.cookies?.find { it.name == "JWT" }?.let {
            var cookieString = "JWT=${it.value}; HttpOnly; Path=/;Max-Age=0;"
            if (supabaseProperties.sslOnly) {
                cookieString += "Secure;"
            }
            response.setHeader("Set-Cookie", cookieString)
            response.setHeader("HX-Redirect", "/")
        }
    }

    fun setRoles(userId: String, request: HttpServletRequest, roles: List<String>?){
        request.cookies?.find { it.name == "JWT" }?.let {
            val roleArray = roles ?: listOf()
            supabaseGoTrueClient.updateUserAppMetadata(it.value,userId, mapOf("roles" to roleArray))
        }
    }

    private fun setCookies(
        response: HttpServletResponse,
        accessToken: String
    ) {
        response.addCookie(Cookie("JWT", accessToken).also {
            it.secure = supabaseProperties.sslOnly
            it.isHttpOnly = true
            it.path = "/"
            it.maxAge = 6000
        })
        response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
    }

    fun setAuthentication(jwt: String): SupabaseAuthenticationToken {
        val decodedJWT = JWT
            .require(Algorithm.HMAC256(supabaseProperties.jwtSecret)).build().verify(jwt).claims

        return SupabaseAuthenticationToken(
            SupabaseUser(decodedJWT)
        )
    }

    fun sendPasswordResetEmail(email: String) {
        supabaseGoTrueClient.resetPasswordForEmail(email)
    }

    fun updatePassword(request: HttpServletRequest, password: String) {
        request.cookies?.find { it.name == "JWT" }?.let {
            supabaseGoTrueClient.updateUser(
                it.value,
                attributes = GoTrueUserAttributes(
                    password = password
                )

            )
        }
    }
}
