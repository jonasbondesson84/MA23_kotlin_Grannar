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

    val categorieFonts = mapOf<String, Int>(
        "Sport" to R.color.sportCategoryText,
        "Nature" to R.color.natureCategoryText,
        "Animals" to R.color.animalsCategoryText,
        "Music" to R.color.musicCategoryText,
        "Literature" to R.color.literatureCategoryText,
        "Travel" to R.color.travelCategoryText,
        "Games" to R.color.gamesCategoryText,
        "Exercise" to R.color.exerciseCategoryText,
        "Other" to R.color.otherCategoryText)


    fun getCategoryNames():List<String>{
        return categories.keys.toList()
    }
    fun getCategoryColorId(categoryName: String?): Int {
        return categories[categoryName] ?: R.color.otherCategory
    }

    fun getCategoryTextColorID(categoryName: String?): Int{
        return categorieFonts[categoryName] ?: R.color.otherCategoryText
    }
}