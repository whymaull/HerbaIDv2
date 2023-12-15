package com.whymaull.herbaid.data.response

import com.google.gson.annotations.SerializedName
import com.whymaull.herbaid.data.database.Recipe

data class FavoriteRecipesResponse(

    @field:SerializedName("favoriteRecipes")
    val favoriteRecipes: List<Recipe?>? = null

)