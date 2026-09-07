package com.example.pantrypal

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Lightweight async HTTP client using ExecutorService + Handler.
 * All requests run on a background thread pool; results are delivered
 * on the main thread via a Handler.
 */
object NetworkClient {

    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

    private const val TAG = "NetworkClient"
    private const val TIMEOUT_MS = 10_000

    // ─── TheMealDB endpoints ─────────────────────────────────────────────────
    private const val MEAL_DB_BASE = "https://www.themealdb.com/api/json/v1/1"

    /** Search recipes by name keyword */
    fun searchRecipes(query: String, onResult: (List<Recipe>) -> Unit, onError: (String) -> Unit) {
        val url = "$MEAL_DB_BASE/search.php?s=${encodeQuery(query)}"
        fetchJson(url,
            onSuccess = { json ->
                val recipes = parseRecipeList(json)
                mainHandler.post { onResult(recipes) }
            },
            onFailure = { err ->
                mainHandler.post { onError(err) }
            }
        )
    }

    /** Fetch full recipe details by meal ID */
    fun fetchRecipeById(id: String, onResult: (Recipe?) -> Unit, onError: (String) -> Unit) {
        val url = "$MEAL_DB_BASE/lookup.php?i=$id"
        fetchJson(url,
            onSuccess = { json ->
                val recipes = parseRecipeList(json)
                mainHandler.post { onResult(recipes.firstOrNull()) }
            },
            onFailure = { err ->
                mainHandler.post { onError(err) }
            }
        )
    }

    /** Filter recipes by category (e.g. "Chicken", "Beef", "Seafood") */
    fun fetchByCategory(category: String, onResult: (List<Recipe>) -> Unit, onError: (String) -> Unit) {
        val url = "$MEAL_DB_BASE/filter.php?c=${encodeQuery(category)}"
        fetchJson(url,
            onSuccess = { json ->
                val recipes = parseMealListFromFilter(json)
                mainHandler.post { onResult(recipes) }
            },
            onFailure = { err ->
                mainHandler.post { onError(err) }
            }
        )
    }

    /** Fetch a random recipe */
    fun fetchRandom(onResult: (Recipe?) -> Unit, onError: (String) -> Unit) {
        val url = "$MEAL_DB_BASE/random.php"
        fetchJson(url,
            onSuccess = { json ->
                val recipes = parseRecipeList(json)
                mainHandler.post { onResult(recipes.firstOrNull()) }
            },
            onFailure = { err ->
                mainHandler.post { onError(err) }
            }
        )
    }

    /** Fetch current weather for the given coordinates */
    fun fetchWeather(
        latitude: Double,
        longitude: Double,
        onResult: (WeatherData) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current_weather=true"
        fetchJson(url,
            onSuccess = { json ->
                try {
                    val cw = JSONObject(json).getJSONObject("current_weather")
                    val weather = WeatherData(
                        temperature = cw.getDouble("temperature"),
                        weatherCode = cw.getInt("weathercode"),
                        isDay = cw.getInt("is_day")
                    )
                    mainHandler.post { onResult(weather) }
                } catch (e: Exception) {
                    mainHandler.post { onError("Failed to parse weather: ${e.message}") }
                }
            },
            onFailure = { err ->
                mainHandler.post { onError(err) }
            }
        )
    }

    // ─── Core HTTP fetcher ───────────────────────────────────────────────────
    private fun fetchJson(
        urlString: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "PantryPal-App/1.0")
                }
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    onSuccess(response.toString())
                } else {
                    onFailure("HTTP Error $responseCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                onFailure("Network error: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }
    }

    // ─── JSON Parsers ────────────────────────────────────────────────────────
    private fun parseRecipeList(json: String): List<Recipe> {
        return try {
            val root = JSONObject(json)
            val mealsArray = root.optJSONArray("meals") ?: return emptyList()
            val list = mutableListOf<Recipe>()
            for (i in 0 until mealsArray.length()) {
                val obj = mealsArray.getJSONObject(i)
                val recipe = Recipe(
                    idMeal = obj.optString("idMeal"),
                    strMeal = obj.optString("strMeal"),
                    strCategory = obj.optString("strCategory"),
                    strArea = obj.optString("strArea"),
                    strInstructions = obj.optString("strInstructions"),
                    strMealThumb = obj.optString("strMealThumb"),
                    strYoutube = obj.optString("strYoutube"),
                    strIngredient1 = obj.optString("strIngredient1").takeIf { it.isNotBlank() },
                    strIngredient2 = obj.optString("strIngredient2").takeIf { it.isNotBlank() },
                    strIngredient3 = obj.optString("strIngredient3").takeIf { it.isNotBlank() },
                    strIngredient4 = obj.optString("strIngredient4").takeIf { it.isNotBlank() },
                    strIngredient5 = obj.optString("strIngredient5").takeIf { it.isNotBlank() },
                    strIngredient6 = obj.optString("strIngredient6").takeIf { it.isNotBlank() },
                    strIngredient7 = obj.optString("strIngredient7").takeIf { it.isNotBlank() },
                    strIngredient8 = obj.optString("strIngredient8").takeIf { it.isNotBlank() },
                    strIngredient9 = obj.optString("strIngredient9").takeIf { it.isNotBlank() },
                    strIngredient10 = obj.optString("strIngredient10").takeIf { it.isNotBlank() },
                    strIngredient11 = obj.optString("strIngredient11").takeIf { it.isNotBlank() },
                    strIngredient12 = obj.optString("strIngredient12").takeIf { it.isNotBlank() },
                    strIngredient13 = obj.optString("strIngredient13").takeIf { it.isNotBlank() },
                    strIngredient14 = obj.optString("strIngredient14").takeIf { it.isNotBlank() },
                    strIngredient15 = obj.optString("strIngredient15").takeIf { it.isNotBlank() },
                    strIngredient16 = obj.optString("strIngredient16").takeIf { it.isNotBlank() },
                    strIngredient17 = obj.optString("strIngredient17").takeIf { it.isNotBlank() },
                    strIngredient18 = obj.optString("strIngredient18").takeIf { it.isNotBlank() },
                    strIngredient19 = obj.optString("strIngredient19").takeIf { it.isNotBlank() },
                    strIngredient20 = obj.optString("strIngredient20").takeIf { it.isNotBlank() },
                    strMeasure1 = obj.optString("strMeasure1"),
                    strMeasure2 = obj.optString("strMeasure2"),
                    strMeasure3 = obj.optString("strMeasure3"),
                    strMeasure4 = obj.optString("strMeasure4"),
                    strMeasure5 = obj.optString("strMeasure5"),
                    strMeasure6 = obj.optString("strMeasure6"),
                    strMeasure7 = obj.optString("strMeasure7"),
                    strMeasure8 = obj.optString("strMeasure8"),
                    strMeasure9 = obj.optString("strMeasure9"),
                    strMeasure10 = obj.optString("strMeasure10"),
                    strMeasure11 = obj.optString("strMeasure11"),
                    strMeasure12 = obj.optString("strMeasure12"),
                    strMeasure13 = obj.optString("strMeasure13"),
                    strMeasure14 = obj.optString("strMeasure14"),
                    strMeasure15 = obj.optString("strMeasure15"),
                    strMeasure16 = obj.optString("strMeasure16"),
                    strMeasure17 = obj.optString("strMeasure17"),
                    strMeasure18 = obj.optString("strMeasure18"),
                    strMeasure19 = obj.optString("strMeasure19"),
                    strMeasure20 = obj.optString("strMeasure20")
                )
                list.add(recipe)
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}", e)
            emptyList()
        }
    }

    /** The filter endpoint returns a simplified object (no ingredients/instructions) */
    private fun parseMealListFromFilter(json: String): List<Recipe> {
        return try {
            val root = JSONObject(json)
            val mealsArray = root.optJSONArray("meals") ?: return emptyList()
            val list = mutableListOf<Recipe>()
            for (i in 0 until mealsArray.length()) {
                val obj = mealsArray.getJSONObject(i)
                list.add(
                    Recipe(
                        idMeal = obj.optString("idMeal"),
                        strMeal = obj.optString("strMeal"),
                        strMealThumb = obj.optString("strMealThumb")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Parse filter error: ${e.message}", e)
            emptyList()
        }
    }

    private fun encodeQuery(q: String): String = java.net.URLEncoder.encode(q, "UTF-8")
}
