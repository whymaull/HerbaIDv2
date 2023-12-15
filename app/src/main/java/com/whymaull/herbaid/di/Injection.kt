package com.whymaull.herbaid.di

import android.content.Context
import com.whymaull.herbaid.data.api.ApiConfig
import com.whymaull.herbaid.data.repository.UserRepository
import com.whymaull.herbaid.pref.UserPreferences
import com.whymaull.herbaid.pref.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreferences.getInstance(context.dataStore)
//        val database = StoryDB.getDatabase(context)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(apiService,user.token,pref)
    }
}