package de.tschuehly.htmx.spring.supabase.auth.events

import java.util.UUID

data class SupabaseUserEmailUpdated(val id: UUID, val email: String) {
}