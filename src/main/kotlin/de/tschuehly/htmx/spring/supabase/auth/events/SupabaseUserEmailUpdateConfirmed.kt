package de.tschuehly.htmx.spring.supabase.auth.events

import java.util.*

data class SupabaseUserEmailUpdateConfirmed(val id: UUID, val email: String) {
}