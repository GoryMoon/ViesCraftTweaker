package se.gory_moon.vctweaker.jei.wrappers;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

public class ShapelessRecipesWrapper extends BlankRecipeWrapper implements IRecipeWrapper {

	private final ShapelessRecipes recipe;

	public ShapelessRecipesWrapper(ShapelessRecipes recipe) {
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ItemStack recipeOutput = recipe.getRecipeOutput();

		ingredients.setInputs(ItemStack.class, recipe.recipeItems);
		ingredients.setOutput(ItemStack.class, recipeOutput);
	}
}
