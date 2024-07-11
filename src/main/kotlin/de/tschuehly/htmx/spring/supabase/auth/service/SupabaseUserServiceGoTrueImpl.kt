package de.tschuehly.htmx.spring.supabase.auth.service

import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import de.tschuehly.htmx.spring.supabase.auth.events.SupabaseUserAuthenticatedEvent
import de.tschuehly.htmx.spring.supabase.auth.exception.*
import de.tschuehly.htmx.spring.supabase.auth.exception.email.OtpEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.PasswordRecoveryEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.RegistrationConfirmationEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.SuccessfulPasswordUpdate
import de.tschuehly.htmx.spring.supabase.auth.exception.info.InvalidLoginCredentialsException
import de.tschuehly.htmx.spring.supabase.auth.exception.info.NewPasswordShouldBeDifferentFromOldPasswordException
import de.tschuehly.htmx.spring.supabase.auth.exception.info.UserAlreadyRegisteredException
import de.tschuehly.htmx.spring.supabase.auth.exception.info.UserNeedsToConfirmEmailBeforeLoginException
import de.tschuehly.htmx.spring.supabase.auth.htmx.HtmxUtil
import de.tschuehly.htmx.spring.supabase.auth.security.JwtAuthenticationToken
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseAuthenticationProvider
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseJwtFilter.Companion.setJWTCookie
import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.gotrue.user.UserInfo
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

class SupabaseUserServiceGoTrueImpl(
    private val supabaseProperties: SupabaseProperties,
    private val goTrueClient: Auth,
    private val applicationEventPublisher: ApplicationEventPublisher,

    private val authenticationManager: SupabaseAuthenticationProvider,
) : SupabaseUserService {

    private val securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy()
    private val securityContextRepository: SecurityContextRepository = RequestAttributeSecurityContextRepository()
    private val logger: Logger = LoggerFactory.getLogger(SupabaseUserServiceGoTrueImpl::class.java)


    override fun signUpWithEmail(email: String, password: String, response: HttpServletResponse) {
        runGoTrue(email) {
            val user = goTrueClient.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            if (emailConfirmationEnabled(user)) {
                throw RegistrationConfirmationEmailSent(email, user?.confirmationSentAt)
            }
            loginWithEmail(email, password, response)
            logger.debug("User with the mail $email successfully signed up")
        }
    }

    override fun loginWithEmail(email: String, password: String, response: HttpServletResponse) {
        runGoTrue(email) {
            goTrueClient.signInWith(Email) {
                this.email = email
                this.password = password
            }
            setJwtCookieToCurrentSession(response)
            logger.debug("User: $email successfully logged in")
        }
    }

    private fun setJwtCookieToCurrentSession(response: HttpServletResponse) {
        }

    override fun sendOtp(email: String) {
        runGoTrue(email) {
            goTrueClient.signInWith(OTP){
                this.email = email
                this.createUser = supabaseProperties.otpCreateUser
            }
            throw OtpEmailSent(email)
        }
    }

    override fun handleClientAuthentication(
        request: HttpServletRequest, response: HttpServletResponse
    ) {
        val header = request.getHeader("HX-Current-URL") ?: throw HxCurrentUrlHeaderNotFound()
        val user = SecurityContextHolder.getContext().authentication.principal as SupabaseUser
        if (header.contains("type=recovery")) {
            logger.debug("User: ${user.email} is trying to reset his password")
            response.setHeader("HX-Redirect", supabaseProperties.passwordRecoveryPage)
        } else {
            applicationEventPublisher.publishEvent(SupabaseUserAuthenticatedEvent(user))
            response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
        }
    }

    override fun signInAnonymously(request: HttpServletRequest, response: HttpServletResponse) {
        runGoTrue {
            goTrueClient.signInAnonymously()
            val user = authenticateWithCurrentSession()
            applicationEventPublisher.publishEvent(SupabaseUserAuthenticatedEvent(user))
        }
    }

    override fun linkAnonToIdentity(email: String, request: HttpServletRequest, response: HttpServletResponse){
        runGoTrue {
            goTrueClient.updateUser {
                this.email = email
            }
        }

    }

    override fun authenticateWithCurrentSession(): SupabaseUser {
        val token = goTrueClient.currentSessionOrNull()?.accessToken
            ?: throw JWTTokenNullException("The JWT that requested from supabase is null")
        HtmxUtil.getResponse().setJWTCookie(token, supabaseProperties)
        HtmxUtil.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
        return authenticate(token)

    }

    override fun authenticate(jwt: String): SupabaseUser {
        val authResult = authenticationManager.authenticate(JwtAuthenticationToken(jwt))
        val context: SecurityContext = securityContextHolderStrategy.createEmptyContext()
        context.authentication = authResult
        HtmxUtil.getResponse().setJWTCookie(jwt, supabaseProperties)
        securityContextRepository.saveContext(context, HtmxUtil.getRequest(), HtmxUtil.getResponse())
        SecurityContextHolder.setContext(context)
        return authResult.principal
    }

    override fun logout(request: HttpServletRequest, response: HttpServletResponse) {
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

    override fun setRolesWithRequest(request: HttpServletRequest, userId: String, roles: List<String>?) {
        request.cookies?.find { it.name == "JWT" }?.let {
            setRoles(it.value, userId, roles)
        }
    }

    private fun setRoles(serviceRoleJWT: String, userId: String, roles: List<String>?) {
        val roleArray = roles ?: listOf()
        runGoTrue(userId = userId) {
            goTrueClient.importAuthToken(serviceRoleJWT)
            goTrueClient.admin.updateUserById(uid = userId) {
                appMetadata = buildJsonObject {
                    putJsonArray("roles") {
                        roleArray.map { add(it) }
                    }
                }
            }
            logger.debug("The roles of the user with id {} were updated to {}", userId, roleArray)
        }
    }


    override fun sendPasswordRecoveryEmail(email: String) {
        runGoTrue(email) {
            goTrueClient.resetPasswordForEmail(email)
            throw PasswordRecoveryEmailSent("User with $email has requested a password recovery email")
        }
    }

    override fun updatePassword(request: HttpServletRequest, password: String) {
        val user = SecurityContextHolder.getContext().authentication.principal as SupabaseUser
        val email = user.email ?: "no-email"
        runGoTrue(email) {
            val jwt = request.cookies?.find { it.name == "JWT" }?.value
                ?: throw JWTTokenNullException("No JWT found in request")
            goTrueClient.importAuthToken(jwt)
            goTrueClient.updateUser {
                this.password = password
            }
            throw SuccessfulPasswordUpdate(user.email)
        }
    }

    private fun runGoTrue(
        email: String = "no-email",
        userId: String = "no-userid",
        block: suspend CoroutineScope.() -> Unit
    ) {
        runBlocking {
            try {
                block()
            } catch (e: RestException) {
                handleGoTrueException(e, email, userId)
            } finally {
                goTrueClient.clearSession()
            }
        }
    }

    private fun handleGoTrueException(e: RestException, email: String, userId: String) {
        val message = e.message ?: let {
            logger.error(e.message)
            throw UnknownSupabaseException()
        }
        when {

            message.contains("User already registered", true) -> throw UserAlreadyRegisteredException(email)
            message.contains("Anonymous sign-ins are disabled", true) -> throw AnonymousSignInDisabled()
            message.contains("Invalid login credentials", true) -> throw InvalidLoginCredentialsException(email)
            message.contains("Email not confirmed", true) -> throw UserNeedsToConfirmEmailBeforeLoginException(email)
            message.contains("Signups not allowed for otp", true) -> throw OtpSignupNotAllowedExceptions(message)
            message.contains("User not allowed", true) -> throw MissingServiceRoleForAdminAccessException(userId)
            message.contains("New password should be different from the old password", true) -> {
                throw NewPasswordShouldBeDifferentFromOldPasswordException(email)
            }
        }
        logger.error(e.message)
        throw UnknownSupabaseException()
    }

    private fun emailConfirmationEnabled(user: UserInfo?): Boolean {
        return user?.email != null
    }
}
