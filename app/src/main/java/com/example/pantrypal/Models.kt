package com.example.pantrypal

import com.google.gson.annotations.SerializedName

// ─── Recipe Model ────────────────────────────────────────────────────────────
data class Recipe(
    @SerializedName("idMeal")         val idMeal: String = "",
    @SerializedName("strMeal")        val strMeal: String = "",
    @SerializedName("strCategory")    val strCategory: String = "",
    @SerializedName("strArea")        val strArea: String = "",
    @SerializedName("strInstructions") val strInstructions: String = "",
    @SerializedName("strMealThumb")   val strMealThumb: String = "",
    @SerializedName("strYoutube")     val strYoutube: String = "",

    // Ingredients
    @SerializedName("strIngredient1")  val strIngredient1: String? = null,
    @SerializedName("strIngredient2")  val strIngredient2: String? = null,
    @SerializedName("strIngredient3")  val strIngredient3: String? = null,
    @SerializedName("strIngredient4")  val strIngredient4: String? = null,
    @SerializedName("strIngredient5")  val strIngredient5: String? = null,
    @SerializedName("strIngredient6")  val strIngredient6: String? = null,
    @SerializedName("strIngredient7")  val strIngredient7: String? = null,
    @SerializedName("strIngredient8")  val strIngredient8: String? = null,
    @SerializedName("strIngredient9")  val strIngredient9: String? = null,
    @SerializedName("strIngredient10") val strIngredient10: String? = null,
    @SerializedName("strIngredient11") val strIngredient11: String? = null,
    @SerializedName("strIngredient12") val strIngredient12: String? = null,
    @SerializedName("strIngredient13") val strIngredient13: String? = null,
    @SerializedName("strIngredient14") val strIngredient14: String? = null,
    @SerializedName("strIngredient15") val strIngredient15: String? = null,
    @SerializedName("strIngredient16") val strIngredient16: String? = null,
    @SerializedName("strIngredient17") val strIngredient17: String? = null,
    @SerializedName("strIngredient18") val strIngredient18: String? = null,
    @SerializedName("strIngredient19") val strIngredient19: String? = null,
    @SerializedName("strIngredient20") val strIngredient20: String? = null,

    // Measurements
    @SerializedName("strMeasure1")  val strMeasure1: String? = null,
    @SerializedName("strMeasure2")  val strMeasure2: String? = null,
    @SerializedName("strMeasure3")  val strMeasure3: String? = null,
    @SerializedName("strMeasure4")  val strMeasure4: String? = null,
    @SerializedName("strMeasure5")  val strMeasure5: String? = null,
    @SerializedName("strMeasure6")  val strMeasure6: String? = null,
    @SerializedName("strMeasure7")  val strMeasure7: String? = null,
    @SerializedName("strMeasure8")  val strMeasure8: String? = null,
    @SerializedName("strMeasure9")  val strMeasure9: String? = null,
    @SerializedName("strMeasure10") val strMeasure10: String? = null,
    @SerializedName("strMeasure11") val strMeasure11: String? = null,
    @SerializedName("strMeasure12") val strMeasure12: String? = null,
    @SerializedName("strMeasure13") val strMeasure13: String? = null,
    @SerializedName("strMeasure14") val strMeasure14: String? = null,
    @SerializedName("strMeasure15") val strMeasure15: String? = null,
    @SerializedName("strMeasure16") val strMeasure16: String? = null,
    @SerializedName("strMeasure17") val strMeasure17: String? = null,
    @SerializedName("strMeasure18") val strMeasure18: String? = null,
    @SerializedName("strMeasure19") val strMeasure19: String? = null,
    @SerializedName("strMeasure20") val strMeasure20: String? = null
) {
    /** Returns a list of (ingredient, measure) pairs, filtering out empty entries */
    fun getIngredientList(): List<Pair<String, String>> {
        val ingredients = listOf(
            strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
            strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
            strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15,
            strIngredient16, strIngredient17, strIngredient18, strIngredient19, strIngredient20
        )
        val measures = listOf(
            strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
            strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
            strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15,
            strMeasure16, strMeasure17, strMeasure18, strMeasure19, strMeasure20
        )
        return ingredients.zip(measures)
            .filter { (ing, _) -> !ing.isNullOrBlank() }
            .map { (ing, meas) -> Pair(ing!!, meas?.trim() ?: "") }
    }
}

// ─── API Response Wrappers ───────────────────────────────────────────────────
data class MealSearchResponse(
    @SerializedName("meals") val meals: List<Recipe>?
)

// ─── Nutrition Goal ──────────────────────────────────────────────────────────
data class NutritionGoal(
    val calories: Int = 2000,
    val protein: Int = 150,
    val carbs: Int = 250,
    val fat: Int = 65
)

// ─── Meal Plan ───────────────────────────────────────────────────────────────
data class MealPlanDay(
    val dayName: String,
    val dayAbbr: String,
    var recipeName: String = "",
    var recipeId: String = "",
    var recipeThumb: String = ""
)

// ─── Weather Data ────────────────────────────────────────────────────────────
data class WeatherData(
    val temperature: Double,
    val weatherCode: Int,
    val isDay: Int
) {
    fun getDescription(): String = when (weatherCode) {
        0 -> if (isDay == 1) "Clear Sky ☀️" else "Clear Night 🌙"
        1 -> "Mainly Clear 🌤️"
        2 -> "Partly Cloudy ⛅"
        3 -> "Overcast ☁️"
        45, 48 -> "Foggy 🌫️"
        51, 53, 55 -> "Drizzle 🌦️"
        61, 63, 65 -> "Rainy 🌧️"
        71, 73, 75, 77 -> "Snowy ❄️"
        80, 81, 82 -> "Rain Showers 🌧️"
        85, 86 -> "Snow Showers 🌨️"
        95, 96, 99 -> "Thunderstorm ⛈️"
        else -> "Unknown 🌡️"
    }

    fun getWeatherEmoji(): String = when (weatherCode) {
        0 -> if (isDay == 1) "☀️" else "🌙"
        1 -> "🌤️"
        2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75, 77 -> "❄️"
        80, 81, 82 -> "🌧️"
        85, 86 -> "🌨️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }

    /**
     * Returns TheMealDB category to search based on weather & temperature
     * Hot → fresh seafood/vegan; Cold/Rain → hearty beef/lamb; Moderate → chicken/pasta
     */
    fun getRecommendedCategory(): Pair<String, String> = when {
        weatherCode in listOf(71, 73, 75, 77, 85, 86) ->
            Pair("Beef", "Perfect for a cold snowy day ❄️")
        weatherCode in listOf(61, 63, 65, 80, 81, 82, 95, 96, 99) ->
            Pair("Lamb", "Warm comfort food for rainy weather 🌧️")
        weatherCode in listOf(51, 53, 55, 45, 48) ->
            Pair("Pasta", "Cozy pasta for a drizzly day 🌦️")
        weatherCode in listOf(0, 1) && temperature > 25 ->
            Pair("Seafood", "Fresh & light for a sunny hot day ☀️")
        weatherCode in listOf(0, 1) && temperature > 15 ->
            Pair("Vegan", "Fresh salads for a beautiful day 🌤️")
        weatherCode in listOf(2, 3) ->
            Pair("Chicken", "Comforting chicken for a cloudy day ⛅")
        else -> Pair("Starter", "Light bites for today 🍽️")
    }
}
