package de.tschuehly.htmx.spring.supabase.auth.events

data class SupabaseUserRolesUpdated(val id: String, val roles: List<String>) {
}