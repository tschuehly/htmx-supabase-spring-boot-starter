package de.tschuehly.supabasesecurityspringbootstarter.test

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

class GoTrueMockConfiguration {
    val mockEngine = GoTrueMock().engine

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()



    @OptIn(ExperimentalCoroutinesApi::class)
    @Bean
    public fun createSupabaseClient(): GoTrue {
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
