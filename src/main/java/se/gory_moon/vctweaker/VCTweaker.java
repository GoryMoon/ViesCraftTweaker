package se.gory_moon.vctweaker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import se.gory_moon.vctweaker.fuel.FuelHandler;
import se.gory_moon.vctweaker.tweaker.TweakerIntegration;


@Mod(modid = VCTweaker.MODID, version = VCTweaker.VERSION, dependencies = "after:crafttweaker;required-after:vc;after:jei;")
public class VCTweaker
{
    public static final String MODID = "vctweaker";
    public static final String VERSION = "@MOD_VERSION@";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FuelHandler.refreshConfigList(Configs.itemList);
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("crafttweaker"))
            TweakerIntegration.register();
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
            FuelHandler.refreshConfigList(Configs.itemList);
        }
    }

    @Config(modid = MODID)
    public static class Configs {

        @Comment("Replaces the JEI integration from ViesCraft with the one from this mod")
        @RequiresMcRestart
        @Name("Replace JEI")
        public static boolean replaceJEI = true;

        @Comment({"If the item list is a whitelist this needs to be true.", "If the item list is a blacklist this needs to be false"})
        @Name("Is Whitelist")
        public static boolean isWhitelist = true;

        @Comment({"If all blocks that are of wood material can be burnt in a airship.", "Can use this instead of defining everything in the list.", "It's affected by the whitelist setting"})
        @Name("Burn Wood Material")
        public static boolean woodMaterial = true;

        @Comment({"Make modded fuels ignore the whitelist/blacklist, still respects the ViesCraft setting about moded fuels.", "Mostly here to enable the default behaviour of the mod."})
        @Name("Moded Ignore Lists")
        public static boolean modedIgnoreList = true;

        @Comment({"The list of items to either whitelist or blacklist when adding fuel to a airship.", "It still cares about ViesCraft fuel settings, to use vanilla/moded fuel or not.", "If you don't set a metadata for a item it automatically adds :* to the end, a wildcard for all metadata"})
        @LangKey("gui.config.itemlist")
        @Name("Item List")
        public static String[] itemList = {"minecraft:coal_block", "minecraft:sapling", "minecraft:stick", "minecraft:blaze_rod", "minecraft:coal",  "minecraft:lava_bucket", "vc:item_viesoline"};

    }
}
