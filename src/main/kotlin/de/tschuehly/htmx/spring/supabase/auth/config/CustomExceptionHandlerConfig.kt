package de.tschuehly.htmx.spring.supabase.auth.config

import de.tschuehly.htmx.spring.supabase.auth.exception.handler.DefaultSupabaseExceptionHandler
import de.tschuehly.htmx.spring.supabase.auth.exception.handler.SupabaseExceptionHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ConditionalOnBean(SupabaseExceptionHandler::class)
@ComponentScan(
    basePackages = ["de.tschuehly.htmx.spring.supabase.auth"],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = [DefaultSupabaseExceptionHandler::class]
    )]
)
class CustomExceptionHandlerConfig