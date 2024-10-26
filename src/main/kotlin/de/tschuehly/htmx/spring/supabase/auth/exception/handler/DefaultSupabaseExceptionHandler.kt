package de.tschuehly.htmx.spring.supabase.auth.exception.handler

import de.tschuehly.htmx.spring.supabase.auth.exception.MissingServiceRoleForAdminAccessException
import de.tschuehly.htmx.spring.supabase.auth.exception.SupabaseAuthException
import de.tschuehly.htmx.spring.supabase.auth.exception.UnknownSupabaseException
import de.tschuehly.htmx.spring.supabase.auth.exception.WeakPasswordException
import de.tschuehly.htmx.spring.supabase.auth.exception.email.OtpEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.PasswordRecoveryEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.RegistrationConfirmationEmailSent
import de.tschuehly.htmx.spring.supabase.auth.exception.email.SuccessfulPasswordUpdate
import de.tschuehly.htmx.spring.supabase.auth.exception.info.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
open class DefaultSupabaseExceptionHandler : SupabaseExceptionHandler {
    private final val logger: Logger = LoggerFactory.getLogger(DefaultSupabaseExceptionHandler::class.java)

    init {
        val msg =
            "You probably want to define a @ControllerAdvice that implements de.tschuehly.htmx.spring.supabase.auth.exception.handler.SupabaseExceptionHandler to handle exceptions from the " +
                    "htmx-supabase-spring-boot-starter and show messages to your user"
        logger.warn(msg)
    }

    @ResponseBody
    override fun handleMissingCredentialsException(exception: MissingCredentialsException): Any {
        logger.debug(exception.message)
        return exception.message ?: "MissingCredentialsException"
    }

    @ResponseBody
    override fun handleInvalidLoginCredentialsException(exception: InvalidLoginCredentialsException): Any {
        logger.debug(exception.message)
        return "InvalidLoginCredentialsException"
    }

    @ResponseBody
    override fun handleUserNeedsToConfirmEmailBeforeLogin(exception: UserNeedsToConfirmEmailBeforeLoginException): Any {
        logger.debug(exception.message)
        return "UserNeedsToConfirmEmailBeforeLoginException"
    }

    @ResponseBody
    override fun handleUserNeedsToConfirmEmailForEmailChange(exception: UserNeedsToConfirmEmailForEmailChangeException): Any {
        logger.debug(exception.message)
        return "UserNeedsToConfirmEmailForEmailChangeException"
    }

    @ResponseBody
    override fun handleSuccessfulRegistration(exception: RegistrationConfirmationEmailSent): Any {
        logger.debug(exception.message)
        return "SuccessfulRegistrationConfirmationEmailSent"
    }

    @ResponseBody
    override fun handlePasswordRecoveryEmailSent(exception: PasswordRecoveryEmailSent): Any {
        logger.debug(exception.message)
        return "PasswordRecoveryEmailSent"
    }

    @ResponseBody
    override fun handleSuccessfulPasswordUpdate(exception: SuccessfulPasswordUpdate): Any {
        logger.debug(exception.message)
        return "SuccessfulPasswordUpdate"
    }

    @ResponseBody
    override fun handleOtpEmailSent(exception: OtpEmailSent): Any {
        logger.debug(exception.message)
        return "OtpEmailSent"
    }

    @ResponseBody
    override fun handleUserAlreadyRegisteredException(exception: UserAlreadyRegisteredException): Any {
        logger.debug(exception.message)
        return "UserAlreadyRegisteredException"
    }

    @ResponseBody
    override fun handleWeakPasswordException(exception: WeakPasswordException): Any {
        logger.debug(exception.message)
        return "WeakPasswordException"
    }

    @ResponseBody
    override fun handlePasswordChangeError(exception: NewPasswordShouldBeDifferentFromOldPasswordException): Any {
        logger.debug(exception.message)
        return "NewPasswordShouldBeDifferentFromOldPasswordException"
    }

    @ResponseBody
    override fun handleMissingServiceRoleForAdminAccessException(exception: MissingServiceRoleForAdminAccessException): Any {
        logger.debug(exception.message)
        return "MissingServiceRoleForAdminAccessException"
    }

    @ResponseBody
    override fun handleSupabaseAuthException(exception: SupabaseAuthException): Any {
        logger.debug("${exception.message}: ${exception.errorCode}: ${exception.error}")
        return "SupabaseAuthException"
    }

    @ResponseBody
    override fun handleUnknownSupabaseException(exception: UnknownSupabaseException): Any {
        logger.debug(exception.message)
        return "UnknownSupabaseException"
    }


}