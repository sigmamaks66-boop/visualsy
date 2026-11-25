package pl.neonvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NeonScreen extends Screen {
    private static final int W = 340;
    private static final int H = 246;

    private static final String[] NAMES = {
            "Chiński kapelusz", "Ślad za graczem", "Efekt trafienia",
            "Efekt zabicia", "Celownik neon", "Znak wodny"
    };

    private int left;
    private int top;
    private final ButtonWidget[] tiles = new ButtonWidget[NAMES.length];
    private ButtonWidget colorBtn;
    private ButtonWidget testBtn;
    private ButtonWidget saveBtn;

    public NeonScreen() {
        super(Text.literal("Neon Visuals"));
    }

    private static boolean get(int i) {
        return switch (i) {
            case 0 -> NeonConfig.hat;
            case 1 -> NeonConfig.trail;
            case 2 -> NeonConfig.hit;
            case 3 -> NeonConfig.kill;
            case 4 -> NeonConfig.crosshair;
            default -> NeonConfig.watermark;
        };
    }

    private static void flip(int i) {
        switch (i) {
            case 0 -> NeonConfig.hat = !NeonConfig.hat;
            case 1 -> NeonConfig.trail = !NeonConfig.trail;
            case 2 -> NeonConfig.hit = !NeonConfig.hit;
            case 3 -> NeonConfig.kill = !NeonConfig.kill;
            case 4 -> NeonConfig.crosshair = !NeonConfig.crosshair;
            default -> NeonConfig.watermark = !NeonConfig.watermark;
        }
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;

        for (int i = 0; i < tiles.length; i++) {
            final int idx = i;
            int col = i % 2, row = i / 2;
            tiles[i] = ButtonWidget.builder(Text.empty(), b -> flip(idx))
                    .dimensions(left + 16 + col * 158, top + 50 + row * 40, 150, 34).build();
            addDrawableChild(tiles[i]);
        }

        colorBtn = ButtonWidget.builder(Text.empty(),
                        b -> NeonConfig.colorMode = (NeonConfig.colorMode + 1) % NeonConfig.MODE_NAMES.length)
                .dimensions(left + 16, top + 170, W - 32, 24).build();
        addDrawableChild(colorBtn);

        testBtn = ButtonWidget.builder(Text.empty(), b -> {
            close();
            Neon.testBurst();
        }).dimensions(left + 16, top + 204, 150, 26).build();
        addDrawableChild(testBtn);

        saveBtn = ButtonWidget.builder(Text.empty(), b -> close())
                .dimensions(left + 174, top + 204, 150, 26).build();
        addDrawableChild(saveBtn);
    }

    // ---------- pomocnicze rysowanie ----------

    private static void box(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 1, y1, x2 - 1, y2, color);
        ctx.fill(x1, y1 + 1, x2, y2 - 1, color);
    }

    private static void frame(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 1, y1, x2 - 1, y1 + 1, color);
        ctx.fill(x1 + 1, y2 - 1, x2 - 1, y2, color);
        ctx.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        ctx.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    private static boolean hover(ButtonWidget b, int mx, int my) {
        return mx >= b.getX() && mx < b.getX() + b.getWidth() && my >= b.getY() && my < b.getY() + b.getHeight();
    }

    private void centered(DrawContext ctx, Text text, ButtonWidget b, int color) {
        int x = b.getX() + (b.getWidth() - textRenderer.getWidth(text)) / 2;
        int y = b.getY() + (b.getHeight() - 8) / 2;
        ctx.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    private void gradientButton(DrawContext ctx, ButtonWidget b, boolean hovered, float shift) {
        float v = hovered ? 1f : 0.85f;
        int c1 = 0xFF000000 | Neon.hsv(Neon.time() + shift, 0.7f, v);
        int c2 = 0xFF000000 | Neon.hsv(Neon.time() + shift + 0.12f, 0.8f, v * 0.9f);
        int x1 = b.getX(), y1 = b.getY(), x2 = x1 + b.getWidth(), y2 = y1 + b.getHeight();
        ctx.fillGradient(x1, y1 + 1, x2, y2 - 1, c1, c2);
        ctx.fill(x1 + 1, y1, x2 - 1, y1 + 1, c1);
        ctx.fill(x1 + 1, y2 - 1, x2 - 1, y2, c2);
    }

    // ---------- rysowanie ----------

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        float t = Neon.time();
        ctx.fillGradient(left - 2, top - 2, left + W + 2, top + H + 2,
                0xFF000000 | Neon.hsv(t, 0.75f, 1f), 0xFF000000 | Neon.hsv(t + 0.3f, 0.75f, 1f));
        box(ctx, left, top, left + W, top + H, 0xFF0E1018);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        float t = Neon.time();
        int gray = 0xFF7C849B;

        ctx.drawTextWithShadow(textRenderer, Neon.rainbowText("NEON VISUALS"), left + 18, top + 14, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("efekty widzisz tylko Ty, serwer nic o nich nie wie"),
                left + 18, top + 27, 0xFF6F778A);
        ctx.drawTextWithShadow(textRenderer, Text.literal("MODUŁY"), left + 16, top + 40, gray);

        // kafelki z przełącznikami
        for (int i = 0; i < tiles.length; i++) {
            ButtonWidget b = tiles[i];
            boolean on = get(i);
            boolean hov = hover(b, mouseX, mouseY);
            int x1 = b.getX(), y1 = b.getY(), x2 = x1 + b.getWidth(), y2 = y1 + b.getHeight();

            box(ctx, x1, y1, x2, y2, hov ? 0xFF22263A : 0xFF181B28);
            if (on) {
                frame(ctx, x1, y1, x2, y2, 0xFF000000 | Neon.hsv(t + i * 0.1f, 0.7f, 1f));
            }
            ctx.drawTextWithShadow(textRenderer, Text.literal(NAMES[i]), x1 + 9, y1 + 13, on ? 0xFFFFFFFF : 0xFF8A90A5);

            // przełącznik
            int sx = x2 - 9 - 28, sy = y1 + 10;
            box(ctx, sx, sy, sx + 28, sy + 14, on ? (0xFF000000 | Neon.hsv(t + i * 0.1f, 0.55f, 0.95f)) : 0xFF2A2E3D);
            int kx = on ? sx + 16 : sx + 2;
            box(ctx, kx, sy + 2, kx + 10, sy + 12, 0xFFFFFFFF);
        }

        // tryb kolorów
        boolean hc = hover(colorBtn, mouseX, mouseY);
        int cx1 = colorBtn.getX(), cy1 = colorBtn.getY(), cx2 = cx1 + colorBtn.getWidth(), cy2 = cy1 + colorBtn.getHeight();
        box(ctx, cx1, cy1, cx2, cy2, hc ? 0xFF22263A : 0xFF181B28);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Kolory: ").append(Text.literal(NeonConfig.MODE_NAMES[NeonConfig.colorMode]).formatted(Formatting.BOLD)),
                cx1 + 9, cy1 + 8, 0xFFFFFFFF);
        for (int i = 0; i < 10; i++) {
            ctx.fill(cx2 - 9 - (10 - i) * 12, cy1 + 7, cx2 - 9 - (10 - i) * 12 + 10, cy1 + 17, Neon.argb(i * 0.1f));
        }

        // test efektu
        boolean ht = hover(testBtn, mouseX, mouseY);
        box(ctx, testBtn.getX(), testBtn.getY(), testBtn.getX() + testBtn.getWidth(), testBtn.getY() + testBtn.getHeight(),
                ht ? 0xFF3A3F5E : 0xFF22263A);
        centered(ctx, Text.literal("TEST EFEKTU"), testBtn, 0xFFFFD166);

        // zapisz
        gradientButton(ctx, saveBtn, hover(saveBtn, mouseX, mouseY), 0f);
        centered(ctx, Text.literal("ZAPISZ").formatted(Formatting.BOLD), saveBtn, 0xFFFFFFFF);

        Text hint = Text.literal("/visuals otwiera to menu");
        ctx.drawTextWithShadow(textRenderer, hint, left + (W - textRenderer.getWidth(hint)) / 2, top + H + 8, 0xFF8A90A5);
    }

    @Override
    public void close() {
        NeonConfig.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
