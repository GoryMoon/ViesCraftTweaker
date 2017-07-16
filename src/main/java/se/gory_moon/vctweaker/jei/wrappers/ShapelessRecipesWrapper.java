package se.gory_moon.vctweaker.jei.wrappers;
/*
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import java.util.List;

public class ShapelessRecipesWrapper extends BlankRecipeWrapper implements IRecipeWrapper {

	private final IJeiHelpers jeiHelpers;
	private final ShapelessRecipes recipe;

	public ShapelessRecipesWrapper(IJeiHelpers jeiHelpers, ShapelessRecipes recipe) {
		this.jeiHelpers = jeiHelpers;
		this.recipe = recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ItemStack recipeOutput = recipe.getRecipeOutput();
		IStackHelper stackHelper = jeiHelpers.getStackHelper();

		List<List<ItemStack>> inputLists = stackHelper.expandRecipeItemStackInputs(recipe.getIngredients());
		ingredients.setInputLists(ItemStack.class, inputLists);
		ingredients.setOutput(ItemStack.class, recipeOutput);
	}
}
*/