package se.gory_moon.vctweaker.fuel;

import com.google.common.collect.Lists;
import com.viesis.viescraft.configs.ViesCraftConfig;
import com.viesis.viescraft.init.InitItemsVC;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraftforge.event.ForgeEventFactory;
import se.gory_moon.vctweaker.VCTweakerContainer;

import java.util.ArrayList;

public class FuelHandler {

    private static ArrayList<String> fuels = Lists.newArrayList();

    public static void refreshConfigList(String[] list) {
        fuels.clear();
        for (String s: list) {
            String[] parts = s.split(":");

            String tmp = "";
            if (parts.length < 3)
                tmp = ":*";

            fuels.add(s + tmp);
        }
    }

    private static boolean containsItem(String location, int meta) {
        return fuels.contains(location + ":" + meta) || fuels.contains(location + ":*");
    }

    private static boolean isItemValid(boolean flag) {
        return (VCTweakerContainer.Configs.isWhitelist && !flag) || (!VCTweakerContainer.Configs.isWhitelist && flag);
    }

    public static int getItemBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        } else {
            Item item = stack.getItem();

            if ("minecraft".equals(item.getRegistryName().getResourceDomain()) || !VCTweakerContainer.Configs.moddedIgnoreList) {
                if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR) {
                    Block block = Block.getBlockFromItem(item);
                    boolean flag = containsItem(block.getRegistryName().toString(), stack.getMetadata()) || (VCTweakerContainer.Configs.woodMaterial && block.getDefaultState().getMaterial() == Material.WOOD);
                    if (isItemValid(flag))
                        return 0;
                } else {
                    boolean flag = containsItem(item.getRegistryName().toString(), stack.getMetadata());
                    if (isItemValid(flag))
                        return 0;
                }
            }

            if (ViesCraftConfig.vanillaFuel) {

                int val = item == Item.getItemFromBlock(Blocks.CARPET) ? 670:
                                (item instanceof ItemBoat ? 4000 :
                                        (item == Items.COAL ? 16000 :
                                                (item == Items.BLAZE_ROD ? 24000 :
                                                        (item == Item.getItemFromBlock(Blocks.COAL_BLOCK) ? 160000 :
                                                                (item == Items.LAVA_BUCKET ? 200000: 0)))));

                val = (item == Items.STICK) ||
                        (item == Items.BOWL) ||
                        (item == Item.getItemFromBlock(Blocks.SAPLING)) ||
                        (item == Item.getItemFromBlock(Blocks.WOODEN_BUTTON)) ||
                        (item == Item.getItemFromBlock(Blocks.WOOL)) ? 1000 : val;

                val = (item instanceof ItemDoor && item != Items.IRON_DOOR) ||
                        (item == Items.SIGN) ? 2000 : val;

                val = (item == Item.getItemFromBlock(Blocks.WOODEN_SLAB) ? 1500 :
                        (item == Item.getItemFromBlock(Blocks.LADDER) ||
                        (item == Items.BOW) ||
                        (item == Items.FISHING_ROD) ||
                        (Block.getBlockFromItem(item).getDefaultState().getMaterial() == Material.WOOD)) ? 3000 : val);

                if (val > 0)
                    return val;
            }

            if ((item instanceof ItemTool && "WOOD".equals(((ItemTool)item).getToolMaterialName())) || (item instanceof ItemSword && "WOOD".equals(((ItemSword)item).getToolMaterialName())) || (item instanceof ItemHoe && "WOOD".equals(((ItemHoe)item).getMaterialName())))
                return 2000;

            return item == InitItemsVC.VIESOLINE_PELLETS ? ViesCraftConfig.viesolineBurnTime * 20 * 10 : (ViesCraftConfig.outsideModFuel ? ForgeEventFactory.getItemBurnTime(stack) : 0);
        }
    }

}
