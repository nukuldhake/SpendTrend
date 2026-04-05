package com.example.spend_trend.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    private const val SUPABASE_URL = "https://ycrseexcomjeghqucana.supabase.co"
    private const val SUPABASE_ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InljcnNlZXhjb21qZWdocXVjYW5hIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ4NjEwNjMsImV4cCI6MjA5MDQzNzA2M30.H_UKiQh7m9yhEgGFcu8Gy3lXgEZqOcsYbLJ2RFqOylI"

    val client =
            createSupabaseClient(supabaseUrl = SUPABASE_URL, supabaseKey = SUPABASE_ANON_KEY) {
                install(Postgrest)
                install(Auth)
                install(Realtime)
            }
}
