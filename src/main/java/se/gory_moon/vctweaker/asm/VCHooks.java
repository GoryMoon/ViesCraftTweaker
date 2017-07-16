package se.gory_moon.vctweaker.asm;

import net.minecraft.client.gui.inventory.GuiContainer;

public class VCHooks {

    public static void drawDefaultBack(GuiContainer screen) {
        screen.drawDefaultBackground();
    }
}
