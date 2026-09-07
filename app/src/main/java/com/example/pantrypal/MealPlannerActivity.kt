package com.example.pantrypal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MealPlannerActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PICK_RECIPE = 1001
    }

    private lateinit var btnBack: ImageButton
    private lateinit var btnSavePlan: TextView
    private lateinit var rvMealDays: RecyclerView

    private lateinit var mealDaysList: MutableList<MealPlanDay>
    private lateinit var adapter: MealDayAdapter
    private var pendingDayIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_planner)

        bindViews()
        loadMealPlan()
        setupRecyclerView()
        setupListeners()
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btn_back)
        btnSavePlan = findViewById(R.id.btn_save_plan)
        rvMealDays = findViewById(R.id.rv_meal_days)
    }

    private fun loadMealPlan() {
        // Retrieve plan and make it mutable so we can edit it in-memory before saving
        mealDaysList = PreferencesManager.getMealPlan(this).toMutableList()
    }

    private fun setupRecyclerView() {
        adapter = MealDayAdapter(
            context = this,
            list = mealDaysList,
            onAddClick = { position ->
                pendingDayIndex = position
                val intent = Intent(this, SearchActivity::class.java).apply {
                    putExtra(SearchActivity.EXTRA_PICK_MODE, true)
                }
                startActivityForResult(intent, REQUEST_PICK_RECIPE)
            },
            onClearClick = { position ->
                mealDaysList[position] = mealDaysList[position].copy(
                    recipeName = "",
                    recipeId = "",
                    recipeThumb = ""
                )
                adapter.notifyItemChanged(position)
            },
            onItemClick = { position ->
                val day = mealDaysList[position]
                if (day.recipeId.isNotEmpty()) {
                    val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
                        putExtra(RecipeDetailsActivity.EXTRA_MEAL_ID, day.recipeId)
                    }
                    startActivity(intent)
                }
            }
        )
        rvMealDays.layoutManager = LinearLayoutManager(this)
        rvMealDays.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        btnSavePlan.setOnClickListener {
            PreferencesManager.saveMealPlan(this, mealDaysList)
            Toast.makeText(this, "Meal plan saved successfully! 📅", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_RECIPE && resultCode == RESULT_OK && data != null) {
            val recipeName = data.getStringExtra(SearchActivity.RESULT_RECIPE_NAME) ?: ""
            val recipeId = data.getStringExtra(SearchActivity.RESULT_RECIPE_ID) ?: ""
            val recipeThumb = data.getStringExtra(SearchActivity.RESULT_RECIPE_THUMB) ?: ""

            if (pendingDayIndex in 0 until mealDaysList.size) {
                mealDaysList[pendingDayIndex] = mealDaysList[pendingDayIndex].copy(
                    recipeName = recipeName,
                    recipeId = recipeId,
                    recipeThumb = recipeThumb
                )
                adapter.notifyItemChanged(pendingDayIndex)
            }
            pendingDayIndex = -1
        }
    }
}

// ─── Meal Day Adapter ──────────────────────────────────────────────────────────
class MealDayAdapter(
    private val context: Context,
    private val list: List<MealPlanDay>,
    private val onAddClick: (Int) -> Unit,
    private val onClearClick: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MealDayAdapter.MealDayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_day, parent, false)
        return MealDayViewHolder(view, onAddClick, onClearClick, onItemClick)
    }

    override fun onBindViewHolder(holder: MealDayViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class MealDayViewHolder(
        itemView: View,
        private val onAddClick: (Int) -> Unit,
        private val onClearClick: (Int) -> Unit,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvDayAbbr: TextView = itemView.findViewById(R.id.tv_day_abbr)
        private val tvDayName: TextView = itemView.findViewById(R.id.tv_day_name)
        private val tvMealName: TextView = itemView.findViewById(R.id.tv_meal_name)
        private val btnAddMeal: TextView = itemView.findViewById(R.id.btn_add_meal)
        private val btnClearMeal: TextView = itemView.findViewById(R.id.btn_clear_meal)

        init {
            btnAddMeal.setOnClickListener {
                onAddClick(bindingAdapterPosition)
            }
            btnClearMeal.setOnClickListener {
                onClearClick(bindingAdapterPosition)
            }
            itemView.setOnClickListener {
                onItemClick(bindingAdapterPosition)
            }
        }

        fun bind(day: MealPlanDay) {
            tvDayAbbr.text = day.dayAbbr
            tvDayName.text = day.dayName

            if (day.recipeId.isEmpty()) {
                tvMealName.text = "Tap to add a recipe"
                tvMealName.setTextColor(itemView.resources.getColor(R.color.text_hint, itemView.context.theme))
                btnAddMeal.text = "+ Add"
                btnClearMeal.visibility = View.GONE
            } else {
                tvMealName.text = day.recipeName
                tvMealName.setTextColor(itemView.resources.getColor(R.color.text_primary, itemView.context.theme))
                btnAddMeal.text = "Change"
                btnClearMeal.visibility = View.VISIBLE
            }
        }
    }
}
