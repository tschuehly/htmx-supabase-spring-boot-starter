package de.tschuehly.htmx.spring.supabase.auth.service

import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import de.tschuehly.htmx.spring.supabase.auth.events.SupabaseUserAuthenticated
import de.tschuehly.htmx.spring.supabase.auth.events.SupabaseUserEmailUpdateConfirmed
import de.tschuehly.htmx.spring.supabase.auth.events.SupabaseUserEmailUpdateRequested
import de.tschuehly.htmx.spring.supabase.auth.events.SupabaseUserRolesUpdated
import de.tschuehly.htmx.spring.supabase.auth.exception.*
import de.tschuehly.htmx.spring.supabase.auth.exception.email.OtpEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.PasswordRecoveryEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.RegistrationConfirmationEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.SuccessfulPasswordUpdate
import de.tschuehly.htmx.spring.supabase.auth.exception.info.*
import de.tschuehly.htmx.spring.supabase.auth.htmx.HtmxUtil
import de.tschuehly.htmx.spring.supabase.auth.security.JwtAuthenticationToken
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseAuthenticationProvider
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseJwtFilter.Companion.setJWTCookie
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseSecurityContextHolder
import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponseHeader.HX_REDIRECT
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

class SupabaseUserService(
    private val supabaseProperties: SupabaseProperties,
    private val goTrueClient: Auth,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val authenticationManager: SupabaseAuthenticationProvider,
) {

    private val securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy()
    private val securityContextRepository: SecurityContextRepository = RequestAttributeSecurityContextRepository()
    private val logger: Logger = LoggerFactory.getLogger(SupabaseUserService::class.java)


    fun signUpWithEmail(email: String, password: String) {
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
            loginWithEmail(email, password)
        }
    }

    private fun emailConfirmationEnabled(user: UserInfo?) = user?.email != null


    fun loginWithEmail(email: String, password: String) {
        runGoTrue(email) {
            goTrueClient.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = authenticateWithCurrentSession()
            applicationEventPublisher.publishEvent(SupabaseUserAuthenticated(user, email))
            logger.debug("User: $email successfully logged in")
        }
    }


    fun signInWithMagicLink(email: String) {
        runGoTrue(email) {
            goTrueClient.signInWith(OTP) {
                this.email = email
                this.createUser = supabaseProperties.otpCreateUser
            }
            throw OtpEmailSent(email)
        }
    }

    fun handleClientAuthentication(
    ) {
        val url = HtmxUtil.getCurrentUrl()
        val user = SupabaseSecurityContextHolder.getAuthenticatedUser()
            ?: throw UnknownSupabaseException("No authenticated user found in SecurityContextRepository")
        if (url.contains("type=recovery")) {
            logger.debug("User: ${user.email} is trying to reset his password")
            HtmxUtil.setHeader(HX_REDIRECT, supabaseProperties.passwordRecoveryPage)
            return
        }
        if (url.contains("type=email_change")) {
            logger.debug("User: ${user.email} has set email")
            val email = user.email ?: throw IllegalStateException("Email shouldn't be null")
            applicationEventPublisher.publishEvent(SupabaseUserEmailUpdateConfirmed(user.id, email))
            return
        }
        applicationEventPublisher.publishEvent(SupabaseUserAuthenticated(user))
        HtmxUtil.setHeader(HX_REDIRECT, supabaseProperties.successfulLoginRedirectPage)
    }

    fun signInAnonymously() {
        runGoTrue {
            goTrueClient.signInAnonymously()
            val user = authenticateWithCurrentSession()
            applicationEventPublisher.publishEvent(SupabaseUserAuthenticated(user))
        }
    }

    fun requestEmailChange(email: String) {
        runGoTrue {
            val user = SupabaseSecurityContextHolder.getAuthenticatedUser()
                ?: throw UnknownSupabaseException("No authenticated user found in SecurityContext")
            goTrueClient.importAuthToken(user.verifiedJwt)
            goTrueClient.updateUser {
                this.email = email
            }
            // TODO:
            if (user.email == email) {
                goTrueClient.resendEmail(OtpType.Email.EMAIL_CHANGE, email)
            }
            applicationEventPublisher.publishEvent(SupabaseUserEmailUpdateRequested(user.id, email))
            throw UserNeedsToConfirmEmailForEmailChangeException(email)
        }
    }

    fun confirmEmailOtp(email: String, otp: String) {
        runGoTrue(email) {
            val user = SupabaseSecurityContextHolder.getAuthenticatedUser()
                ?: throw UnknownSupabaseException("No authenticated user found in SecurityContext")
            goTrueClient.importAuthToken(user.verifiedJwt)
            goTrueClient.verifyEmailOtp(type = OtpType.Email.EMAIL_CHANGE, email = email, token = otp)
            applicationEventPublisher.publishEvent(SupabaseUserEmailUpdateConfirmed(user.id, email))
        }
    }

    fun resendEmailChangeConfirmation(email: String) {
        runGoTrue(email) {
            goTrueClient.resendEmail(OtpType.Email.EMAIL_CHANGE, email)
        }
    }

    fun signInAnonymouslyWithEmail(email: String) {
        runGoTrue(email) {
            goTrueClient.signInAnonymously()
            goTrueClient.updateUser {
                this.email = email
            }
            val user = authenticateWithCurrentSession()
            applicationEventPublisher.publishEvent(SupabaseUserAuthenticated(user, email))
            applicationEventPublisher.publishEvent(SupabaseUserEmailUpdateRequested(user.id, email))
        }
    }

    private fun authenticateWithCurrentSession(): SupabaseUser {
        val token = goTrueClient.currentSessionOrNull()?.accessToken
            ?: throw JWTTokenNullException("The JWT that requested from supabase is null")
        HtmxUtil.getResponse().setJWTCookie(token, supabaseProperties)
        HtmxUtil.setHeader(HX_REDIRECT, supabaseProperties.successfulLoginRedirectPage)
        return authenticate(token)

    }

    fun authenticate(jwt: String): SupabaseUser {
        val authResult = authenticationManager.authenticate(JwtAuthenticationToken(jwt))
        val context: SecurityContext = securityContextHolderStrategy.createEmptyContext()
        context.authentication = authResult
        HtmxUtil.getResponse().setJWTCookie(jwt, supabaseProperties)
        securityContextRepository.saveContext(context, HtmxUtil.getRequest(), HtmxUtil.getResponse())
        SecurityContextHolder.setContext(context)
        return authResult.principal
    }

    fun logout() {
        SecurityContextHolder.getContext().authentication = null
        val jwt = HtmxUtil.getCookie("JWT")?.value
        if (jwt != null) {
            HtmxUtil.getResponse().setJWTCookie(jwt, supabaseProperties, 0)
            HtmxUtil.setHeader(HX_REDIRECT, supabaseProperties.postLogoutPage ?: "/")
        }
    }


    fun setRolesWithRequest(userId: String, roles: List<String>?) {
        HtmxUtil.getCookie("JWT")?.let {
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

    fun sendPasswordRecoveryEmail(email: String) {
        runGoTrue(email) {
            goTrueClient.resetPasswordForEmail(email)
            throw PasswordRecoveryEmailSent("User with $email has requested a password recovery email")
        }
    }

    fun updatePassword(password: String) {
        val user = SupabaseSecurityContextHolder.getAuthenticatedUser()
            ?: throw UnknownSupabaseException("No authenticated user found in SecurityContextRepository")
        val email = user.email ?: "no-email"
        runGoTrue(email) {
            val jwt = HtmxUtil.getCookie("JWT")?.value
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
            AuthErrorCode.EmailExists -> throw UserAlreadyRegisteredException(email)
            AuthErrorCode.UserAlreadyExists -> throw UserAlreadyRegisteredException(email)
            AuthErrorCode.SamePassword -> throw NewPasswordShouldBeDifferentFromOldPasswordException(email)
            AuthErrorCode.WeakPassword -> throw WeakPasswordException(email)
            AuthErrorCode.OtpExpired -> throw OtpExpiredException(email)
            AuthErrorCode.ValidationFailed -> throw ValidationFailedException(email)
            AuthErrorCode.NotAdmin -> throw MissingServiceRoleForAdminAccessException(SupabaseSecurityContextHolder.getAuthenticatedUser()?.id)
            else -> throw SupabaseAuthException(exc, email)
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
