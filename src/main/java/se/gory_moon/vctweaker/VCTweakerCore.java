package se.gory_moon.vctweaker;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions("se.gory_moon.vctweaker.asm")
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public class VCTweakerCore implements IFMLLoadingPlugin {

    public static File modFile = null;

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{getAccessTransformerClass()};
    }

    @Override
    public String getModContainerClass() {
        return "se.gory_moon.vctweaker.VCTweakerContainer";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return "se.gory_moon.vctweaker.VCTweakerChecker";
    }

    @Override
    public void injectData(Map<String, Object> data) {
        modFile = (File) data.get("coremodLocation");
    }

    @Override
    public String getAccessTransformerClass() {
        return "se.gory_moon.vctweaker.asm.VCTransformer";
    }
}
