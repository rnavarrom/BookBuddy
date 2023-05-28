package com.example.bookbuddy.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_prefs"
val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

/**
 * Class to store log session of a user
 */
class UserPreferences(context: Context) {

    private val dataStore = context.dataStore
    object PreferencesKeys {
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
    }
    suspend fun saveCredentials(username: String, password: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
            preferences[PreferencesKeys.PASSWORD] = password
        }
    }
    val userCredentialsFlow = dataStore.data.map { preferences ->
        val username = preferences[PreferencesKeys.USERNAME] ?: ""
        val password = preferences[PreferencesKeys.PASSWORD] ?: ""
        Pair(username, password)
    }
}