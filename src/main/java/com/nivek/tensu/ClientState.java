package com.nivek.tensu;

import java.util.HashMap;
import java.util.Map;

public final class ClientState {
    public record Entry(int kills, int level, boolean equipped) {}
    private static final Map<String, Entry> DATA = new HashMap<>();
    private ClientState() {}
    public static void clear() { DATA.clear(); }
    public static void set(String id, int kills, int level, boolean equipped) { DATA.put(id, new Entry(kills, level, equipped)); }
    public static Entry get(String id) { return DATA.getOrDefault(id, new Entry(0, 0, false)); }
}
