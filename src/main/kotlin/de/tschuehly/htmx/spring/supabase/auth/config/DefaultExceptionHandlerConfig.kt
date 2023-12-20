package de.tschuehly.htmx.spring.supabase.auth.config

import de.tschuehly.htmx.spring.supabase.auth.exception.handler.SupabaseExceptionHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnMissingBean(SupabaseExceptionHandler::class)
@ComponentScan(
    basePackages = ["de.tschuehly.htmx.spring.supabase.auth"]
)
class DefaultExceptionHandlerConfig