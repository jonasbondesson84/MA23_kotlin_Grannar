package com.example.grannar

object CategoryManager {

     val categories = mapOf<String, Int>(
         "Sport" to R.color.sportCategory,
         "Nature" to R.color.natureCategory,
         "Animals" to R.color.animalsCategory,
         "Music" to R.color.musicCategory,
         "Literature" to R.color.literatureCategory,
         "Travel" to R.color.travelCategory,
         "Games" to R.color.gamesCategory,
         "Exercise" to R.color.exerciseCategory,
         "Other" to R.color.otherCategory)


    fun getCategoryNames():List<String>{
        return categories.keys.toList()
    }
    fun getCategoryColorId(categoryName: String?): Int {
        return categories[categoryName] ?: R.color.otherCategory
    }
}