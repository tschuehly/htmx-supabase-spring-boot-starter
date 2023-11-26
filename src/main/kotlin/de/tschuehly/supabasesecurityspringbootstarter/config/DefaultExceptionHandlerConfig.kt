package de.tschuehly.supabasesecurityspringbootstarter.config

import de.tschuehly.supabasesecurityspringbootstarter.exception.SupabaseExceptionHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnMissingBean(SupabaseExceptionHandler::class)
@ComponentScan(
    basePackages = ["de.tschuehly.supabasesecurityspringbootstarter"]
)
class DefaultExceptionHandlerConfig