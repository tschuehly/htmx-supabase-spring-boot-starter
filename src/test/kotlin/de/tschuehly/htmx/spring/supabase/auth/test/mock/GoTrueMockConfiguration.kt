package de.tschuehly.htmx.spring.supabase.auth.test.mock

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.test.StandardTestDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GoTrueMockConfiguration {
    val mockEngine = GoTrueMock().engine

    val dispatcher = StandardTestDispatcher()


    @Bean
    fun createSupabaseClient(): Auth {
        val supabase = createSupabaseClient(
            supabaseUrl = "https://example.com",
            supabaseKey = "example",
        ) {
            httpEngine = mockEngine
            install(Auth) {
                coroutineDispatcher = dispatcher
                autoSaveToStorage = false
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
        }
        return supabase.auth
    }
}
