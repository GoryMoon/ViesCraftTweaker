package se.gory_moon.vctweaker.jei;

import com.viesis.viescraft.client.gui.GuiTileEntityAirshipWorkbench;
import com.viesis.viescraft.common.items.crafting.CraftingManagerVC;
import com.viesis.viescraft.common.tileentity.ContainerAirshipWorkbench;
import com.viesis.viescraft.init.InitBlocksVC;
import mezz.jei.api.*;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import se.gory_moon.vctweaker.VCTweaker;
import se.gory_moon.vctweaker.jei.wrappers.ShapedOreRecipeWrapper;
import se.gory_moon.vctweaker.jei.wrappers.ShapedRecipesWrapper;
import se.gory_moon.vctweaker.jei.wrappers.ShapelessOreRecipeWrapper;
import se.gory_moon.vctweaker.jei.wrappers.ShapelessRecipesWrapper;
import se.gory_moon.vctweaker.util.Log;

@JEIPlugin
public class VCTJEIPlugin implements IModPlugin {

    public static final String WORKBENCH_CRAFTING = "vc.workbench";

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {}

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (VCTweaker.Configs.replaceJEI) {
            final IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
            registry.addRecipeCategories(new WorkbenchRecipeCategory(guiHelper));
        }
    }

    @Override
    public void register(IModRegistry registry) {
        if (VCTweaker.Configs.replaceJEI) {
            final IJeiHelpers jeiHelpers = registry.getJeiHelpers();

            registry.addRecipes(CraftingManagerVC.getInstance().getRecipeList(), WORKBENCH_CRAFTING);
            registry.addRecipeCatalyst(new ItemStack(InitBlocksVC.airship_workbench), WORKBENCH_CRAFTING);

            registry.handleRecipes(ShapedOreRecipe.class, recipe -> new ShapedOreRecipeWrapper(jeiHelpers, recipe), WORKBENCH_CRAFTING);
            registry.handleRecipes(ShapedRecipes.class, ShapedRecipesWrapper::new, WORKBENCH_CRAFTING);
            registry.handleRecipes(ShapelessOreRecipe.class, recipe -> new ShapelessOreRecipeWrapper(jeiHelpers, recipe), WORKBENCH_CRAFTING);
            registry.handleRecipes(ShapelessRecipes.class, ShapelessRecipesWrapper::new, WORKBENCH_CRAFTING);

            registry.addRecipeClickArea(GuiTileEntityAirshipWorkbench.class, 88, 32, 28, 23, WORKBENCH_CRAFTING);
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerAirshipWorkbench.class, WORKBENCH_CRAFTING, 1, 9, 10, 36);

            Log.info("Replaced ViesCraft JEI implementation");
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {}
}
