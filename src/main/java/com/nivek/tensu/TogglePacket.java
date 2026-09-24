package com.nivek.tensu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record TogglePacket(String id) {
    public static void encode(TogglePacket m, FriendlyByteBuf b) {
        b.writeUtf(m.id);
    }

    public static TogglePacket decode(FriendlyByteBuf b) {
        return new TogglePacket(b.readUtf());
    }

    public static void handle(TogglePacket m, CustomPayloadEvent.Context ctx) {
        var p = ctx.getSender();
        if (p != null && AbilityData.known().contains(m.id)) {
            AbilityData.toggle(p, m.id);
            Network.sync(p);
        }
    }
}
