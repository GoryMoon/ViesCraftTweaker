package se.gorymoon.vctweaker.jei;


import com.viesis.viescraft.client.gui.GuiTileEntityAirshipWorkbench;
import com.viesis.viescraft.common.items.crafting.CraftingManagerVC;
import com.viesis.viescraft.common.tileentity.ContainerAirshipWorkbench;
import com.viesis.viescraft.init.InitBlocksVC;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import se.gorymoon.vctweaker.jei.wrappers.ShapedOreRecipeWrapper;
import se.gorymoon.vctweaker.jei.wrappers.ShapedRecipesWrapper;
import se.gorymoon.vctweaker.jei.wrappers.ShapelessOreRecipeWrapper;
import se.gorymoon.vctweaker.jei.wrappers.ShapelessRecipesWrapper;
import se.gorymoon.vctweaker.util.Log;

public class JEIPlugin {

    public static final String WORKBENCH_CRAFTING = "vc.workbench";

    public static void register(IModRegistry registry) {
        final IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        final IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registry.addRecipeCategories(new WorkbenchRecipeCategory(guiHelper));

        registry.addRecipes(CraftingManagerVC.getInstance().getRecipeList(), WORKBENCH_CRAFTING);
        registry.addRecipeCategoryCraftingItem(new ItemStack(InitBlocksVC.airship_workbench), WORKBENCH_CRAFTING);

        registry.handleRecipes(ShapedOreRecipe.class, recipe -> new ShapedOreRecipeWrapper(jeiHelpers, recipe), WORKBENCH_CRAFTING);
        registry.handleRecipes(ShapedRecipes.class, ShapedRecipesWrapper::new, WORKBENCH_CRAFTING);
        registry.handleRecipes(ShapelessOreRecipe.class, recipe -> new ShapelessOreRecipeWrapper(jeiHelpers, recipe), WORKBENCH_CRAFTING);
        registry.handleRecipes(ShapelessRecipes.class, ShapelessRecipesWrapper::new, WORKBENCH_CRAFTING);

        registry.addRecipeClickArea(GuiTileEntityAirshipWorkbench.class, 88, 32, 28, 23, WORKBENCH_CRAFTING);

        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerAirshipWorkbench.class, WORKBENCH_CRAFTING, 1, 9, 10, 36);

        Log.info("Replaced ViesCraft JEI implementation");
    }

}
