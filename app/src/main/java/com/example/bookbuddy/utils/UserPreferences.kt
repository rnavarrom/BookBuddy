package com.example.bookbuddy.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_prefs"
val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class UserPreferences(context: Context) {

    private val dataStore = context.dataStore

    // Define the keys
    public object PreferencesKeys {
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
    }

    // Save username and password
    suspend fun saveCredentials(username: String, password: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
            preferences[PreferencesKeys.PASSWORD] = password
        }
    }

    // Retrive the username and password
    val userCredentialsFlow = dataStore.data.map { preferences ->
        val username = preferences[PreferencesKeys.USERNAME] ?: ""
        val password = preferences[PreferencesKeys.PASSWORD] ?: ""
        Pair(username, password)
    }
}