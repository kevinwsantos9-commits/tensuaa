package com.nivek.tensu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record SyncPacket(String[] ids, int[] kills, int[] levels, boolean[] equipped) {
    public static SyncPacket from(ServerPlayer p) {
        var ids = AbilityData.known().toArray(String[]::new);
        int[] kills = new int[ids.length];
        int[] levels = new int[ids.length];
        boolean[] eq = new boolean[ids.length];
        for (int i = 0; i < ids.length; i++) {
            kills[i] = AbilityData.kills(p, ids[i]);
            levels[i] = AbilityData.level(p, ids[i]);
            eq[i] = AbilityData.equipped(p, ids[i]);
        }
        return new SyncPacket(ids, kills, levels, eq);
    }

    public static void encode(SyncPacket m, FriendlyByteBuf b) {
        b.writeVarInt(m.ids.length);
        for (int i = 0; i < m.ids.length; i++) {
            b.writeUtf(m.ids[i]);
            b.writeVarInt(m.kills[i]);
            b.writeVarInt(m.levels[i]);
            b.writeBoolean(m.equipped[i]);
        }
    }

    public static SyncPacket decode(FriendlyByteBuf b) {
        int n = b.readVarInt();
        String[] ids = new String[n];
        int[] k = new int[n], l = new int[n];
        boolean[] e = new boolean[n];
        for (int i = 0; i < n; i++) {
            ids[i] = b.readUtf();
            k[i] = b.readVarInt();
            l[i] = b.readVarInt();
            e[i] = b.readBoolean();
        }
        return new SyncPacket(ids, k, l, e);
    }

    public static void handle(SyncPacket m, CustomPayloadEvent.Context ctx) {
        ClientState.clear();
        for (int i = 0; i < m.ids.length; i++) {
            ClientState.set(m.ids[i], m.kills[i], m.levels[i], m.equipped[i]);
        }
    }
}
