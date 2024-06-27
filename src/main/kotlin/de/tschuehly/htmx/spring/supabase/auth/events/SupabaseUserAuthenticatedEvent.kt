package de.tschuehly.htmx.spring.supabase.auth.events

import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser


data class SupabaseUserAuthenticatedEvent(val user: SupabaseUser) {
}