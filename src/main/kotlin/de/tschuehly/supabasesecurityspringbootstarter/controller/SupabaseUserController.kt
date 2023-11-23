package de.tschuehly.supabasesecurityspringbootstarter.controller

import de.tschuehly.supabasesecurityspringbootstarter.exception.MissingCredentialsException
import de.tschuehly.supabasesecurityspringbootstarter.exception.SuccessfulRegistrationConfirmationEmailSentException
import de.tschuehly.supabasesecurityspringbootstarter.service.ISupabaseUserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Controller
@RequestMapping("api/user")
class SupabaseUserController(
    val supabaseUserService: ISupabaseUserService,
) {
    val logger: Logger = LoggerFactory.getLogger(SupabaseUserController::class.java)

    @PostMapping("/register")
    fun register(
        request: HttpServletRequest
    ) {
        val email = request.parameterMap["email"]?.get(0)
        val password = request.parameterMap["password"]?.get(0)
        if (email != null && password != null) {
            logger.debug("User with the email $email is trying to register")
            val user = supabaseUserService.registerWithEmail(email.trim(), password.trim())
            logger.debug("User with the mail ${email} successfully registered, Confirmation Mail sent")
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestParam credentials: Map<String, String>,
        response: HttpServletResponse,
        request: HttpServletRequest
    ) {
        val email = credentials["email"]
        val password = credentials["password"]
        if (email != null && password != null) {
            logger.debug("User with the email $email is trying to login")
            supabaseUserService.loginWithEmail(email.trim(), password.trim(), response)
            logger.debug("User: $email successfully logged in")
        }
    }

    @PostMapping("/jwt")
    fun authorizeWithJwtOrResetPassword(request: HttpServletRequest, response: HttpServletResponse) {
        supabaseUserService.authorizeWithJwtOrResetPassword(request, response)
    }

    @GetMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        supabaseUserService.logout(request, response)
    }

    @PutMapping("/setRoles")
    @ResponseBody
    fun setRoles(
        @RequestParam
        roles: List<String>?,
        request: HttpServletRequest,
        @RequestParam
        userId: String,
    ) {
        if (userId == "") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId required")
        }
        supabaseUserService.setRolesWithRequest(request, userId, roles)
    }

    @PostMapping("/sendPasswordResetEmail")
    @ResponseBody
    fun sendPasswordResetEmail(
        @RequestParam
        email: String
    ) {
        logger.debug("User with the email $email requested a password reset")
        supabaseUserService.sendPasswordRecoveryEmail(email)
    }

    @PostMapping("/updatePassword")
    @ResponseBody
    fun updatePassword(
        request: HttpServletRequest,
        @RequestParam
        password: String
    ) {
        supabaseUserService.updatePassword(request, password)
    }
}
