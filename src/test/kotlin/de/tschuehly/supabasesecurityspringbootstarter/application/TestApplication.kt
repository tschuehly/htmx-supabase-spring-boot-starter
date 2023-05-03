package de.tschuehly.supabasesecurityspringbootstarter.application

import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseAutoConfiguration
import de.tschuehly.supabasesecurityspringbootstarter.config.SupabaseProperties
import de.tschuehly.supabasesecurityspringbootstarter.types.SupabaseUser
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.types.GoTrueTokenResponse
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class
    ],

)
@ImportAutoConfiguration(SupabaseAutoConfiguration::class)
class TestApplication {


    @Bean
    fun supabaseGoTrueClient(supabaseProperties: SupabaseProperties): GoTrueClient<SupabaseUser, GoTrueTokenResponse> {
        return GoTrueClient.customApacheJacksonGoTrueClient(
            url = "http://localhost:9999",
            headers = emptyMap()
        )
    }


}

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}


