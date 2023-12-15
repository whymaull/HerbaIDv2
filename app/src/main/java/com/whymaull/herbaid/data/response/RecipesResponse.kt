package com.whymaull.herbaid.data.response

import com.google.gson.annotations.SerializedName
import com.whymaull.herbaid.data.database.Recipe

data class RecipesResponse(

    @field:SerializedName("recipes")
    val recipes: List<Recipe?>? = null

)
