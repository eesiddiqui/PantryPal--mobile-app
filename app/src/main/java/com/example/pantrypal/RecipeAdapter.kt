package com.example.pantrypal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class RecipeAdapter(
    private val onItemClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecipeViewHolder(
        itemView: View,
        private val onItemClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivRecipeThumb: ImageView = itemView.findViewById(R.id.iv_recipe_thumb)
        private val tvItemCategory: TextView = itemView.findViewById(R.id.tv_item_category)
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        private val tvItemArea: TextView = itemView.findViewById(R.id.tv_item_area)
        private var currentRecipe: Recipe? = null

        init {
            itemView.setOnClickListener {
                currentRecipe?.let(onItemClick)
            }
        }

        fun bind(recipe: Recipe) {
            currentRecipe = recipe
            tvItemTitle.text = recipe.strMeal
            
            if (recipe.strCategory.isNullOrBlank()) {
                tvItemCategory.visibility = View.GONE
            } else {
                tvItemCategory.visibility = View.VISIBLE
                tvItemCategory.text = recipe.strCategory
            }

            if (recipe.strArea.isNullOrBlank()) {
                tvItemArea.text = "🌍 International"
            } else {
                tvItemArea.text = "🌍 ${recipe.strArea}"
            }

            Glide.with(itemView.context)
                .load(recipe.strMealThumb)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(ivRecipeThumb)
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.idMeal == newItem.idMeal
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}
