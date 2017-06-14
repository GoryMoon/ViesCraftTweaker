package se.gory_moon.vctweaker.jei.wrappers;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;

import java.util.Arrays;
import java.util.List;

public class ShapedRecipesWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {

	private final ShapedRecipes recipe;

	public ShapedRecipesWrapper(ShapedRecipes recipe) {
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		List<ItemStack> recipeItems = Arrays.asList(recipe.recipeItems);
		ItemStack recipeOutput = recipe.getRecipeOutput();

		ingredients.setInputs(ItemStack.class, recipeItems);
		ingredients.setOutput(ItemStack.class, recipeOutput);
	}

	@Override
	public int getWidth() {
		return recipe.recipeWidth;
	}

	@Override
	public int getHeight() {
		return recipe.recipeHeight;
	}
}
