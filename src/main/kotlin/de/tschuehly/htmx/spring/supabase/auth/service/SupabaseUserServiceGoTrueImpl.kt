package de.tschuehly.htmx.spring.supabase.auth.service

import de.tschuehly.htmx.spring.supabase.auth.config.SupabaseProperties
import de.tschuehly.htmx.spring.supabase.auth.exception.*
import de.tschuehly.htmx.spring.supabase.auth.exception.email.OtpEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.PasswordRecoveryEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.RegistrationConfirmationEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.SuccessfulPasswordUpdate
import de.tschuehly.htmx.spring.supabase.auth.exception.info.InvalidLoginCredentialsException
import de.tschuehly.htmx.spring.supabase.auth.exception.info.NewPasswordShouldBeDifferentFromOldPasswordException
import de.tschuehly.htmx.spring.supabase.auth.exception.info.UserAlreadyRegisteredException
import de.tschuehly.htmx.spring.supabase.auth.exception.info.UserNeedsToConfirmEmailBeforeLoginException
import de.tschuehly.htmx.spring.supabase.auth.security.SupabaseJwtFilter.Companion.setJWTCookie
import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.admin.AdminUserBuilder
import io.github.jan.supabase.gotrue.mfa.FactorType
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserMfaFactor
import io.github.jan.supabase.gotrue.user.UserSession
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder

class SupabaseUserServiceGoTrueImpl(
    private val supabaseProperties: SupabaseProperties,
    private val goTrueClient: Auth
) : SupabaseUserService {
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

    override fun signUpWithEmailAndMeta(
        request: HttpServletRequest,
        email: String,
        password: String,
        meta: Map<String, String>,
        response: HttpServletResponse
    ): UserInfo? {
        return runBlocking {
            val deferredUserInfo: Deferred<UserInfo?> = async {
                runGoTrueWithResult(email = email) {
                    request.cookies?.find { it.name == "JWT" }?.let {
                        val jsonMap = meta.mapValues { v -> JsonPrimitive(v.value) }

                        goTrueClient.importAuthToken(it.value)
                        goTrueClient.admin.createUserWithEmail {
                            val ub = AdminUserBuilder.Email()
                            ub.email = email
                            ub.password = password
                            ub.userMetadata = JsonObject(jsonMap)
                        }
                    }
                }
            }

            val userInfo = deferredUserInfo.await()
            if (userInfo != null) {
                loginWithEmail(email, password, response)
                logger.debug("User with the mail $email successfully signed up")
            } else {
                logger.error("Failed to sign up user with the mail $email")
            }
            userInfo
        }
    }

    override fun signUpWithEmailAndMeta(
        serviceJwt: String,
        email: String,
        password: String,
        meta: Map<String, String>,
        response: HttpServletResponse
    ): UserInfo? {
        return runBlocking {
            val deferredUserInfo: Deferred<UserInfo?> = async {
                runGoTrueWithResult(email = email) {
                    val jsonMap = meta.mapValues { v -> JsonPrimitive(v.value) }
                    goTrueClient.importAuthToken(serviceJwt)
                    goTrueClient.admin.createUserWithEmail {
                        this@createUserWithEmail.password = password
                        this@createUserWithEmail.email = email
                        this@createUserWithEmail.userMetadata = JsonObject(jsonMap)
                    }
                }
            }

            val userInfo = deferredUserInfo.await()
            if (userInfo != null) {
                loginWithEmail(email, password, response)
                logger.debug("User with the mail $email successfully signed up")
            } else {
                logger.error("Failed to sign up user with the mail $email")
            }
            userInfo
        }
    }

    override fun inviteUserByEmail(
        serviceJwt: String,
        email: String,
        redirectUrl: String,
        meta: Map<String, String>,
        response: HttpServletResponse
    ) {
        runGoTrue {
            val jsonMap = meta.mapValues { v -> JsonPrimitive(v.value) }
            goTrueClient.importAuthToken(serviceJwt)
            goTrueClient.admin.inviteUserByEmail(email, redirectUrl, JsonObject(jsonMap))
        }
    }


    override fun loginWithEmail(email: String, password: String, response: HttpServletResponse) {
        runGoTrue(email) {
            goTrueClient.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val token = goTrueClient.currentSessionOrNull()?.accessToken
                ?: throw JWTTokenNullException("The JWT that $email requested from supabase is null")
            response.setJWTCookie(token, supabaseProperties)
            response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
            logger.debug("User: $email successfully logged in")
        }
    }

    override fun sendOtp(email: String) {
        runGoTrue(email) {
            goTrueClient.signInWith(OTP) {
                this.email = email
                this.createUser = supabaseProperties.otpCreateUser
            }
            throw OtpEmailSent(email)
        }
    }

    override fun authorizeWithJwtOrResetPassword(
        request: HttpServletRequest, response: HttpServletResponse
    ) {
        val header = request.getHeader("HX-Current-URL") ?: throw HxCurrentUrlHeaderNotFound()
        val user = SecurityContextHolder.getContext().authentication.principal as SupabaseUser
        if (header.contains("type=recovery")) {
            logger.debug("User: ${user.email} is trying to reset his password")
            response.setHeader("HX-Redirect", supabaseProperties.passwordRecoveryPage)
        } else {
            response.setHeader("HX-Redirect", supabaseProperties.successfulLoginRedirectPage)
        }
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
            goTrueClient.modifyUser(true) {
                this.password = password
            }
            throw SuccessfulPasswordUpdate(user.email)
        }
    }

    override fun enrollSecondFactor(
        request: HttpServletRequest,
        issuer: String,
        deviceName: String
    ): Map<String, String>? {
        return runBlocking {
            val deferredResult = async {
                runGoTrueWithResult {
                    val jwt = request.cookies?.find { it.name == "JWT" }?.value
                        ?: throw JWTTokenNullException("No JWT found in request")
                    goTrueClient.importAuthToken(jwt)

                    try {
                        // Enroll the second factor using TOTP
                        val factorResponse = goTrueClient.mfa.enroll(FactorType.TOTP, deviceName) {
                            this.issuer = issuer
                        }
                        val factor = factorResponse.data ?: throw Exception("Failed to enroll second factor")

                        // Extract the necessary details
                        val id = factor.uri
                        val qrCode = factor.qrCode // SVG as a string
                        val secret = factor.secret

                        // Return the necessary information as a map
                        mapOf(
                            "id" to id,
                            "qrCode" to qrCode,
                            "secret" to secret
                        )
                    } catch (e: RestException) {
                        // TODO: handle exceptions
                        null
                    } finally {
                        goTrueClient.clearSession()
                    }
                }
            }

            val result = deferredResult.await()
            // TODO: error handling
            result
        }
    }

    override fun retrieveMFAFactorsForCurrentUser(request: HttpServletRequest): List<UserMfaFactor>? {
        return runBlocking {
            val deferredResult = async {
                runGoTrueWithResult {
                    val jwt = request.cookies?.find { it.name == "JWT" }?.value
                        ?: throw JWTTokenNullException("No JWT found in request")
                    goTrueClient.importAuthToken(jwt)
                    try {
                        val factors = goTrueClient.mfa.retrieveFactorsForCurrentUser()
                        factors
                    } catch (e: RestException) {
                        // Handle the exception if needed
                        null
                    } finally {
                        goTrueClient.clearSession()
                    }
                }
            }

            val result = deferredResult.await()
            // TODO: error handling
            result
        }
    }

    override fun createAndVerifyMFAChallenge(request: HttpServletRequest, factorId: String, code: String): UserSession? {
        return runBlocking {
            val deferredResult = async {
                runGoTrueWithResult {
                    val jwt = request.cookies?.find { it.name == "JWT" }?.value
                        ?: throw JWTTokenNullException("No JWT found in request")
                    goTrueClient.importAuthToken(jwt)
                    try {
                        val challenge = goTrueClient.mfa.createChallenge(factorId)
                        val userSession = goTrueClient.mfa.verifyChallenge(factorId, challenge.id, code, true)
                        userSession
                    } catch (e: RestException) {
                        // TODO: handle exceptions
                        null
                    } finally {
                        goTrueClient.clearSession()
                    }
                }
            }

            val result = deferredResult.await()
            result
        }
    }

    override fun unenrollMfaFactor(request: HttpServletRequest, factorId: String) {
        return runBlocking {
            val deferredResult = async {
                runGoTrue {
                    val jwt = request.cookies?.find { it.name == "JWT" }?.value
                        ?: throw JWTTokenNullException("No JWT found in request")
                    goTrueClient.importAuthToken(jwt)
                    try {
                        goTrueClient.mfa.unenroll(factorId)
                    } catch (e: RestException) {
                        // TODO: Handle exceptions
                    } finally {
                        goTrueClient.clearSession()
                    }
                }
            }
        }
    }

    override fun loggedInUsingMfa(request: HttpServletRequest): Boolean {
        return runBlocking {
            val deferredResult = async {
                runGoTrueWithResult {
                    val jwt = request.cookies?.find { it.name == "JWT" }?.value
                        ?: throw JWTTokenNullException("No JWT found in request")
                    goTrueClient.importAuthToken(jwt)
                    goTrueClient.mfa.status.active
                }
            }

            val result = deferredResult.await()
            result ?: false
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

    private suspend fun <T> runGoTrueWithResult(
        email: String = "no-email",
        userId: String = "no-userid",
        block: suspend () -> T?
    ): T? {
        return try {
            block()
        } catch (e: RestException) {
            handleGoTrueException(e, email, userId)
            null
        } finally {
            goTrueClient.clearSession()
        }
    }

    private fun handleGoTrueException(e: RestException, email: String, userId: String) {
        val message = e.message ?: let {
            logger.error(e.message)
            throw UnknownSupabaseException()
        }
        when {
            message.contains("User already registered", true) -> throw UserAlreadyRegisteredException(email)
            message.contains("already been registered", true) -> throw UserAlreadyRegisteredException(email)
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
        return user != null
    }
}
