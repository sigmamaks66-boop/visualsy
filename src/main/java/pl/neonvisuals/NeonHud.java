package pl.neonvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Neonowy celownik i znak wodny. */
public final class NeonHud {
    private NeonHud() {}

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        if (NeonConfig.crosshair && mc.currentScreen == null && mc.options.getPerspective().isFirstPerson()) {
            int cx = ctx.getScaledWindowWidth() / 2;
            int cy = ctx.getScaledWindowHeight() / 2;
            arm(ctx, cx - 8, cy, cx - 3, cy + 1, Neon.argb(0.00f));
            arm(ctx, cx + 4, cy, cx + 9, cy + 1, Neon.argb(0.25f));
            arm(ctx, cx, cy - 8, cx + 1, cy - 3, Neon.argb(0.50f));
            arm(ctx, cx, cy + 4, cx + 1, cy + 9, Neon.argb(0.75f));
            ctx.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
        }

        if (NeonConfig.watermark) {
            ctx.drawTextWithShadow(mc.textRenderer, Neon.rainbowText("NEON VISUALS"), 6, 6, 0xFFFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("/visuals"), 6, 17, 0xFF6F778A);
        }
    }

    private static void arm(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xB0000000);
        ctx.fill(x1, y1, x2, y2, color);
    }
}
