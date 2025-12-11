package de.tschuehly.htmx.spring.supabase.auth.controller

import de.tschuehly.htmx.spring.supabase.auth.exception.info.MissingCredentialsException.Companion.MissingCredentials
import de.tschuehly.htmx.spring.supabase.auth.service.SupabaseUserService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Controller
class SupabaseUserController(
    val supabaseUserService: SupabaseUserService,
) {
    companion object {
        const val BASE_PATH = "/api/user"
        const val LOGIN = "$BASE_PATH/login"
        const val SIGNUP = "$BASE_PATH/signup"
        const val LOGIN_ANON = "$BASE_PATH/loginAnon"
        const val LOGIN_ANON_WITH_EMAIL = "$BASE_PATH/loginAnonWithEmail"
        const val LINK_IDENTITY = "$BASE_PATH/linkIdentity"
        const val SIGN_IN_WITH_MAGIC_LINK = "$BASE_PATH/signInWithMagicLink"
        const val CONFIRM_EMAIL_OTP = "$BASE_PATH/confirmEmailOtp"
        const val JWT = "$BASE_PATH/jwt"
        const val LOGOUT = "$BASE_PATH/logout"
        const val SET_ROLES = "$BASE_PATH/setRoles"
        const val SEND_PASSWORD_RESET_EMAIL = "$BASE_PATH/sendPasswordResetEmail"
        const val UPDATE_PASSWORD = "$BASE_PATH/updatePassword"
    }

    val logger: Logger = LoggerFactory.getLogger(SupabaseUserController::class.java)


    @PostMapping(LOGIN)
    fun login(
        @RequestParam email: String?,
        @RequestParam password: String?,
        response: HttpServletResponse,
    ) {
        checkCredentialsAndExecute(email, password) { checkedEmail, checkedPassword ->
            logger.debug("User with the email $checkedEmail is trying to login")
            supabaseUserService.loginWithEmail(checkedEmail, checkedPassword)
        }
    }

    @PostMapping(SIGNUP)
    fun signUp(
        @RequestParam email: String?,
        @RequestParam password: String?
    ) {
        checkCredentialsAndExecute(email, password) { checkedEmail, checkedPassword ->
            logger.debug("User with the email $checkedEmail is trying to signup")
            supabaseUserService.signUpWithEmail(checkedEmail, checkedPassword)
        }
    }

    @PostMapping(LOGIN_ANON)
    @ResponseBody
    fun anonSignIn() {
        supabaseUserService.signInAnonymously()
    }

    @PostMapping(LOGIN_ANON_WITH_EMAIL)
    @ResponseBody
    fun anonSignInWithEmail(@RequestParam email: String?) {
        if (email != null) {
            supabaseUserService.signInAnonymouslyWithEmail(email.trim().lowercase())
        } else {
            MissingCredentials.EMAIL_MISSING.throwExc()
        }
    }


    @PostMapping(LINK_IDENTITY)
    fun linkIdentity(
        @RequestParam email: String?
    ) {
        if (email != null) {
            logger.debug("User with the email $email is linking an Anonymous User")
            supabaseUserService.requestEmailChange(email.trim().lowercase())
        } else {
            MissingCredentials.EMAIL_MISSING.throwExc()
        }
    }

    @PostMapping(SIGN_IN_WITH_MAGIC_LINK)
    fun sendEmailOtp(
        @RequestParam email: String?
    ) {
        if (email != null) {
            logger.debug("User with the email $email is trying to sign in with a Magic Link")
            supabaseUserService.signInWithMagicLink(email.trim().lowercase())
        } else {
            MissingCredentials.EMAIL_MISSING.throwExc()
        }
    }

    @PostMapping(CONFIRM_EMAIL_OTP)
    @ResponseBody
    fun confirmEmailOtp(
        @RequestParam email: String?,
        @RequestParam otp: String?
    ) {
        if (email.isNullOrBlank()) {
            MissingCredentials.EMAIL_MISSING.throwExc()
        }
        if (otp.isNullOrBlank()) {
            MissingCredentials.OTP_MISSING.throwExc()
        }
        logger.debug("User with the email $email is confirming an OTP")
        supabaseUserService.confirmEmailOtp(email!!.trim().lowercase(), otp!!)
    }


    private fun checkCredentialsAndExecute(
        email: String?, password: String?,
        function: (email: String, password: String) -> Unit
    ) {
        when {
            email.isNullOrBlank() && password.isNullOrBlank() ->
                MissingCredentials.PASSWORD_AND_EMAIL_MISSING.throwExc()

            email.isNullOrBlank() ->
                MissingCredentials.EMAIL_MISSING.throwExc()

            password.isNullOrBlank() ->
                MissingCredentials.PASSWORD_MISSING.throwExc()

            else ->
                function(email.trim().lowercase(), password.trim())
        }
    }

    @PostMapping(JWT)
    @ResponseBody
    fun authorizeWithJwtOrResetPassword() {
        supabaseUserService.handleClientAuthentication()
    }

    @GetMapping(LOGOUT)
    @ResponseBody
    fun logout() {
        supabaseUserService.logout()
    }

    @PutMapping(SET_ROLES)
    @ResponseBody
    fun setRoles(
        @RequestParam
        roles: List<String>?,
        @RequestParam
        userId: String,
    ) {
        if (userId == "") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId required")
        }
        supabaseUserService.setRolesWithRequest(userId, roles)
    }

    @PostMapping(SEND_PASSWORD_RESET_EMAIL)
    @ResponseBody
    fun sendPasswordResetEmail(
        @RequestParam
        email: String
    ) {
        logger.debug("User with the email $email requested a password reset")
        supabaseUserService.sendPasswordRecoveryEmail(email.trim().lowercase())
    }

    @PostMapping(UPDATE_PASSWORD)
    @ResponseBody
    fun updatePassword(@RequestParam password: String) {
        supabaseUserService.updatePassword(password.trim())
    }
}