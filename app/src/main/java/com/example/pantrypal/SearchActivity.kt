package com.example.pantrypal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * SearchActivity – allows users to search TheMealDB by name keyword.
 * Displays results in a RecyclerView with RecipeAdapter.
 * Supports "pick mode" for MealPlannerActivity (returns selected recipe).
 */
class SearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PICK_MODE = "pick_mode"
        const val RESULT_RECIPE_NAME = "result_recipe_name"
        const val RESULT_RECIPE_ID = "result_recipe_id"
        const val RESULT_RECIPE_THUMB = "result_recipe_thumb"
    }

    private lateinit var btnBack: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: TextView
    private lateinit var tvResultsCount: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var layoutNoResults: LinearLayout
    private lateinit var rvResults: RecyclerView

    private var isPickMode = false
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        isPickMode = intent.getBooleanExtra(EXTRA_PICK_MODE, false)
        bindViews()
        setupAdapter()
        setupListeners()

        // Show keyboard automatically
        etSearch.requestFocus()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btn_back)
        etSearch = findViewById(R.id.et_search)
        btnSearch = findViewById(R.id.btn_search)
        tvResultsCount = findViewById(R.id.tv_results_count)
        pbLoading = findViewById(R.id.pb_search_loading)
        layoutNoResults = findViewById(R.id.layout_no_results)
        rvResults = findViewById(R.id.rv_search_results)
    }

    private fun setupAdapter() {
        adapter = RecipeAdapter { recipe ->
            if (isPickMode) {
                // Return selected recipe back to MealPlannerActivity
                val data = Intent().apply {
                    putExtra(RESULT_RECIPE_NAME, recipe.strMeal)
                    putExtra(RESULT_RECIPE_ID, recipe.idMeal)
                    putExtra(RESULT_RECIPE_THUMB, recipe.strMealThumb)
                }
                setResult(RESULT_OK, data)
                finish()
            } else {
                // Navigate to recipe details
                val intent = Intent(this, RecipeDetailsActivity::class.java)
                intent.putExtra(RecipeDetailsActivity.EXTRA_MEAL_ID, recipe.idMeal)
                startActivity(intent)
            }
        }
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnSearch.setOnClickListener { performSearch() }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }
    }

    private fun performSearch() {
        val query = etSearch.text.toString().trim()
        if (query.isEmpty()) return

        // Hide keyboard
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)

        // Show loading
        pbLoading.visibility = View.VISIBLE
        layoutNoResults.visibility = View.GONE
        tvResultsCount.visibility = View.GONE
        adapter.submitList(emptyList())

        NetworkClient.searchRecipes(
            query = query,
            onResult = { recipes ->
                pbLoading.visibility = View.GONE
                if (recipes.isEmpty()) {
                    layoutNoResults.visibility = View.VISIBLE
                    tvResultsCount.visibility = View.GONE
                } else {
                    layoutNoResults.visibility = View.GONE
                    tvResultsCount.visibility = View.VISIBLE
                    tvResultsCount.text = "${recipes.size} recipe${if (recipes.size == 1) "" else "s"} found"
                    adapter.submitList(recipes)
                }
            },
            onError = {
                pbLoading.visibility = View.GONE
                layoutNoResults.visibility = View.VISIBLE
            }
        )
    }
}
