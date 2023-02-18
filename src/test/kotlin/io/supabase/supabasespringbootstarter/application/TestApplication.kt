package io.supabase.supabasespringbootstarter

import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.types.GoTrueTokenResponse
import io.supabase.supabasespringbootstarter.config.SupabaseProperties
import io.supabase.supabasespringbootstarter.types.SupabaseUser
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class
    ]
)
class TestApplication {
    @Bean
    fun supabaseGoTrueClient(supabaseProperties: SupabaseProperties): GoTrueClient<SupabaseUser, GoTrueTokenResponse> {
        return GoTrueClient.customApacheJacksonGoTrueClient(
            url = "http://localhost:9999",
            headers = emptyMap()
        )
    }


}

