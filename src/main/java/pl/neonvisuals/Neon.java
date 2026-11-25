package pl.neonvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/** Kolory i efekty (wszystko tylko po stronie klienta, to zwykłe cząsteczki). */
public final class Neon {
    private static final double TAU = Math.PI * 2;
    private static final Random R = new Random();

    private static LivingEntity target;
    private static int targetAge;
    private static int tick;
    private static double lastPx, lastPy, lastPz;

    private Neon() {}

    // ---------- kolory ----------

    public static float time() {
        return (System.currentTimeMillis() % 6000L) / 6000f;
    }

    public static int hsv(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        float hh = h * 6f;
        int i = (int) hh;
        float f = hh - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    /** Kolor RGB (bez alfy) zależny od wybranego trybu i czasu. */
    public static int rgb(float offset) {
        float t = time() + offset;
        float wave = (float) Math.sin(t * TAU);
        return switch (NeonConfig.colorMode) {
            case 1 -> hsv(0.86f + 0.07f * wave, 0.75f, 1f);
            case 2 -> hsv(0.56f + 0.05f * wave, 0.8f, 1f);
            case 3 -> hsv(0.36f + 0.05f * wave, 0.8f, 1f);
            default -> hsv(t, 0.8f, 1f);
        };
    }

    public static int argb(float offset) {
        return 0xFF000000 | rgb(offset);
    }

    public static MutableText rainbowText(String s) {
        MutableText out = Text.empty();
        for (int i = 0; i < s.length(); i++) {
            out.append(Text.literal(String.valueOf(s.charAt(i)))
                    .setStyle(Style.EMPTY.withColor(rgb(i * 0.06f)).withBold(true)));
        }
        return out;
    }

    // ---------- cząsteczki ----------

    private static void spawn(ClientWorld w, ParticleEffect e, double x, double y, double z,
                              double vx, double vy, double vz, boolean important) {
        if (important) {
            w.addImportantParticleClient(e, x, y, z, vx, vy, vz);
        } else {
            w.addParticleClient(e, x, y, z, vx, vy, vz);
        }
    }

    private static void dust(ClientWorld w, double x, double y, double z,
                             double vx, double vy, double vz, int rgb, float scale, boolean important) {
        spawn(w, new DustParticleEffect(rgb, scale), x, y, z, vx, vy, vz, important);
    }

    private static double rnd() {
        return R.nextDouble() * 2 - 1;
    }

    /** Mały wybuch przy trafieniu. */
    public static void hitBurst(Entity e) {
        ClientWorld w = MinecraftClient.getInstance().world;
        if (w == null) return;
        double cx = e.getX(), cy = e.getY() + e.getHeight() * 0.6, cz = e.getZ();
        for (int i = 0; i < 18; i++) {
            double a = R.nextDouble() * TAU;
            double sp = 0.06 + R.nextDouble() * 0.1;
            dust(w, cx, cy, cz, Math.cos(a) * sp, (R.nextDouble() - 0.3) * 0.2, Math.sin(a) * sp,
                    rgb(i * 0.05f), 0.9f, false);
        }
        for (int i = 0; i < 5; i++) {
            spawn(w, ParticleTypes.END_ROD, cx, cy, cz, rnd() * 0.1, rnd() * 0.1, rnd() * 0.1, false);
        }
    }

    /** Duży efekt po zabiciu: pierścienie, słup światła i totemy. */
    public static void killBurst(double x, double y, double z) {
        ClientWorld w = MinecraftClient.getInstance().world;
        if (w == null) return;

        for (int ring = 0; ring < 3; ring++) {
            int n = 28;
            for (int i = 0; i < n; i++) {
                double a = i * TAU / n;
                double sp = 0.12 + ring * 0.05;
                dust(w, x, y + 0.2 + ring * 0.55, z, Math.cos(a) * sp, 0.02, Math.sin(a) * sp,
                        rgb(i / (float) n + ring * 0.2f), 1.1f, true);
            }
        }
        for (int i = 0; i < 26; i++) {
            dust(w, x + rnd() * 0.15, y + i * 0.1, z + rnd() * 0.15, 0, 0.1 + R.nextDouble() * 0.1, 0,
                    rgb(i * 0.04f), 1.2f, true);
        }
        for (int i = 0; i < 40; i++) {
            spawn(w, ParticleTypes.TOTEM_OF_UNDYING, x, y + 1.0, z,
                    rnd() * 0.4, 0.1 + R.nextDouble() * 0.5, rnd() * 0.4, true);
        }
    }

    /** Podgląd efektu zabicia kilka kroków przed Tobą. */
    public static void testBurst() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return;
        Vec3d v = p.getRotationVec(1f);
        killBurst(p.getX() + v.x * 2.5, p.getY(), p.getZ() + v.z * 2.5);
    }

    // ---------- zdarzenia ----------

    public static void onAttack(Entity e) {
        if (!(e instanceof LivingEntity le)) return;
        if (NeonConfig.hit) hitBurst(e);
        if (NeonConfig.kill && e instanceof PlayerEntity) {
            target = le;
            targetAge = 0;
        }
    }

    public static void clientTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        ClientWorld w = mc.world;
        if (p == null || w == null) {
            target = null;
            return;
        }
        tick++;

        // wykrywanie zabicia gracza, w którego ostatnio uderzyłeś
        if (target != null) {
            if (target.getHealth() <= 0.0f) {
                killBurst(target.getX(), target.getY(), target.getZ());
                target = null;
            } else if (target.isRemoved() || ++targetAge > 120) {
                target = null;
            }
        }

        // chiński kapelusz nad głową
        if (NeonConfig.hat && !p.isSpectator()) {
            double base = p.getY() + p.getHeight() + 0.02;
            double ang = tick * 0.3;
            for (int s = 0; s < 4; s++) {
                for (int l = 0; l < 4; l++) {
                    double a = ang + s * Math.PI / 2;
                    double r = 0.65 * (1 - l / 4.0) + 0.05;
                    dust(w, p.getX() + Math.cos(a) * r, base + l * 0.1, p.getZ() + Math.sin(a) * r,
                            0, 0, 0, rgb(l * 0.08f + s * 0.05f), 0.55f, false);
                }
            }
        }

        // ślad za graczem
        if (NeonConfig.trail) {
            double dx = p.getX() - lastPx, dy = p.getY() - lastPy, dz = p.getZ() - lastPz;
            if (dx * dx + dy * dy + dz * dz > 0.0016) {
                for (int k = 0; k < 3; k++) {
                    dust(w, p.getX() + rnd() * 0.2, p.getY() + 0.1 + R.nextDouble() * 0.2, p.getZ() + rnd() * 0.2,
                            0, 0.02, 0, rgb(k * 0.1f), 0.9f, false);
                }
            }
        }
        lastPx = p.getX();
        lastPy = p.getY();
        lastPz = p.getZ();
    }
}
