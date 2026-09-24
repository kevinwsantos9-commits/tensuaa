package com.nivek.tensu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record TeleportPacket() {
    public static void encode(TeleportPacket m, FriendlyByteBuf b) {}

    public static TeleportPacket decode(FriendlyByteBuf b) {
        return new TeleportPacket();
    }

    public static void handle(TeleportPacket m, CustomPayloadEvent.Context ctx) {
        ServerPlayer p = ctx.getSender();
        if (p == null || !AbilityData.equipped(p, "enderman_teleport")) return;

        BlockHitResult hit = p.level().clip(new ClipContext(
            p.getEyePosition(),
            p.getEyePosition().add(p.getLookAngle().scale(32)),
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            p));

        Vec3 dest;
        if (hit.getType() == HitResult.Type.BLOCK) {
            dest = hit.getLocation().add(
                hit.getDirection().getStepX() * 1.5,
                hit.getDirection().getStepY() * 1.5,
                hit.getDirection().getStepZ() * 1.5);
        } else {
            dest = p.position().add(p.getLookAngle().scale(8));
        }

        if (p.level().noCollision(p.getBoundingBox().move(dest.subtract(p.position())))) {
            p.teleportTo(dest.x, dest.y, dest.z);
            p.resetFallDistance();
            p.hurtMarked = true;
        }
    }
}
