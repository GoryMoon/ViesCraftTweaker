package se.gorymoon.vctweaker.tweaker;

import com.viesis.viescraft.common.items.crafting.CraftingManagerVC;
import minetweaker.IUndoableAction;
import minetweaker.api.item.IIngredient;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import minetweaker.api.recipes.*;
import minetweaker.mc1112.recipes.MCRecipeManager;
import minetweaker.mc1112.recipes.RecipeConverter;
import minetweaker.mc1112.recipes.ShapedRecipeBasic;
import minetweaker.mc1112.util.MineTweakerHacks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import java.util.List;
import java.util.ArrayList;

import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import static minetweaker.api.minecraft.MineTweakerMC.*;

@ZenClass("mods.VCTweaker")
public class TweakerIntegration {

    private static boolean matches(Object input, IIngredient ingredient) {
        if((input == null) != (ingredient == null)) {
            return false;
        } else if(ingredient != null) {
            if(input instanceof ItemStack) {
                if(((ItemStack)input).isEmpty() != (ingredient == null)) {
                    return false;
                }
                if(!ingredient.matches(getIItemStack((ItemStack) input))) {
                    return false;
                }
            } else if(input instanceof String) {
                if(!ingredient.contains(getOreDict((String) input))) {
                    return false;
                }
            }
        }

        return true;
    }

    @ZenGetter("all")
    public List<ICraftingRecipe> getRecipesFor(IIngredient ingredient) {
        List<ICraftingRecipe> results = new ArrayList<>();
        List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();

        recipes.stream().filter(recipe -> ingredient.matches(MineTweakerMC.getIItemStack(recipe.getRecipeOutput()))).forEach(recipe -> {
            ICraftingRecipe converted = RecipeConverter.toCraftingRecipe(recipe);
            results.add(converted);
        });

        return results;
    }

    @ZenMethod
    public List<ICraftingRecipe> getAll() {
        List<ICraftingRecipe> results = new ArrayList<>();
        List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();

        for(IRecipe recipe : recipes) {
            ICraftingRecipe converted = RecipeConverter.toCraftingRecipe(recipe);
            results.add(converted);
        }

        return results;
    }

    @ZenMethod
    public static int remove(IIngredient output, @Optional boolean nbtMatch) {
        List<IRecipe> toRemove = new ArrayList<>();
        List<Integer> removeIndex = new ArrayList<>();
        List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();

        for(int i = 0; i < recipes.size(); i++) {
            IRecipe recipe = recipes.get(i);

            // certain special wrappers have no predefined output. ignore those
            // since these cannot be removed with MineTweaker scripts
            if(!recipe.getRecipeOutput().isEmpty()) {
                if(nbtMatch ? output.matchesExact(getIItemStack(recipe.getRecipeOutput())) : output.matches(getIItemStack(recipe.getRecipeOutput()))) {
                    toRemove.add(recipe);
                    removeIndex.add(i);
                }
            }
        }

        MineTweakerAPI.apply(new ActionRemoveRecipes(toRemove, removeIndex));
        return toRemove.size();
    }

    @ZenMethod
    public static void addShaped(IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        addShaped(output, ingredients, function, action, false);
    }

    @ZenMethod
    public static void addShapedMirrored(IItemStack output, IIngredient[][] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        addShaped(output, ingredients, function, action, true);
    }

    @ZenMethod
    public static void addShapeless(IItemStack output, IIngredient[] ingredients, @Optional IRecipeFunction function, @Optional IRecipeAction action) {
        ShapelessRecipe recipe = new ShapelessRecipe(output, ingredients, function, action);
        IRecipe irecipe = RecipeConverter.convert(recipe);
        MineTweakerAPI.apply(new ActionAddRecipe(irecipe, recipe));
    }

    @ZenMethod
    public static int removeShaped(IIngredient output, IIngredient[][] ingredients) {
        int ingredientsWidth = 0;
        int ingredientsHeight = 0;

        if(ingredients != null) {
            ingredientsHeight = ingredients.length;

            for(int i = 0; i < ingredients.length; i++) {
                ingredientsWidth = Math.max(ingredientsWidth, ingredients[i].length);
            }
        }

        List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();
        List<IRecipe> toRemove = new ArrayList<IRecipe>();
        List<Integer> removeIndex = new ArrayList<Integer>();
        outer:
        for(int i = 0; i < recipes.size(); i++) {
            IRecipe recipe = recipes.get(i);

            if(recipe.getRecipeOutput().isEmpty() || !output.matches(getIItemStack(recipe.getRecipeOutput()))) {
                continue;
            }

            if(ingredients != null) {
                if(recipe instanceof ShapedRecipes) {
                    ShapedRecipes srecipe = (ShapedRecipes) recipe;
                    if(ingredientsWidth != srecipe.recipeWidth || ingredientsHeight != srecipe.recipeHeight) {
                        continue;
                    }

                    for(int j = 0; j < ingredientsHeight; j++) {
                        IIngredient[] row = ingredients[j];
                        for(int k = 0; k < ingredientsWidth; k++) {
                            IIngredient ingredient = k > row.length ? null : row[k];
                            ItemStack recipeIngredient = srecipe.recipeItems[j * srecipe.recipeWidth + k];

                            if(!matches(recipeIngredient, ingredient)) {
                                continue outer;
                            }
                        }
                    }
                } else if(recipe instanceof ShapedOreRecipe) {
                    ShapedOreRecipe srecipe = (ShapedOreRecipe) recipe;
                    int recipeWidth = MineTweakerHacks.getShapedOreRecipeWidth(srecipe);
                    int recipeHeight = srecipe.getRecipeSize() / recipeWidth;
                    if(ingredientsWidth != recipeWidth || ingredientsHeight != recipeHeight) {
                        continue;
                    }

                    for(int j = 0; j < ingredientsHeight; j++) {
                        IIngredient[] row = ingredients[j];
                        for(int k = 0; k < ingredientsWidth; k++) {
                            IIngredient ingredient = k > row.length ? null : row[k];
                            Object input = srecipe.getInput()[j * recipeWidth + k];
                            if(!matches(input, ingredient)) {
                                continue outer;
                            }
                        }
                    }
                } else {
                    if(recipe instanceof ShapelessRecipes) {
                        continue;
                    } else if(recipe instanceof ShapelessOreRecipe) {
                        continue;
                    } else {
                    }
                }
            } else {
                if(recipe instanceof ShapelessRecipes) {
                    continue;
                } else if(recipe instanceof ShapelessOreRecipe) {
                    continue;
                } else {
                }
            }

            toRemove.add(recipe);
            removeIndex.add(i);
        }

        MineTweakerAPI.apply(new ActionRemoveRecipes(toRemove, removeIndex));
        return toRemove.size();
    }

    @ZenMethod
    public static int removeShapeless(IIngredient output, IIngredient[] ingredients, boolean wildcard) {
        List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();
        List<IRecipe> toRemove = new ArrayList<IRecipe>();
        List<Integer> removeIndex = new ArrayList<Integer>();
        outer:
        for(int i = 0; i < recipes.size(); i++) {
            IRecipe recipe = recipes.get(i);

            if(recipe.getRecipeOutput().isEmpty() || !output.matches(getIItemStack(recipe.getRecipeOutput()))) {
                continue;
            }

            if(ingredients != null) {
                if(recipe instanceof ShapelessRecipes) {
                    ShapelessRecipes srecipe = (ShapelessRecipes) recipe;

                    if(ingredients.length > srecipe.getRecipeSize()) {
                        continue;
                    } else if(!wildcard && ingredients.length < srecipe.getRecipeSize()) {
                        continue;
                    }

                    checkIngredient:
                    for(int j = 0; j < ingredients.length; j++) {
                        for(int k = 0; k < srecipe.getRecipeSize(); k++) {
                            if(matches(srecipe.recipeItems.get(k), ingredients[j])) {
                                continue checkIngredient;
                            }
                        }

                        continue outer;
                    }
                } else if(recipe instanceof ShapelessOreRecipe) {
                    ShapelessOreRecipe srecipe = (ShapelessOreRecipe) recipe;
                    NonNullList<Object> inputs = srecipe.getInput();

                    if(inputs.size() < ingredients.length) {
                        continue;
                    }
                    if(!wildcard && inputs.size() > ingredients.length) {
                        continue;
                    }

                    checkIngredient:
                    for(int j = 0; j < ingredients.length; j++) {
                        for(int k = 0; k < srecipe.getRecipeSize(); k++) {
                            if(matches(inputs.get(k), ingredients[j])) {
                                continue checkIngredient;
                            }
                        }

                        continue outer;
                    }
                }
                if(recipe instanceof ShapedRecipes) {
                    continue;
                } else if(recipe instanceof ShapedOreRecipe) {
                    continue;
                } else {
                }
            } else {
                if(recipe instanceof ShapedRecipes) {
                    continue;
                } else if(recipe instanceof ShapedOreRecipe) {
                    continue;
                } else {

                }
            }
            toRemove.add(recipe);
            removeIndex.add(i);
        }

        MineTweakerAPI.apply(new ActionRemoveRecipes(toRemove, removeIndex));
        return toRemove.size();
    }

    @ZenMethod
    public static IItemStack craft(IItemStack[][] contents) {
        Container container = new ContainerVirtual();

        int width = 0;
        int height = contents.length;
        for(IItemStack[] row : contents) {
            width = Math.max(width, row.length);
        }

        ItemStack[] iContents = new ItemStack[width * height];
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < contents[i].length; j++) {
                if(contents[i][j] != null) {
                    iContents[i * width + j] = getItemStack(contents[i][j]);
                }
            }
        }

        InventoryCrafting inventory = new InventoryCrafting(container, width, height);
        for(int i = 0; i < iContents.length; i++) {
            inventory.setInventorySlotContents(i, iContents[i]);
        }
        ItemStack result = CraftingManager.getInstance().findMatchingRecipe(inventory, null);
        if(result.isEmpty()) {
            return null;
        } else {
            return getIItemStack(result);
        }
    }

    private static void addShaped(IItemStack output, IIngredient[][] ingredients, IRecipeFunction function, IRecipeAction action, boolean mirrored) {
        ShapedRecipe recipe = new ShapedRecipe(output, ingredients, function, action, mirrored);
        IRecipe irecipe = RecipeConverter.convert(recipe);
        MineTweakerAPI.apply(new ActionAddRecipe(irecipe, recipe));
    }

    private static class ActionRemoveRecipes implements IUndoableAction {

        private final List<Integer> removingIndices;
        private final List<IRecipe> removingRecipes;

        public ActionRemoveRecipes(List<IRecipe> recipes, List<Integer> indices) {
            this.removingIndices = indices;
            this.removingRecipes = recipes;
        }

        @Override
        public void apply() {
            List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();
            for(int i = removingIndices.size() - 1; i >= 0; i--) {
                recipes.remove((int) removingIndices.get(i));
                MineTweakerAPI.getIjeiRecipeRegistry().removeRecipe(removingRecipes.get(i));
            }
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();
            for(int i = 0; i < removingIndices.size(); i++) {
                int index = Math.min(recipes.size(), removingIndices.get(i));
                recipes.add(index, removingRecipes.get(i));
                MineTweakerAPI.getIjeiRecipeRegistry().addRecipe(removingRecipes.get(i));
            }
        }

        @Override
        public String describe() {
            return "Removing " + removingIndices.size() + " wrappers";
        }

        @Override
        public String describeUndo() {
            return "Restoring " + removingIndices.size() + " wrappers";
        }

        @Override
        public Object getOverrideKey() {
            return null;
        }
    }

    private static class ContainerVirtual extends Container {

        @Override
        public boolean canInteractWith(EntityPlayer var1) {
            return false;
        }
    }

    private static class ActionAddRecipe implements IUndoableAction {

        private final IRecipe recipe;
        private final ICraftingRecipe craftingRecipe;

        public ActionAddRecipe(IRecipe recipe, ICraftingRecipe craftingRecipe) {
            this.recipe = recipe;
            this.craftingRecipe = craftingRecipe;
        }

        @Override
        public void apply() {
            List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();
            recipes.add(recipe);
            if(recipe instanceof ShapedRecipeBasic) {
                ShapedRecipeBasic r = (ShapedRecipeBasic) recipe;
            }
            MineTweakerAPI.getIjeiRecipeRegistry().addRecipe(recipe);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            List<IRecipe> recipes = CraftingManagerVC.getInstance().getRecipeList();
            recipes.remove(recipe);
            MineTweakerAPI.getIjeiRecipeRegistry().removeRecipe(recipe);
        }

        @Override
        public String describe() {
            return "Adding recipe for " + recipe.getRecipeOutput().getDisplayName();
        }

        @Override
        public String describeUndo() {
            return "Removing recipe for " + recipe.getRecipeOutput().getDisplayName();
        }

        @Override
        public Object getOverrideKey() {
            return null;
        }
    }

}
