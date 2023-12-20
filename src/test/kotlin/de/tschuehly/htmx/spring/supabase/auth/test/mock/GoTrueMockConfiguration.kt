package de.tschuehly.htmx.spring.supabase.auth.test.mock

import com.russhwolf.settings.MapSettings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SettingsCodeVerifierCache
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
