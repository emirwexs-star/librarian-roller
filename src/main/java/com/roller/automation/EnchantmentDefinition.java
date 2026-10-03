package com.roller.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnchantmentDefinition {
    private final String id;
    private final String displayName;
    private final int maxLevel;

    public EnchantmentDefinition(String id, String displayName, int maxLevel) {
        this.id = id;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    private static final List<EnchantmentDefinition> ALL = new ArrayList<>();

    static {
        // En çok aranan popüler köylü büyüleri
        add("minecraft:mending", "Mending (Tamir)", 1);
        add("minecraft:unbreaking", "Unbreaking (Kırılmazlık)", 3);
        add("minecraft:protection", "Protection (Koruma)", 4);
        add("minecraft:blast_protection", "Blast Protection (Patlama Koruması)", 4);
        add("minecraft:fire_protection", "Fire Protection (Ateş Koruması)", 4);
        add("minecraft:projectile_protection", "Projectile Protection (Ok Koruması)", 4);
        add("minecraft:feather_falling", "Feather Falling (Tüy Düşüşü)", 4);
        add("minecraft:efficiency", "Efficiency (Verimlilik)", 5);
        add("minecraft:fortune", "Fortune (Servet)", 3);
        add("minecraft:silk_touch", "Silk Touch (İpeksi Dokunuş)", 1);
        add("minecraft:sharpness", "Sharpness (Keskinlik)", 5);
        add("minecraft:smite", "Smite (Darbe)", 5);
        add("minecraft:bane_of_arthropods", "Bane of Arthropods (Eklem Bacaklıların Kıyameti)", 5);
        add("minecraft:looting", "Looting (Ganimet)", 3);
        add("minecraft:power", "Power (Güç)", 5);
        add("minecraft:infinity", "Infinity (Sonsuzluk)", 1);
        add("minecraft:punch", "Punch (Yumruk)", 2);
        add("minecraft:flame", "Flame (Alev)", 1);
        add("minecraft:respiration", "Respiration (Solunum)", 3);
        add("minecraft:aqua_affinity", "Aqua Affinity (Su Adaptasyonu)", 1);
        add("minecraft:thorns", "Thorns (Dikenler)", 3);
        add("minecraft:depth_strider", "Depth Strider (Derin Koşucu)", 3);
        add("minecraft:frost_walker", "Frost Walker (Buz Yürüyüşü)", 2);
        add("minecraft:sweeping_edge", "Sweeping Edge (Süpürücü Bıçak)", 3);
        add("minecraft:loyalty", "Loyalty (Sadakat)", 3);
        add("minecraft:riptide", "Riptide (Girdap)", 3);
        add("minecraft:channeling", "Channeling (Kanalize)", 1);
        add("minecraft:impaling", "Impaling (Zıpkın)", 5);
        add("minecraft:quick_charge", "Quick Charge (Hızlı Doldurma)", 3);
        add("minecraft:multishot", "Multishot (Çoklu Atış)", 1);
        add("minecraft:piercing", "Piercing (Delme)", 4);
        add("minecraft:soul_speed", "Soul Speed (Ruh Hızı)", 3);
        add("minecraft:swift_sneak", "Swift Sneak (Seri Eğilme)", 3);
    }

    private static void add(String id, String name, int maxLevel) {
        ALL.add(new EnchantmentDefinition(id, name, maxLevel));
    }

    public static List<EnchantmentDefinition> getAll() {
        return Collections.unmodifiableList(ALL);
    }

    public static EnchantmentDefinition getById(String id) {
        for (EnchantmentDefinition def : ALL) {
            if (def.getId().equalsIgnoreCase(id)) {
                return def;
            }
        }
        return ALL.get(0); // Varsayılan Mending
    }
}
