package com.whymaull.herbaid.data.response

import com.google.gson.annotations.SerializedName
import com.whymaull.herbaid.data.database.Recipe

data class ComplaintResponse(

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("recommendedRecipes")
    val recommendedRecipes: List<Recipe?>? = null
)