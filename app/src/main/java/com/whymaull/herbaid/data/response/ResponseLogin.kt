package com.whymaull.herbaid.data.response

import com.google.gson.annotations.SerializedName

data class ResponseLogin(

    @field:SerializedName("loginResult")
    val loginResult: LoginResult? = null,

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null

)
data class LoginResult(

    @field:SerializedName("token")
    val token: String? = null

)
