package com.mathgulag;

import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void question(int a, int b, int left, int total) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof MathScreen ms) ms.update(a, b, left, total);
        else mc.setScreen(new MathScreen(a, b, left, total));
    }

    public static void close() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof MathScreen) mc.setScreen(null);
    }
}
