//package de.tschuehly.supabasesecurityspringbootstarter.application
//
//import de.tschuehly.supabasesecurityspringbootstarter.exception.*
//import org.springframework.web.bind.annotation.ControllerAdvice
//import org.springframework.web.bind.annotation.ResponseBody
//
//class CustomExceptionHandler: SupabaseExceptionHandler  {
//    @ResponseBody
//    override fun handleMissingCredentialsException(exception: MissingCredentialsException): Any {
//        println("Test")
//        return "test"
//    }
//
//    override fun handleInvalidLoginCredentialsException(exception: InvalidLoginCredentialsException): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun handleUserNeedsToConfirmEmail(exception: UserNeedsToConfirmEmailBeforeLoginException): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun handleSuccessfulRegistration(exception: SuccessfulSignUpConfirmationEmailSent): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun handlePasswordRecoveryEmailSent(exception: PasswordRecoveryEmailSent): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun handleSuccessfulPasswordUpdate(exception: SuccessfulPasswordUpdate): Any {
//        TODO("Not yet implemented")
//    }
//}