package com.nivek.tensu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class AbilityData {
    private static final String ROOT = "tensu";
    private static final String KILLS = "kills";
    private static final String LEVELS = "levels";
    private static final String EQUIPPED = "equipped";

    private AbilityData() {}

    private static CompoundTag root(Player p) {
        CompoundTag pd = p.getPersistentData();
        if (!pd.contains(ROOT)) pd.put(ROOT, new CompoundTag());
        return pd.getCompound(ROOT);
    }

    private static CompoundTag section(Player p, String key) {
        CompoundTag r = root(p);
        if (!r.contains(key)) r.put(key, new CompoundTag());
        return r.getCompound(key);
    }

    public static int kills(Player p, String id) { return section(p, KILLS).getInt(id); }
    public static int level(Player p, String id) { return section(p, LEVELS).getInt(id); }
    public static boolean equipped(Player p, String id) { return section(p, EQUIPPED).getBoolean(id); }

    public static int requiredForNext(String id, int level) {
        return switch (id) {
            case "rabbit_jump" -> switch (level) { case 1 -> 5; case 2 -> 10; case 3 -> 20; case 4 -> 35; default -> 0; };
            case "cow_resistance", "chicken_feather", "wolf_instinct", "turtle_defense" -> switch (level) { case 1 -> 5; case 2 -> 10; case 3 -> 20; case 4 -> 35; default -> 0; };
            case "fish_breath" -> switch (level) { case 1 -> 4; case 2 -> 8; case 3 -> 15; case 4 -> 25; default -> 0; };
            case "iron_golem" -> switch (level) { case 1 -> 2; case 2 -> 4; case 3 -> 7; case 4 -> 12; default -> 0; };
            case "enderman_teleport" -> switch (level) { case 1 -> 2; case 2 -> 4; case 3 -> 7; case 4 -> 12; default -> 0; };
            case "spider_climb" -> switch (level) { case 1 -> 3; case 2 -> 6; case 3 -> 10; case 4 -> 16; default -> 0; };
            case "zombie_nightvision" -> switch (level) { case 1 -> 5; case 2 -> 10; case 3 -> 20; case 4 -> 35; default -> 0; };
            default -> 0;
        };
    }

    public static int maxLevel(String id) { return 5; }

    public static void absorb(Player p, String id) {
        CompoundTag r = root(p);
        CompoundTag ks = section(p, KILLS);
        CompoundTag ls = section(p, LEVELS);
        int k = ks.getInt(id) + 1;
        ks.putInt(id, k);
        int old = ls.getInt(id);
        int next = Math.min(maxLevel(id), old == 0 ? 1 : old);
        if (old == 0) next = 1;
        else if (old < maxLevel(id)) {
            int required = requiredForNext(id, old);
            if (required > 0 && k >= cumulativeKills(id, old)) next = old + 1;
        }
        ls.putInt(id, next);
        r.put(KILLS, ks);
        r.put(LEVELS, ls);
        r.put(EQUIPPED, section(p, EQUIPPED));
    }

    private static int cumulativeKills(String id, int targetLevel) {
        int total = 0;
        for (int level = 1; level <= targetLevel; level++) total += requiredForNext(id, level);
        return total;
    }

    public static void toggle(Player p, String id) {
        if (level(p, id) <= 0) return;
        CompoundTag eq = section(p, EQUIPPED);
        eq.putBoolean(id, !eq.getBoolean(id));
        root(p).put(EQUIPPED, eq);
    }

    public static List<String> known() {
        return List.of("rabbit_jump", "cow_resistance", "chicken_feather", "wolf_instinct", "turtle_defense", "fish_breath", "iron_golem", "enderman_teleport", "spider_climb", "zombie_nightvision");
    }

    public static String fromMob(String mobId) {
        return switch (mobId) {
            case "minecraft:rabbit" -> "rabbit_jump";
            case "minecraft:cow" -> "cow_resistance";
            case "minecraft:chicken" -> "chicken_feather";
            case "minecraft:wolf" -> "wolf_instinct";
            case "minecraft:turtle" -> "turtle_defense";
            case "minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish" -> "fish_breath";
            case "minecraft:iron_golem" -> "iron_golem";
            case "minecraft:enderman" -> "enderman_teleport";
            case "minecraft:spider", "minecraft:cave_spider" -> "spider_climb";
            case "minecraft:zombie", "minecraft:husk", "minecraft:drowned" -> "zombie_nightvision";
            default -> null;
        };
    }

    public static String name(String id) {
        return switch (id) {
            case "rabbit_jump" -> "Super Salto";
            case "cow_resistance" -> "Resistência";
            case "chicken_feather" -> "Queda Suave";
            case "wolf_instinct" -> "Instinto";
            case "turtle_defense" -> "Defesa";
            case "fish_breath" -> "Respiração Aquática";
            case "iron_golem" -> "Força do Golem";
            case "enderman_teleport" -> "Teleporte";
            case "spider_climb" -> "Escalada";
            case "zombie_nightvision" -> "Visão Noturna";
            default -> id;
        };
    }

    public static String shortInfo(String id) {
        return switch (id) {
            case "rabbit_jump" -> "Aumenta a altura do salto aos poucos.";
            case "cow_resistance" -> "Reduz parte do dano recebido.";
            case "chicken_feather" -> "Reduz o dano de queda.";
            case "wolf_instinct" -> "Melhora velocidade e percepção em combate.";
            case "turtle_defense" -> "Aumenta sua resistência.";
            case "fish_breath" -> "Aumenta seu tempo debaixo d'água.";
            case "iron_golem" -> "Mais força e salto quando a mão está vazia.";
            case "enderman_teleport" -> "Pressione R para se teletransportar.";
            case "spider_climb" -> "Escala paredes por tempo limitado.";
            case "zombie_nightvision" -> "Visão noturna; no nível V fica permanente.";
            default -> "";
        };
    }
}
