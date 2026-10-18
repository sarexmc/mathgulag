package com.mathgulag;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MathScreen extends Screen {
    private static ResourceLocation tex(String n) {
        return new ResourceLocation(MathGulag.MODID, "textures/gui/" + n + ".png");
    }

    static final ResourceLocation VEINS = tex("veins");
    static final ResourceLocation QBOX = tex("qbox");
    static final ResourceLocation[] SPIKE = new ResourceLocation[10];
    static final ResourceLocation[] DIGIT = new ResourceLocation[10];
    static final ResourceLocation GX = tex("glyph_x"), GEQ = tex("glyph_eq"), GQ = tex("glyph_q");

    static {
        for (int i = 0; i < 10; i++) {
            SPIKE[i] = tex("spike_" + i);
            DIGIT[i] = tex("glyph_" + i);
        }
    }

    // posicion (fraccion de pantalla) de cada digito 0..9, igual que en el diseno original
    static final float[][] POS = {
            {0.29f, 0.54f}, {0.14f, 0.44f}, {0.11f, 0.86f}, {0.70f, 0.44f}, {0.83f, 0.68f},
            {0.69f, 0.72f}, {0.39f, 0.77f}, {0.23f, 0.73f}, {0.49f, 0.86f}, {0.89f, 0.50f}};

    private int a, b, left, total;
    private String input = "";

    public MathScreen(int a, int b, int left, int total) {
        super(Component.literal("Gulag"));
        update(a, b, left, total);
    }

    public void update(int a, int b, int left, int total) {
        this.a = a; this.b = b; this.left = left; this.total = total; this.input = "";
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void tick() { if (left > 0) left--; }

    private int size() { return Math.max(34, Math.min(width, height) / 5); }

    private boolean hit(int d, double mx, double my) {
        double cx = POS[d][0] * width, cy = POS[d][1] * height, r = size() / 2.0;
        return (mx - cx) * (mx - cx) + (my - cy) * (my - cy) <= r * r;
    }

    private ResourceLocation glyphFor(char c) {
        if (c >= '0' && c <= '9') return DIGIT[c - '0'];
        if (c == 'X') return GX;
        if (c == '=') return GEQ;
        if (c == '?') return GQ;
        return null;
    }

    /** Dibuja texto con los glifos chorreantes, centrado en cx. h = alto de cada glifo. */
    private void glyphs(GuiGraphics g, String t, int cx, int y, int h) {
        int adv = (int) (h * 0.62f);
        int x = cx - (t.length() * adv) / 2 - (h - adv) / 2;
        for (int i = 0; i < t.length(); i++) {
            ResourceLocation r = glyphFor(t.charAt(i));
            if (r != null) g.blit(r, x + i * adv, y, h, h, 0, 0, 128, 128, 128, 128);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x30180003);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(VEINS, 0, 0, width, height, 0, 0, 512, 512, 512, 512);

        int s = size();
        for (int d = 0; d < 10; d++) {
            int cx = (int) (POS[d][0] * width), cy = (int) (POS[d][1] * height);
            int ss = hit(d, mx, my) ? s + 8 : s;
            g.blit(SPIKE[d], cx - ss / 2, cy - ss / 2, ss, ss, 0, 0, 128, 128, 128, 128);
        }

        int boxW = (int) (width * 0.40f), boxH = (int) (boxW * 0.5f);
        g.blit(QBOX, width / 2 - boxW / 2, (int) (height * 0.10f), boxW, boxH, 0, 0, 512, 256, 512, 256);
        int qh = Math.max(40, (int) (height * 0.17f));
        int qy = (int) (height * 0.10f) + boxH / 2 - qh / 2 - 6;
        glyphs(g, a + " X " + b, width / 2, qy, qh);
        glyphs(g, "= " + (input.isEmpty() ? "?" : input), width / 2, qy + qh - 8, qh / 2);

        int bw = (int) (width * 0.8f), x0 = width / 10, y0 = height - 12;
        g.fill(x0, y0, x0 + bw, y0 + 6, 0xFF3A0008);
        int fw = (int) (bw * Math.max(0, left) / (float) Math.max(1, total));
        g.fill(x0, y0, x0 + fw, y0 + 6, 0xFFFF3030);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (int d = 0; d < 10; d++) if (hit(d, mx, my)) { press(d); return true; }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mod) {
        if (key >= 48 && key <= 57) { press(key - 48); return true; }
        if (key >= 320 && key <= 329) { press(key - 320); return true; }
        if (key == 259) { input = ""; return true; }
        return super.keyPressed(key, sc, mod);
    }

    private void press(int d) {
        input += d;
        if (input.length() >= String.valueOf(a * b).length()) {
            int v = Integer.parseInt(input);
            input = "";
            Net.CH.sendToServer(new Net.AnswerMsg(v));
        }
    }
}
