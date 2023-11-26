package de.tschuehly.supabasesecurityspringbootstarter.application

import de.tschuehly.supabasesecurityspringbootstarter.SupabaseAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class
    ],
)
@ImportAutoConfiguration(SupabaseAutoConfiguration::class)

class TestApplication {

}

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}


