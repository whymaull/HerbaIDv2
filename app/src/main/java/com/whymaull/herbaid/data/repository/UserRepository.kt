package com.whymaull.herbaid.data.repository

import androidx.lifecycle.LiveData
import com.whymaull.herbaid.data.api.ApiService
import com.whymaull.herbaid.pref.UserModel
import com.whymaull.herbaid.pref.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

class UserRepository private constructor(
    private val userPreferences: UserPreferences,
    private val apiService: ApiService
) {

    suspend fun saveSession(user: UserModel) {
        userPreferences.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreferences.getSession()
    }

    suspend fun logout() {
        userPreferences.logout()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            token:String,
            userPreference: UserPreferences,
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference,apiService)
            }.also { instance = it }
    }

}