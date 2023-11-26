package de.tschuehly.supabasesecurityspringbootstarter.exception

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Conditional
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
@Conditional(SupabaseExceptionHandlerExists::class)
class DefaultSupabaseExceptionHandler : SupabaseExceptionHandler {
    private final val logger: Logger = LoggerFactory.getLogger(DefaultSupabaseExceptionHandler::class.java)

    init {
        val msg =
            "You probably want to define a @ControllerAdvice that implements de.tschuehly.supabasesecurityspringbootstarter.exception.SupabaseExceptionHandler to handle exceptions from the " +
                    "supabase-security-spring-boot-starter and show messages to your user"
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
    override fun handleUserNeedsToConfirmEmail(exception: UserNeedsToConfirmEmailBeforeLoginException): Any {
        logger.debug(exception.message)
        return "UserNeedsToConfirmEmailBeforeLoginException"
    }

    @ResponseBody
    override fun handleSuccessfulRegistration(exception: SuccessfulSignUpConfirmationEmailSent): Any {
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

}