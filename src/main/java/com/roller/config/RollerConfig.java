package com.roller.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RollerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("librarian_roller.json").toFile();

    private static RollerConfig INSTANCE = new RollerConfig();

    // Büyü ve Fiyat Ayarları
    public String targetEnchantmentId = "minecraft:mending";
    public String targetEnchantmentName = "Mending (Tamir)";
    public int targetLevel = 1;
    public int minEmeralds = 10;
    public int maxEmeralds = 20;

    // Zamanlama ve Bekleme Ayarları (Milisaniye cinsinden)
    public long breakDurationMs = 500;        // Kürsü kırma süresi
    public long postBreakWaitMs = 500;        // Kürsü kırıldıktan sonra bekleme süresi
    public long professionResetWaitMs = 1000; // Meslek sıfırlanmasını bekleme süresi
    public long prePlaceWaitMs = 500;         // Kürsü yerleştirmeden önce bekleme süresi
    public long professionGainWaitMs = 1000;  // Meslek edinmesini bekleme süresi
    public long tradeCheckWaitMs = 500;       // Ticaretleri kontrol etmeden önce bekleme süresi

    // Güvenlik ve Zaman Aşımı Ayarları
    public long maxTimeoutMs = 12000;         // Herhangi bir adımda takılı kalmayı önleyen güvenlik zaman aşımı

    public static RollerConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                RollerConfig loaded = GSON.fromJson(reader, RollerConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                }
            } catch (IOException e) {
                System.err.println("[LibrarianRoller] Config dosyası okunamadı: " + e.getMessage());
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            System.err.println("[LibrarianRoller] Config dosyası kaydedilemedi: " + e.getMessage());
        }
    }
}
