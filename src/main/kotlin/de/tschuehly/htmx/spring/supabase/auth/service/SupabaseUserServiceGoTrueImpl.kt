package de.tschuehly.htmx.spring.supabase.auth.service

import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import de.tschuehly.htmx.spring.supabase.auth.events.SupabaseUserAuthenticated
import de.tschuehly.htmx.spring.supabase.auth.events.SupabaseUserEmailUpdated
import de.tschuehly.htmx.spring.supabase.auth.events.SupabaseUserRolesUpdated
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
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseSecurityContextHolder
import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.exception.AuthErrorCode
import io.github.jan.supabase.gotrue.exception.AuthRestException
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
                logger.debug("User $email signed up, email confirmation sent")
                throw RegistrationConfirmationEmailSent(email, user?.confirmationSentAt)
            }
            logger.debug("User $email successfully signed up")
            loginWithEmail(email, password, response)
        }
    }

    private fun emailConfirmationEnabled(user: UserInfo?) = user?.email != null


    override fun loginWithEmail(email: String, password: String, response: HttpServletResponse) {
        runGoTrue(email) {
            goTrueClient.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = authenticateWithCurrentSession()
            applicationEventPublisher.publishEvent(SupabaseUserAuthenticated(user))
            logger.debug("User: $email successfully logged in")
        }
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
        val user = SupabaseSecurityContextHolder.getAuthenticatedUser()
            ?: throw UnknownSupabaseException("No authenticated user found in SecurityContextRepository")
        if (header.contains("type=recovery")) {
            logger.debug("User: ${user.email} is trying to reset his password")
            response.setHeader("HX-Redirect", supabaseProperties.passwordRecoveryPage)
        } else {
            applicationEventPublisher.publishEvent(SupabaseUserAuthenticated(user))
            response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
        }
    }

    override fun signInAnonymously(request: HttpServletRequest, response: HttpServletResponse) {
        runGoTrue {
            goTrueClient.signInAnonymously()
            val user = authenticateWithCurrentSession()
            applicationEventPublisher.publishEvent(SupabaseUserAuthenticated(user))
        }
    }

    override fun linkAnonToIdentity(email: String, request: HttpServletRequest, response: HttpServletResponse) {
        runGoTrue {
            val user = SupabaseSecurityContextHolder.getAuthenticatedUser()
                ?: throw UnknownSupabaseException("No authenticated user found in SecurityContext")
            goTrueClient.importAuthToken(user.verifiedJwt)
            goTrueClient.updateUser {
                this.email = email
            }
            applicationEventPublisher.publishEvent(SupabaseUserEmailUpdated(user.id, email))
            throw UserNeedsToConfirmEmailBeforeLoginException(email)
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
        runGoTrue() {
            goTrueClient.importAuthToken(serviceRoleJWT)
            goTrueClient.admin.updateUserById(uid = userId) {
                appMetadata = buildJsonObject {
                    putJsonArray("roles") {
                        roleArray.map { add(it) }
                    }
                }
            }
            applicationEventPublisher.publishEvent(SupabaseUserRolesUpdated(userId, roleArray))
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
        val user = SupabaseSecurityContextHolder.getAuthenticatedUser()
            ?: throw UnknownSupabaseException("No authenticated user found in SecurityContextRepository")
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
        block: suspend CoroutineScope.() -> Unit
    ) {
        runBlocking {
            try {
                block()
            } catch (exc: AuthRestException) {
                handleAuthException(exc, email)
            } catch (e: RestException) {
                handleGoTrueException(e, email)
            } finally {
                goTrueClient.clearSession()
            }
        }
    }

    private fun handleAuthException(exc: AuthRestException, email: String) {
        when (exc.errorCode) {
            AuthErrorCode.UserAlreadyExists -> throw UserAlreadyRegisteredException(email)
            AuthErrorCode.SamePassword -> throw NewPasswordShouldBeDifferentFromOldPasswordException(email)
            AuthErrorCode.WeakPassword -> throw WeakPasswordException(email)
            AuthErrorCode.OtpExpired -> throw OtpExpiredException(email)
            AuthErrorCode.NotAdmin -> throw MissingServiceRoleForAdminAccessException(SupabaseSecurityContextHolder.getAuthenticatedUser()?.id)
            else -> throw SupabaseAuthException(exc)
        }
    }

    private fun handleGoTrueException(e: RestException, email: String) {
        val message = e.message ?: let {
            logger.error(e.message)
            throw UnknownSupabaseException()
        }
        when {
            message.contains("Anonymous sign-ins are disabled", true) -> throw AnonymousSignInDisabled()
            message.contains("Invalid login credentials", true) -> throw InvalidLoginCredentialsException(email)
            message.contains("Email not confirmed", true) -> throw UserNeedsToConfirmEmailBeforeLoginException(email)
            message.contains("Signups not allowed for otp", true) -> throw OtpSignupNotAllowedExceptions(message)
        }
        logger.error(e.message)
        throw UnknownSupabaseException()
    }
}
