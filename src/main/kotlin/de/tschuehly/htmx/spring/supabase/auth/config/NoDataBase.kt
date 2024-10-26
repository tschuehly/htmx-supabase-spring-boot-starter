package de.tschuehly.htmx.spring.supabase.auth.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration


@ConditionalOnProperty(prefix = "supabase.database", name = ["host"], matchIfMissing = true)
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class, DataSourceTransactionManagerAutoConfiguration::class])
class NoDataBase {

}