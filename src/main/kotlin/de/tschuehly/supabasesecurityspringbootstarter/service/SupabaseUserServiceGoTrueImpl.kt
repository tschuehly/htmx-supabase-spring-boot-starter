package de.tschuehly.supabasesecurityspringbootstarter.service

import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
import de.tschuehly.supabasesecurityspringbootstarter.exception.*
import de.tschuehly.supabasesecurityspringbootstarter.security.SupabaseJwtFilter.Companion.setJWTCookie
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SupabaseUserServiceGoTrueImpl(
    val supabaseProperties: SupabaseProperties,
    val goTrueClient: GoTrue
) : ISupabaseUserService {
    val logger: Logger = LoggerFactory.getLogger(SupabaseUserServiceGoTrueImpl::class.java)
    override fun registerWithEmail(email: String, password: String) {
        runBlocking {
            try {
                val user = goTrueClient.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                logger.debug("User with the mail $email successfully registered, Confirmation Mail sent at ${user?.confirmationSentAt}")
                throw SuccessfulRegistrationConfirmationEmailSent("User with the mail $email successfully registered, Confirmation Mail sent")
            } finally {
                goTrueClient.sessionManager.deleteSession()
            }
        }
    }

    override fun loginWithEmail(email: String, password: String, response: HttpServletResponse) {

        runBlocking {
            try {
                goTrueClient.loginWith(Email) {
                    this.email = email
                    this.password = password
                }
                val token = goTrueClient.currentSessionOrNull()?.accessToken
                    ?: throw JWTTokenNullException("The JWT that $email requested is null")
                response.setJWTCookie(token,supabaseProperties)
                response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)

                logger.debug("User: $email successfully logged in")
            } catch (e: BadRequestRestException) {
                val errorMessage = e.message
                if (errorMessage?.contains("Invalid login credentials") == true) {
                    val msg = "$email has tried to login with invalid credentials"
                    logger.debug(msg)
                    throw InvalidLoginCredentialsException(
                        msg, e
                    )
                } else if (errorMessage?.contains("Email not confirmed") == true) {
                    val msg = "$email needs to confirm email before he can login"
                    logger.debug(msg)
                    throw UserNeedsToConfirmEmailBeforeLoginException(msg)
                }
            } finally {
                goTrueClient.sessionManager.deleteSession()
            }
        }
    }

    override fun authorizeWithJwtOrResetPassword(
        request: HttpServletRequest, response: HttpServletResponse
    ): HttpServletResponse {
        val header: String? = request.getHeader("HX-Current-URL")
        if (header != null) {
            val user = SecurityContextHolder.getContext().authentication.principal as SupabaseUser
            if (header.contains("type=recovery")) {
                logger.debug("User: ${user.email} is trying to reset his password")
                response.setHeader("HX-Redirect", supabaseProperties.passwordRecoveryPage)
            }else{
                response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
            }
        }
        return response
    }

    override fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        SecurityContextHolder.getContext().authentication = null
        request.cookies?.find { it.name == "JWT" }?.let {
            var cookieString = "JWT=${it.value}; HttpOnly; Path=/;Max-Age=0;"
            if (supabaseProperties.sslOnly) {
                cookieString += "Secure;"
            }
            response.setHeader("Set-Cookie", cookieString)
            response.setHeader("HX-Redirect", "/") // TODO: Introduce Redirect Header or HTMX / JSON Switch
        }
    }

    override fun setRolesWithRequest(request: HttpServletRequest, userId: String, roles: List<String>?) {
        request.cookies?.find { it.name == "JWT" }?.let {
            setRoles(it.value, userId, roles)
        }
    }

    override fun setRoles(serviceRoleJWT: String, userId: String, roles: List<String>?) {
        val roleArray = roles ?: listOf()

        runBlocking {
            try {
                goTrueClient.importAuthToken(serviceRoleJWT)
                goTrueClient.admin.updateUserById(uid = userId) {
                    appMetadata = buildJsonObject {
                        putJsonArray("roles") {
                            roleArray.map { add(it) }
                        }
                    }
                }
                logger.debug("The roles of the user with id {} were updated to {}", userId, roleArray)
            } catch (e: UnauthorizedRestException) {
                val errorMessage = e.message
                if (errorMessage?.contains("User not allowed") == true) {
                    val msg = "User with id: $userId has tried to setRoles without having service_role"
                    logger.debug(msg)
                    throw MissingServiceRoleForAdminAccessException(
                        msg, e
                    )
                }
            } finally {
                goTrueClient.sessionManager.deleteSession()
            }
        }
    }



    override fun sendPasswordRecoveryEmail(email: String) {
        runBlocking {
            goTrueClient.sendRecoveryEmail(email)
            throw PasswordRecoveryEmailSent("User with $email has requested a password recovery email")
        }
    }

    override fun updatePassword(request: HttpServletRequest, password: String) {

        runBlocking {
            try {
                request.cookies?.find { it.name == "JWT" }?.let { cookie ->
                    goTrueClient.importAuthToken(cookie.value)
                    goTrueClient.modifyUser(true) {
                        this.password = password
                    }
                    val user = SecurityContextHolder.getContext().authentication.principal as SupabaseUser
                    val msg = "User with the mail: ${user.email} updated his password successfully"
                    logger.debug(msg)
                    throw SuccessfulPasswordUpdate(msg)
                }
            } finally {
                goTrueClient.sessionManager.deleteSession()
            }
        }
    }
}
