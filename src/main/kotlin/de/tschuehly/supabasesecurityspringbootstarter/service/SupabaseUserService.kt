package de.tschuehly.supabasesecurityspringbootstarter.service

import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
import de.tschuehly.supabasesecurityspringbootstarter.exception.*
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseAuthenticationProvider
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseJwtFilter.Companion.setJWTCookie
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.http.GoTrueHttpException
import io.supabase.gotrue.types.GoTrueTokenResponse
import io.supabase.gotrue.types.GoTrueUserAttributes
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SupabaseUserService(
    val supabaseProperties: SupabaseProperties,
    val supabaseGoTrueClient: GoTrueClient<SupabaseUser, GoTrueTokenResponse>,
    val supabaseAuthenticationProvider: SupabaseAuthenticationProvider
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
            response.setJWTCookie(resp.accessToken, supabaseProperties)
        } catch (e: GoTrueHttpException) {
            if (e.data?.contains("Invalid login credentials") == true) {
                val msg = "$username either does not exist or has tried to login with the wrong password"
                logger.debug(msg)
                throw InvalidLoginCredentialsException(
                    msg,
                    e
                )
            } else if (e.data?.contains("Email not confirmed") == true) {
                val msg = "$username needs to confirm email before he can login"
                logger.debug(msg)
                throw UserNeedsToConfirmEmailBeforeLoginException(msg)
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
            val user = SecurityContextHolder.getContext().authentication.principal as SupabaseUser
            if (header.contains("type=recovery")) {
                logger.debug("User: ${user.email} is trying to reset his password")
                response.setHeader("HX-Redirect", supabaseProperties.passwordRecoveryPage)
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
            response.setHeader("HX-Redirect", "/") // TODO: Introduce Redirect Header or HTXM / JSON Switch
        }
    }

    fun setRolesWithRequest(request: HttpServletRequest, userId: String, roles: List<String>?) {
        request.cookies?.find { it.name == "JWT" }?.let {
            setRoles(it.value, userId, roles)
        }
    }

    fun setRoles(serviceRoleJWT: String, userId: String, roles: List<String>?) {
        val roleArray = roles ?: listOf()
        supabaseGoTrueClient.updateUserAppMetadata(serviceRoleJWT, userId, mapOf("roles" to roleArray))
        logger.debug("The roles of the user with id $userId were updated to $roleArray")
    }

    fun sendPasswordRecoveryEmail(email: String) {
        supabaseGoTrueClient.resetPasswordForEmail(email)
        throw PasswordRecoveryEmailSent("User with $email has requested a password recovery email")
    }

    fun updatePassword(request: HttpServletRequest, password: String) {
        request.cookies?.find { it.name == "JWT" }?.let { cookie ->
            supabaseGoTrueClient.updateUser(
                cookie.value,
                attributes = GoTrueUserAttributes(
                    password = password
                )
            )
            val user = SecurityContextHolder.getContext().authentication.principal as SupabaseUser
            val msg = "User with the mail: ${user.email} updated his password successfully"
            logger.debug(msg)
            throw SuccessfulPasswordUpdate(msg)
        }
    }
}
