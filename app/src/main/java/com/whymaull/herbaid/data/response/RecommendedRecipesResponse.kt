package com.whymaull.herbaid.data.response

import com.google.gson.annotations.SerializedName
import com.whymaull.herbaid.data.database.Recipe

data class RecommendedRecipesResponse(

    @field:SerializedName("recommendedRecipes")
    val recommendedRecipes: List<Recipe?>? = null

)
