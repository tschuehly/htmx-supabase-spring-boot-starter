package de.tschuehly.supabasesecurityspringbootstarter.application.advice

import org.slf4j.LoggerFactory


import de.tschuehly.supabasesecurityspringbootstarter.exception.*
import org.slf4j.Logger
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
class ExceptionHandlerAdvice : SupabaseExceptionHandler {
    val logger: Logger = LoggerFactory.getLogger(ExceptionHandlerAdvice::class.java)

    @ResponseBody
    override fun handleMissingCredentialsException(exception: MissingCredentialsException): String {
        logger.debug(exception.message)
        return exception.message ?: "MissingCredentialsException"
    }

    @ResponseBody
    override fun handleInvalidLoginCredentialsException(exception: InvalidLoginCredentialsException): String {
        logger.debug(exception.message)
        return "InvalidLoginCredentialsException"
    }

    @ResponseBody
    override fun handleUserNeedsToConfirmEmail(exception: UserNeedsToConfirmEmailBeforeLoginException): String {
        logger.debug(exception.message)
        return "UserNeedsToConfirmEmailBeforeLoginException"
    }

    @ResponseBody
    override fun handleSuccessfulRegistration(exception: SuccessfulSignUpConfirmationEmailSent): String {
        logger.debug(exception.message)
        return "SuccessfulRegistrationConfirmationEmailSent"
    }

    @ResponseBody
    override fun handlePasswordRecoveryEmailSent(exception: PasswordRecoveryEmailSent): String {
        logger.debug(exception.message)
        return "PasswordRecoveryEmailSent"
    }

    @ResponseBody
    override fun handleSuccessfulPasswordUpdate(exception: SuccessfulPasswordUpdate): String {
        logger.debug(exception.message)
        return "SuccessfulPasswordUpdate"
    }
}
