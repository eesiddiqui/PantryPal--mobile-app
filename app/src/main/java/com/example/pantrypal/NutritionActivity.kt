package com.example.pantrypal

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NutritionActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etCalories: EditText
    private lateinit var etProtein: EditText
    private lateinit var etCarbs: EditText
    private lateinit var etFat: EditText
    private lateinit var btnSaveGoals: TextView
    private lateinit var tvGoalType: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var rvRecommended: RecyclerView

    private val adapter = RecipeAdapter { recipe ->
        val intent = android.content.Intent(this, RecipeDetailsActivity::class.java).apply {
            putExtra(RecipeDetailsActivity.EXTRA_MEAL_ID, recipe.idMeal)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition)

        bindViews()
        setupRecyclerView()
        setupListeners()
        loadSavedGoals()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btn_back)
        etCalories = findViewById(R.id.et_calories)
        etProtein = findViewById(R.id.et_protein)
        etCarbs = findViewById(R.id.et_carbs)
        etFat = findViewById(R.id.et_fat)
        btnSaveGoals = findViewById(R.id.btn_save_goals)
        tvGoalType = findViewById(R.id.tv_goal_type)
        pbLoading = findViewById(R.id.pb_nutrition_loading)
        rvRecommended = findViewById(R.id.rv_recommended)
    }

    private fun setupRecyclerView() {
        rvRecommended.layoutManager = LinearLayoutManager(this)
        rvRecommended.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        btnSaveGoals.setOnClickListener {
            saveGoals()
        }
    }

    private fun loadSavedGoals() {
        val goal = PreferencesManager.getNutritionGoal(this)
        etCalories.setText(goal.calories.toString())
        etProtein.setText(goal.protein.toString())
        etCarbs.setText(goal.carbs.toString())
        etFat.setText(goal.fat.toString())

        updateRecommendations(goal)
    }

    private fun saveGoals() {
        val calStr = etCalories.text.toString().trim()
        val proStr = etProtein.text.toString().trim()
        val carbStr = etCarbs.text.toString().trim()
        val fatStr = etFat.text.toString().trim()

        if (calStr.isEmpty() || proStr.isEmpty() || carbStr.isEmpty() || fatStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all goals", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = calStr.toIntOrNull() ?: 2000
        val pro = proStr.toIntOrNull() ?: 150
        val carb = carbStr.toIntOrNull() ?: 250
        val fat = fatStr.toIntOrNull() ?: 65

        val newGoal = NutritionGoal(
            calories = cal,
            protein = pro,
            carbs = carb,
            fat = fat
        )

        PreferencesManager.saveNutritionGoal(this, newGoal)
        Toast.makeText(this, "Goals saved successfully! 🥗", Toast.LENGTH_SHORT).show()

        updateRecommendations(newGoal)
    }

    private fun updateRecommendations(goal: NutritionGoal) {
        // Determine category and label based on macro ratios
        // 1g protein = 4 cal, 1g carb = 4 cal, 1g fat = 9 cal
        val proteinCal = goal.protein * 4
        val carbCal = goal.carbs * 4
        val fatCal = goal.fat * 9
        val totalCal = (proteinCal + carbCal + fatCal).toDouble().coerceAtLeast(1.0)

        val proteinRatio = proteinCal / totalCal
        val carbRatio = carbCal / totalCal
        val fatRatio = fatCal / totalCal

        val (category, typeLabel) = when {
            // High Protein if protein is > 30% of total calories or > 140g
            proteinRatio > 0.30 || goal.protein > 140 -> Pair("Chicken", "💪 High Protein")
            // Low Carb if carbs is < 20% or < 80g
            carbRatio < 0.25 || goal.carbs < 80 -> Pair("Seafood", "🥑 Low Carb")
            // Keto if fat is > 50% or fat > 100g
            fatRatio > 0.45 || goal.fat > 100 -> Pair("Beef", "🥩 Keto-Friendly")
            // Otherwise Balanced
            else -> Pair("Vegetarian", "🥗 Balanced")
        }

        tvGoalType.text = typeLabel

        // Fetch matching recipes
        pbLoading.visibility = View.VISIBLE
        rvRecommended.visibility = View.GONE

        NetworkClient.fetchByCategory(category,
            onResult = { recipes ->
                pbLoading.visibility = View.GONE
                rvRecommended.visibility = View.VISIBLE
                // Show top 6 matches
                adapter.submitList(recipes.take(6))
            },
            onError = { err ->
                pbLoading.visibility = View.GONE
                Toast.makeText(this, "Failed to load recommendations: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
