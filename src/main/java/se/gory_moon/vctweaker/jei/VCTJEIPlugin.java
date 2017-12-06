package se.gory_moon.vctweaker.jei;

import com.viesis.viescraft.client.gui.GuiTileEntityAirshipWorkbench;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

@JEIPlugin
public class VCTJEIPlugin implements IModPlugin {

    //public static final String WORKBENCH_CRAFTING = "vc.workbench";

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {}

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        /*if (VCTweakerContainer.Configs.replaceJEI) {
            final IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
            registry.addRecipeCategories(new WorkbenchRecipeCategory(guiHelper));
        }*/
    }

    @Override
    public void register(IModRegistry registry) {
        //final IJeiHelpers jeiHelpers = registry.getJeiHelpers();

        //registry.addRecipeCatalyst(new ItemStack(InitBlocksVC.AIRSHIP_WORKBENCH), VanillaRecipeCategoryUid.CRAFTING);

        /*registry.handleRecipes(ShapedOreRecipe.class, recipe -> new ShapedOreRecipeWrapper(jeiHelpers, recipe), WORKBENCH_CRAFTING);
        registry.handleRecipes(ShapedRecipes.class, recipe -> new ShapedRecipesWrapper(jeiHelpers, recipe), WORKBENCH_CRAFTING);
        registry.handleRecipes(ShapelessOreRecipe.class, recipe -> new ShapelessOreRecipeWrapper(jeiHelpers, recipe), WORKBENCH_CRAFTING);
        registry.handleRecipes(ShapelessRecipes.class, recipe -> new ShapelessRecipesWrapper(jeiHelpers, recipe), WORKBENCH_CRAFTING);*/

        registry.addRecipeClickArea(GuiTileEntityAirshipWorkbench.class, 88, 32, 28, 23, VanillaRecipeCategoryUid.CRAFTING);
        //registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerAirshipWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {}
}
