package com.example.pantrypal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class
FavoritesActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCount: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnBrowse: TextView
    private lateinit var rvFavorites: RecyclerView

    private val adapter = RecipeAdapter { recipe ->
        val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
            putExtra(RecipeDetailsActivity.EXTRA_MEAL_ID, recipe.idMeal)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        bindViews()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btn_back)
        tvCount = findViewById(R.id.tv_favorites_count)
        layoutEmpty = findViewById(R.id.layout_empty_favorites)
        btnBrowse = findViewById(R.id.btn_go_search)
        rvFavorites = findViewById(R.id.rv_favorites)
    }

    private fun setupRecyclerView() {
        rvFavorites.layoutManager = LinearLayoutManager(this)
        rvFavorites.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        btnBrowse.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish() // Optional: close favorites so when they go back they don't see empty state again if they favorited something
        }
    }

    private fun loadFavorites() {
        val favorites = PreferencesManager.getFavorites(this)
        
        if (favorites.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
            tvCount.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvFavorites.visibility = View.VISIBLE
            tvCount.visibility = View.VISIBLE
            tvCount.text = "${favorites.size} favorite recipe${if (favorites.size == 1) "" else "s"} saved"
            adapter.submitList(favorites)
        }
    }
}
