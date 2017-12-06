package se.gory_moon.vctweaker;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import se.gory_moon.vctweaker.fuel.FuelHandler;

import javax.annotation.Nullable;
import java.io.File;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VCTweakerContainer extends DummyModContainer {

    public static final String MODID = "vctweaker";
    public static final String VERSION = "@MOD_VERSION@";

    public VCTweakerContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "vctweaker";
        meta.name = "ViesCraft Tweaker";
        meta.version = VERSION;
        meta.credits = "Viesis - The author of ViesCraft";
        meta.authorList = Collections.singletonList("GoryMoon");
        meta.description = "A mod that modifies and fixes some things in ViesCraft";
        meta.logoFile = "/assets/vctweaker/textures/logo.png";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Override
    public Set<ArtifactVersion> getRequirements() {
        return Collections.singleton(VersionParser.parseVersionReference("vc@[5.5.0,)"));
    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return Arrays.asList(VersionParser.parseVersionReference("vc@[5.5.0,)"), VersionParser.parseVersionReference("jei"));
    }

    @Subscribe
    public void modConstruction(FMLConstructionEvent event) {
        ConfigManager.loadData(event.getASMHarvestedData());
        ConfigManager.sync(this.getModId(), Config.Type.INSTANCE);
        FuelHandler.refreshConfigList(Configs.itemList);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @Nullable
    public Certificate getSigningCertificate() {
        Certificate[] certificates = getClass().getProtectionDomain().getCodeSource().getCertificates();
        return certificates != null ? certificates[0] : null;
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
            FuelHandler.refreshConfigList(Configs.itemList);
        }
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        return getSource() == null || getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
    }

    @Override
    public File getSource() {
        return VCTweakerCore.modFile;
    }

    @Override
    public Object getMod()
    {
        return this;
    }

    @Config(modid = MODID)
    public static class Configs {

        @Comment("Add JEI integration to ViesCraft")
        @RequiresMcRestart
        @Name("Add JEI")
        public static boolean replaceJEI = true;

        @Comment({"If the item list is a whitelist this needs to be true.", "If the item list is a blacklist this needs to be false"})
        @Name("Is Whitelist")
        public static boolean isWhitelist = true;

        @Comment({"If all blocks that are of wood material can be burnt in a airship.", "Can use this instead of defining everything in the list.", "It's affected by the whitelist setting"})
        @Name("Burn Wood Material")
        public static boolean woodMaterial = true;

        @Comment({"Make modded fuels ignore the whitelist/blacklist, still respects the ViesCraft setting about moded fuels.", "Mostly here to enable the default behaviour of the mod."})
        @Name("Modded Ignore Lists")
        public static boolean moddedIgnoreList = true;

        @Comment({"The list of items to either whitelist or blacklist when adding fuel to a airship.", "It still cares about ViesCraft fuel settings, to use vanilla/moded fuel or not.", "If you don't set a metadata for a item it automatically adds :* to the end, a wildcard for all metadata"})
        @LangKey("Item List")
        @Name("Item List")
        public static String[] itemList = {"minecraft:coal_block", "minecraft:sapling", "minecraft:stick", "minecraft:blaze_rod", "minecraft:coal",  "minecraft:lava_bucket", "vc:viesoline_pellets"};

    }
}
