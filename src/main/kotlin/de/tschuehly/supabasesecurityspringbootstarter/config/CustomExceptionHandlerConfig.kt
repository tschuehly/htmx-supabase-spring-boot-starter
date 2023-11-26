package de.tschuehly.supabasesecurityspringbootstarter.config

import de.tschuehly.supabasesecurityspringbootstarter.exception.DefaultSupabaseExceptionHandler
import de.tschuehly.supabasesecurityspringbootstarter.exception.SupabaseExceptionHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ConditionalOnBean(SupabaseExceptionHandler::class)
@ComponentScan(
    basePackages = ["de.tschuehly.supabasesecurityspringbootstarter"],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = [DefaultSupabaseExceptionHandler::class]
    )]
)
class CustomExceptionHandlerConfig