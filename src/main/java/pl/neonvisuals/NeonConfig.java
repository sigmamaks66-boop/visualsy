package pl.neonvisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NeonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("neonvisuals.json");

    public static final String[] MODE_NAMES = {"Tęcza", "Róż", "Niebieski", "Zielony"};

    public static boolean hat = true;
    public static boolean trail = true;
    public static boolean hit = true;
    public static boolean kill = true;
    public static boolean crosshair = true;
    public static boolean watermark = true;
    public static int colorMode = 0;

    private static final class Data {
        boolean hat = true, trail = true, hit = true, kill = true, crosshair = true, watermark = true;
        int colorMode = 0;
    }

    private NeonConfig() {}

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Data d = GSON.fromJson(Files.readString(FILE), Data.class);
                if (d != null) {
                    hat = d.hat; trail = d.trail; hit = d.hit; kill = d.kill;
                    crosshair = d.crosshair; watermark = d.watermark;
                    colorMode = Math.floorMod(d.colorMode, MODE_NAMES.length);
                }
            }
        } catch (Exception e) {
            System.err.println("[NeonVisuals] Nie udało się wczytać configu: " + e);
        }
    }

    public static void save() {
        try {
            Data d = new Data();
            d.hat = hat; d.trail = trail; d.hit = hit; d.kill = kill;
            d.crosshair = crosshair; d.watermark = watermark; d.colorMode = colorMode;
            Files.writeString(FILE, GSON.toJson(d));
        } catch (IOException e) {
            System.err.println("[NeonVisuals] Nie udało się zapisać configu: " + e);
        }
    }
}
