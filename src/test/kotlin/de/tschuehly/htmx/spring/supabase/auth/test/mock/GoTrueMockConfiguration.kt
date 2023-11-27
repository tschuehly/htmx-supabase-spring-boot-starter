package de.tschuehly.htmx.spring.supabase.auth.test.mock

import com.russhwolf.settings.MapSettings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.SettingsCodeVerifierCache
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.gotrue.gotrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GoTrueMockConfiguration {
    val mockEngine = GoTrueMock().engine

    val dispatcher = StandardTestDispatcher()


    @Bean
    fun createSupabaseClient(): GoTrue {
        return createSupabaseClient(
            supabaseUrl = "https://example.com",
            supabaseKey = "example",
        ) {
            httpEngine = mockEngine

            install(GoTrue) {
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
                coroutineDispatcher = dispatcher
                sessionManager = SettingsSessionManager(MapSettings())
                codeVerifierCache = SettingsCodeVerifierCache(MapSettings())

            }
        }.gotrue
    }
}
