package com.whymaull.herbaid.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whymaull.herbaid.data.api.ApiConfig
import com.whymaull.herbaid.data.repository.UserRepository
import com.whymaull.herbaid.data.response.ResponseLogin
import com.whymaull.herbaid.pref.UserModel
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel (private val reps: UserRepository) : ViewModel() {

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            reps.saveSession(user)
        }
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isMessage = MutableLiveData<String>()
    val isMessage: LiveData<String> = _isMessage

    fun isMessageNUlL() {
        _isMessage.value = ""
    }

    fun signIn(email: String, password: String) {
        _isLoading.value = true

        val client = ApiConfig.getApiService().signIn(email, password)

        client.enqueue(object : Callback<ResponseLogin> {
            override fun onResponse(

                call: Call<ResponseLogin>,
                response: Response<ResponseLogin>,
            ) {
                Log.i("SignupViewModel", "${response.code()}")

                if (response.isSuccessful) {

                    val appResponse = response.body()
                    saveSession(UserModel(token = appResponse?.loginResult?.token!!, isLogin = true))
                    Log.i(
                        "LoginViewModel", "$appResponse"
                    )
                    _isLoading.value = false
                    _isMessage.value = appResponse.message!!

                } else {

                    val str = response.errorBody()!!.string()
                    try {
                        val json = JSONObject(str)

                        _isMessage.value =
                            json.getString("message")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }

                _isLoading.postValue(false)
            }

            override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                Log.e("SignupViewModel", "Gagal daftar: ${t.message}")
                _isLoading.postValue(false)
            }
        })
    }

}