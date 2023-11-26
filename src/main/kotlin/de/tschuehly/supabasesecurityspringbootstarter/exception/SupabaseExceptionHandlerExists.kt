package de.tschuehly.supabasesecurityspringbootstarter.exception

import org.springframework.boot.autoconfigure.condition.AllNestedConditions
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.ConfigurationCondition
import org.springframework.web.bind.annotation.ControllerAdvice

class SupabaseExceptionHandlerExists :
    AllNestedConditions(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN) {
    companion object{
        @ConditionalOnMissingBean(annotation = [ControllerAdvice::class])
        class controllerAdviceMissing

        @ConditionalOnMissingBean(SupabaseExceptionHandler::class)
        class supabaseExceptionHandlerMissing

    }

}