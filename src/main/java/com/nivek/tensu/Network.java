package com.nivek.tensu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class Network {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    private Network() {}

    public static void init() {
        CHANNEL = ChannelBuilder.named(
                ResourceLocation.fromNamespaceAndPath(Tensu.MODID, "main"))
            .networkProtocolVersion(PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

        CHANNEL.messageBuilder(SyncPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(SyncPacket::encode)
            .decoder(SyncPacket::decode)
            .consumerMainThread(SyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(TogglePacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
            .encoder(TogglePacket::encode)
            .decoder(TogglePacket::decode)
            .consumerMainThread(TogglePacket::handle)
            .add();

        CHANNEL.messageBuilder(TeleportPacket.class, 2, NetworkDirection.PLAY_TO_SERVER)
            .encoder(TeleportPacket::encode)
            .decoder(TeleportPacket::decode)
            .consumerMainThread(TeleportPacket::handle)
            .add();
    }

    public static void sync(ServerPlayer player) {
        CHANNEL.send(SyncPacket.from(player), PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(Object packet) {
        CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }
}
