package com.nivek.tensu;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CommandHandler {
    private CommandHandler() {}
    public static void register(){MinecraftForge.EVENT_BUS.register(CommandHandler.class);}
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){
        e.getDispatcher().register(net.minecraft.commands.Commands.literal("tensu_toggle")
            .then(net.minecraft.commands.Commands.argument("ability", com.mojang.brigadier.arguments.StringArgumentType.word())
                .executes(c->{var p=c.getSource().getPlayerOrException(); String id=com.mojang.brigadier.arguments.StringArgumentType.getString(c,"ability"); if(!AbilityData.known().contains(id)) return 0; AbilityData.toggle(p,id); Network.sync(p); return 1;})));
    }
}
