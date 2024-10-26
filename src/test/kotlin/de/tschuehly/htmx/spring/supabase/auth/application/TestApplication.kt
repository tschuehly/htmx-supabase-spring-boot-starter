package de.tschuehly.htmx.spring.supabase.auth.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource

@SpringBootApplication
@PropertySource(value = ["classpath:/test.properties"], ignoreResourceNotFound = true)
class TestApplication {

}

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}


