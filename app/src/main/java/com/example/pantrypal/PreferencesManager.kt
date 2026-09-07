package com.example.pantrypal

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SharedPreferences wrapper for persisting favorites, meal plan, and nutrition goals.
 */
object PreferencesManager {

    private const val PREFS_NAME = "pantrypal_prefs"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_MEAL_PLAN = "meal_plan"
    private const val KEY_NUTRITION = "nutrition_goal"

    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ─── Favorites ────────────────────────────────────────────────────────────

    fun getFavorites(context: Context): MutableList<Recipe> {
        val json = getPrefs(context).getString(KEY_FAVORITES, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Recipe>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveFavorite(context: Context, recipe: Recipe) {
        val list = getFavorites(context)
        if (list.none { it.idMeal == recipe.idMeal }) {
            list.add(recipe)
            getPrefs(context).edit()
                .putString(KEY_FAVORITES, gson.toJson(list))
                .apply()
        }
    }

    fun removeFavorite(context: Context, recipeId: String) {
        val list = getFavorites(context)
        list.removeAll { it.idMeal == recipeId }
        getPrefs(context).edit()
            .putString(KEY_FAVORITES, gson.toJson(list))
            .apply()
    }

    fun isFavorite(context: Context, recipeId: String): Boolean =
        getFavorites(context).any { it.idMeal == recipeId }

    // ─── Meal Plan ────────────────────────────────────────────────────────────

    fun getMealPlan(context: Context): List<MealPlanDay> {
        val json = getPrefs(context).getString(KEY_MEAL_PLAN, null)
        if (json != null) {
            val type = object : TypeToken<List<MealPlanDay>>() {}.type
            return try {
                gson.fromJson(json, type) ?: buildDefaultMealPlan()
            } catch (e: Exception) {
                buildDefaultMealPlan()
            }
        }
        return buildDefaultMealPlan()
    }

    fun saveMealPlan(context: Context, plan: List<MealPlanDay>) {
        getPrefs(context).edit()
            .putString(KEY_MEAL_PLAN, gson.toJson(plan))
            .apply()
    }

    private fun buildDefaultMealPlan(): List<MealPlanDay> = listOf(
        MealPlanDay("Monday", "MON"),
        MealPlanDay("Tuesday", "TUE"),
        MealPlanDay("Wednesday", "WED"),
        MealPlanDay("Thursday", "THU"),
        MealPlanDay("Friday", "FRI"),
        MealPlanDay("Saturday", "SAT"),
        MealPlanDay("Sunday", "SUN")
    )

    // ─── Nutrition Goals ──────────────────────────────────────────────────────

    fun getNutritionGoal(context: Context): NutritionGoal {
        val json = getPrefs(context).getString(KEY_NUTRITION, null) ?: return NutritionGoal()
        return try {
            gson.fromJson(json, NutritionGoal::class.java) ?: NutritionGoal()
        } catch (e: Exception) {
            NutritionGoal()
        }
    }

    fun saveNutritionGoal(context: Context, goal: NutritionGoal) {
        getPrefs(context).edit()
            .putString(KEY_NUTRITION, gson.toJson(goal))
            .apply()
    }
}
