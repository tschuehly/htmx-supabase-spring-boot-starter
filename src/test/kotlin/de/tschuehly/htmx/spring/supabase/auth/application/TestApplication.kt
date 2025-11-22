package de.tschuehly.htmx.spring.supabase.auth.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.health.DataSourceHealthContributorAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource

@SpringBootApplication(exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceHealthContributorAutoConfiguration::class
    ])
@PropertySource(value = ["classpath:/test.properties"], ignoreResourceNotFound = true)
class TestApplication {

}

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}


