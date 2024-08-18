package de.tschuehly.htmx.spring.supabase.auth.application

import de.tschuehly.htmx.spring.supabase.auth.SupabaseAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication
class TestApplication {

}

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}


