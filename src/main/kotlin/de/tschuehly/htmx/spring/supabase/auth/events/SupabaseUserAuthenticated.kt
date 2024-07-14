package de.tschuehly.htmx.spring.supabase.auth.events

import de.tschuehly.htmx.spring.supabase.auth.types.SupabaseUser


data class SupabaseUserAuthenticated(val user: SupabaseUser) {
}