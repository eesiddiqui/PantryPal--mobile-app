package com.example.pantrypal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvSearchHint: TextView
    private lateinit var tvWeatherTemp: TextView
    private lateinit var tvWeatherDesc: TextView
    private lateinit var tvWeatherIcon: TextView
    private lateinit var tvWeatherRecommendation: TextView
    private lateinit var tvWeatherCategory: TextView
    private lateinit var rvWeatherRecipes: RecyclerView
    private lateinit var pbWeatherLoading: ProgressBar

    private lateinit var btnGoSearch: LinearLayout
    private lateinit var btnGoFavorites: LinearLayout
    private lateinit var btnGoNutrition: LinearLayout
    private lateinit var btnGoMealPlanner: LinearLayout

    private val LOCATION_PERMISSION_REQUEST = 100

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val recipeAdapter = RecipeAdapter { recipe ->
        openRecipeDetails(recipe.idMeal)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bindViews()
        setupRecyclerView()
        setupNavigationButtons()
        setGreeting()

        checkLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        setGreeting()
    }

    private fun bindViews() {
        tvGreeting = findViewById(R.id.tv_greeting)
        tvSearchHint = findViewById(R.id.tv_search_hint)
        tvWeatherTemp = findViewById(R.id.tv_weather_temp)
        tvWeatherDesc = findViewById(R.id.tv_weather_desc)
        tvWeatherIcon = findViewById(R.id.tv_weather_icon)
        tvWeatherRecommendation = findViewById(R.id.tv_weather_recommendation)
        tvWeatherCategory = findViewById(R.id.tv_weather_category)
        rvWeatherRecipes = findViewById(R.id.rv_weather_recipes)
        pbWeatherLoading = findViewById(R.id.pb_weather_loading)

        btnGoSearch = findViewById(R.id.btn_go_search)
        btnGoFavorites = findViewById(R.id.btn_go_favorites)
        btnGoNutrition = findViewById(R.id.btn_go_nutrition)
        btnGoMealPlanner = findViewById(R.id.btn_go_meal_planner)
    }

    private fun setupRecyclerView() {
        rvWeatherRecipes.layoutManager = LinearLayoutManager(this)
        rvWeatherRecipes.adapter = recipeAdapter
        rvWeatherRecipes.isNestedScrollingEnabled = false
    }

    private fun setupNavigationButtons() {
        tvSearchHint.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        btnGoSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        btnGoFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        btnGoNutrition.setOnClickListener {
            startActivity(Intent(this, NutritionActivity::class.java))
        }

        btnGoMealPlanner.setOnClickListener {
            startActivity(Intent(this, MealPlannerActivity::class.java))
        }
    }

    private fun setGreeting() {

        val hour =
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        tvGreeting.text =
            when {
                hour < 12 -> "Good Morning! "
                hour < 17 -> "Good Afternoon! "
                else -> "Good Evening! "
            }
    }

    private fun checkLocationPermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            getCurrentLocation()

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun getCurrentLocation() {

        try {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {

                        loadWeatherAndRecommendations(
                            location.latitude,
                            location.longitude
                        )

                    } else {

                        loadWeatherAndRecommendations(
                            37.2936,
                            126.9749
                        )
                    }
                }

        } catch (e: SecurityException) {

            loadWeatherAndRecommendations(
                37.2936,
                126.9749
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            getCurrentLocation()

        } else {

            loadWeatherAndRecommendations(
                37.2936,
                126.9749
            )
        }
    }

    private fun loadWeatherAndRecommendations(
        latitude: Double,
        longitude: Double
    ) {

        pbWeatherLoading.visibility = View.VISIBLE
        rvWeatherRecipes.visibility = View.GONE

        NetworkClient.fetchWeather(
            latitude,
            longitude,
            onResult = { weather ->

                updateWeatherUI(weather)

                val (category, hint) =
                    weather.getRecommendedCategory()

                tvWeatherCategory.text = hint

                loadRecommendedRecipes(category)
            },
            onError = {

                tvWeatherDesc.text =
                    "Unable to load weather"

                tvWeatherRecommendation.text =
                    "Showing popular recipes instead"

                loadRecommendedRecipes("Chicken")
            }
        )
    }

    private fun updateWeatherUI(
        weather: WeatherData
    ) {

        tvWeatherTemp.text =
            "${weather.temperature.toInt()}°C"

        tvWeatherDesc.text =
            weather.getDescription()

        tvWeatherIcon.text =
            weather.getWeatherEmoji()

        val (_, hint) =
            weather.getRecommendedCategory()

        tvWeatherRecommendation.text =
            hint
    }

    private fun loadRecommendedRecipes(
        category: String
    ) {

        NetworkClient.fetchByCategory(
            category,
            onResult = { recipes ->

                pbWeatherLoading.visibility =
                    View.GONE

                rvWeatherRecipes.visibility =
                    View.VISIBLE

                recipeAdapter.submitList(
                    recipes.take(5)
                )
            },
            onError = {

                pbWeatherLoading.visibility =
                    View.GONE
            }
        )
    }

    private fun openRecipeDetails(
        mealId: String
    ) {

        val intent =
            Intent(
                this,
                RecipeDetailsActivity::class.java
            )

        intent.putExtra(
            RecipeDetailsActivity.EXTRA_MEAL_ID,
            mealId
        )

        startActivity(intent)
    }
}