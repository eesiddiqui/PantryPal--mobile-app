package com.example.pantrypal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * RecipeDetailsActivity – shows full recipe information including:
 *  - Hero image with category/area tags
 *  - Ingredient list (RecyclerView)
 *  - Step-by-step instructions
 *  - Favorite toggle (persisted via SharedPreferences)
 *  - Add to Meal Plan action
 *  - Watch YouTube video link
 */
class RecipeDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MEAL_ID = "meal_id"
    }

    private lateinit var pbLoading: ProgressBar
    private lateinit var scrollDetails: NestedScrollView
    private lateinit var ivRecipeImage: ImageView
    private lateinit var btnBack: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var tvRecipeTitle: TextView
    private lateinit var tvCategoryTag: TextView
    private lateinit var tvAreaTag: TextView
    private lateinit var btnAddMealPlan: TextView
    private lateinit var btnWatchVideo: TextView
    private lateinit var rvIngredients: RecyclerView
    private lateinit var tvInstructions: TextView

    private var currentRecipe: Recipe? = null
    private lateinit var ingredientAdapter: IngredientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        bindViews()
        setupIngredientList()

        val mealId = intent.getStringExtra(EXTRA_MEAL_ID) ?: run { finish(); return }
        loadRecipeDetails(mealId)
    }

    private fun bindViews() {
        pbLoading = findViewById(R.id.pb_details_loading)
        scrollDetails = findViewById(R.id.scroll_details)
        ivRecipeImage = findViewById(R.id.iv_recipe_image)
        btnBack = findViewById(R.id.btn_back)
        btnFavorite = findViewById(R.id.btn_favorite)
        tvRecipeTitle = findViewById(R.id.tv_recipe_title)
        tvCategoryTag = findViewById(R.id.tv_category_tag)
        tvAreaTag = findViewById(R.id.tv_area_tag)
        btnAddMealPlan = findViewById(R.id.btn_add_meal_plan)
        btnWatchVideo = findViewById(R.id.btn_watch_video)
        rvIngredients = findViewById(R.id.rv_ingredients)
        tvInstructions = findViewById(R.id.tv_instructions)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupIngredientList() {
        ingredientAdapter = IngredientAdapter()
        rvIngredients.layoutManager = LinearLayoutManager(this)
        rvIngredients.adapter = ingredientAdapter
        rvIngredients.isNestedScrollingEnabled = false
    }

    private fun loadRecipeDetails(id: String) {
        pbLoading.visibility = View.VISIBLE
        scrollDetails.visibility = View.GONE

        NetworkClient.fetchRecipeById(id,
            onResult = { recipe ->
                pbLoading.visibility = View.GONE
                if (recipe != null) {
                    currentRecipe = recipe
                    populateUI(recipe)
                    scrollDetails.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
            onError = {
                pbLoading.visibility = View.GONE
                Toast.makeText(this, "Failed to load recipe. Check your connection.", Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }

    private fun populateUI(recipe: Recipe) {
        // Hero image
        Glide.with(this)
            .load(recipe.strMealThumb)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(ivRecipeImage)

        // Title and tags
        tvRecipeTitle.text = recipe.strMeal
        tvCategoryTag.text = recipe.strCategory.ifBlank { "Food" }
        tvAreaTag.text = if (recipe.strArea.isNotBlank()) "🌍 ${recipe.strArea}" else "International"

        // Instructions
        tvInstructions.text = recipe.strInstructions
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()

        // Ingredients
        ingredientAdapter.submitList(recipe.getIngredientList())

        // Favorite button
        updateFavoriteButton()
        btnFavorite.setOnClickListener { toggleFavorite(recipe) }

        // Meal plan button
        btnAddMealPlan.setOnClickListener { showMealPlanDialog(recipe) }

        // YouTube video
        if (recipe.strYoutube.isNotBlank()) {
            btnWatchVideo.visibility = View.VISIBLE
            btnWatchVideo.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recipe.strYoutube)))
            }
        } else {
            btnWatchVideo.visibility = View.GONE
        }
    }

    private fun updateFavoriteButton() {
        val recipe = currentRecipe ?: return
        val isFav = PreferencesManager.isFavorite(this, recipe.idMeal)
        btnFavorite.setImageResource(
            if (isFav) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        btnFavorite.setColorFilter(
            if (isFav) resources.getColor(R.color.accent, theme)
            else android.graphics.Color.WHITE
        )
    }

    private fun toggleFavorite(recipe: Recipe) {
        if (PreferencesManager.isFavorite(this, recipe.idMeal)) {
            PreferencesManager.removeFavorite(this, recipe.idMeal)
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
        } else {
            PreferencesManager.saveFavorite(this, recipe)
            Toast.makeText(this, "❤️ Saved to favorites!", Toast.LENGTH_SHORT).show()
        }
        updateFavoriteButton()
    }

    private fun showMealPlanDialog(recipe: Recipe) {
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        AlertDialog.Builder(this)
            .setTitle("Add to Meal Plan")
            .setItems(days) { _, index ->
                val plan = PreferencesManager.getMealPlan(this).toMutableList()
                plan[index] = plan[index].copy(
                    recipeName = recipe.strMeal,
                    recipeId = recipe.idMeal,
                    recipeThumb = recipe.strMealThumb
                )
                PreferencesManager.saveMealPlan(this, plan)
                Toast.makeText(this, "✅ Added to ${days[index]}!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
