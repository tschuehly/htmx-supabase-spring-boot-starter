package de.tschuehly.supabasesecurityspringbootstarter.service

import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
import de.tschuehly.supabasesecurityspringbootstarter.exception.*
import de.tschuehly.supabasesecurityspringbootstarter.exception.email.OtpEmailSent
import de.tschuehly.supabasesecurityspringbootstarter.exception.email.PasswordRecoveryEmailSent
import de.tschuehly.supabasesecurityspringbootstarter.exception.email.SuccessfulPasswordUpdate
import de.tschuehly.supabasesecurityspringbootstarter.exception.email.RegistrationConfirmationEmailSent
import de.tschuehly.supabasesecurityspringbootstarter.exception.info.InvalidLoginCredentialsException
import de.tschuehly.supabasesecurityspringbootstarter.exception.info.UserAlreadyRegisteredException
import de.tschuehly.supabasesecurityspringbootstarter.exception.info.UserNeedsToConfirmEmailBeforeLoginException
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

class SupabaseUserServiceGoTrueImpl(
    private val supabaseProperties: SupabaseProperties,
    private val goTrueClient: GoTrue
) : ISupabaseUserService {
    private val logger: Logger = LoggerFactory.getLogger(SupabaseUserServiceGoTrueImpl::class.java)
    override fun signUpWithEmail(email: String, password: String, response: HttpServletResponse) {
        runBlocking {
            try {
                val user = goTrueClient.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                if (emailConfirmationDisabled(user)) {
                    loginWithEmail(email, password, response)
                    logger.debug("User with the mail $email successfully signed up, and logged in")
                } else {
                    val msg = "User with the mail $email successfully signed up, " +
                            "Confirmation Mail sent at ${user?.confirmationSentAt}"
                    logger.debug(msg)
                    throw RegistrationConfirmationEmailSent(msg)
                }
            }catch (e: BadRequestRestException){
                val errorMessage = e.message
                if (errorMessage?.contains("User already registered") == true) {
                    val msg = "user with the $email has tried to sign up again, but he was already registered"
                    logger.debug(msg)
                    throw UserAlreadyRegisteredException(msg)
                }
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
                response.setJWTCookie(token, supabaseProperties)
                response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)

                logger.debug("User: $email successfully logged in")
            } catch (e: BadRequestRestException) {
                val errorMessage = e.message
                if (errorMessage?.contains("Invalid login credentials") == true) {
                    val msg = "$email has tried to login with invalid credentials"
                    logger.debug(msg)
                    throw InvalidLoginCredentialsException(msg, e)
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

    override fun sendOtp(email: String){
        runBlocking {
            goTrueClient.sendOtpTo(Email){
                this.email = email
            }
            val msg = "OTP sent to $email"
            logger.debug(msg)
            throw OtpEmailSent(msg)
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
            } else {
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

    private fun emailConfirmationDisabled(user: Email.Result?): Boolean {
        return user == null
    }
}
