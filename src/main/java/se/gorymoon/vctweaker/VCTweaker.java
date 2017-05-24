package se.gorymoon.vctweaker;

import minetweaker.MineTweakerAPI;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import se.gorymoon.vctweaker.tweaker.TweakerIntegration;

@Mod(modid = VCTweaker.MODID, version = VCTweaker.VERSION, dependencies = "required-after:crafttweaker;")
public class VCTweaker
{
    public static final String MODID = "vctweaker";
    public static final String VERSION = "1.0";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent event) {
        MineTweakerAPI.registerClass(TweakerIntegration.class);
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
    }

    @Config(modid = MODID)
    public static class Configs {

        @Config.Comment("Replaces the JEI integration from ViesCraft with the one from this mod")
        @Config.RequiresMcRestart
        public static boolean replaceJEI = true;

    }

}
