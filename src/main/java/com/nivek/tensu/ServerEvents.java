package com.nivek.tensu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerEvents {
    private static final String CLIMB_TIMER="tensu_climb_timer";

    @SubscribeEvent public void onLogin(PlayerEvent.PlayerLoggedInEvent e){ if(e.getEntity() instanceof ServerPlayer p) Network.sync(p); }
    @SubscribeEvent public void onRespawn(PlayerEvent.PlayerRespawnEvent e){ if(e.getEntity() instanceof ServerPlayer p) Network.sync(p); }
    @SubscribeEvent public void onDeath(LivingDeathEvent e){
        if(!(e.getSource().getEntity() instanceof ServerPlayer p) || !(e.getEntity() instanceof LivingEntity)) return;
        ResourceLocation id=e.getEntity().getType().builtInRegistryHolder().key().location(); String ability=AbilityData.fromMob(id.toString()); if(ability==null)return;
        int old=AbilityData.level(p,ability); AbilityData.absorb(p,ability); int now=AbilityData.level(p,ability); int kills=AbilityData.kills(p,ability);
        String msg = now>old ? "§d✦ Habilidade evoluiu! §f"+AbilityData.name(ability)+" §7Nível "+now : "§d✦ Absorveu §f"+AbilityData.name(ability)+" §7Nível "+now+" §8("+kills+" abates)";
        p.displayClientMessage(net.minecraft.network.chat.Component.literal(msg),true); Network.sync(p);
    }

    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END || e.player.level().isClientSide())return;
        ServerPlayer p=(ServerPlayer)e.player;
        applyEffects(p);
        int timer=p.getPersistentData().getInt(CLIMB_TIMER); if(timer>0)p.getPersistentData().putInt(CLIMB_TIMER,timer-1);
        if(AbilityData.equipped(p,"spider_climb") && p.horizontalCollision && !p.isInWaterOrBubble()){
            int max=climbTicks(AbilityData.level(p,"spider_climb"));
            if(timer<=0)p.getPersistentData().putInt(CLIMB_TIMER,max);
            if(p.getPersistentData().getInt(CLIMB_TIMER)>0){var v=p.getDeltaMovement(); p.setDeltaMovement(v.x,0.13,v.z); p.hurtMarked=true;}
        }
    }

    private void applyEffects(ServerPlayer p){
        if(AbilityData.equipped(p,"cow_resistance")){int l=AbilityData.level(p,"cow_resistance"); p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,25,Math.min(2,(l-1)/2),true,false,false));}
        if(AbilityData.equipped(p,"turtle_defense")){int l=AbilityData.level(p,"turtle_defense"); p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,25,Math.min(3,(l+1)/2),true,false,false));}
        if(AbilityData.equipped(p,"wolf_instinct")){int l=AbilityData.level(p,"wolf_instinct"); p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,25,Math.min(2,(l-1)/2),true,false,false));}
        if(AbilityData.equipped(p,"fish_breath")){int l=AbilityData.level(p,"fish_breath"); int seconds=fishSeconds(l); if(p.isUnderWater() && p.tickCount%20==0)p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,seconds*20+10,0,true,false,false));}
        if(AbilityData.equipped(p,"zombie_nightvision")){int l=AbilityData.level(p,"zombie_nightvision"); p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,l>=5?MobEffectInstance.INFINITE_DURATION:80,0,true,false,false));}
        if(AbilityData.equipped(p,"iron_golem")){
            ItemStack main=p.getMainHandItem(); if(main.isEmpty()){int l=AbilityData.level(p,"iron_golem"); p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,25,Math.min(2,(l-1)/2),true,false,false));}
        }
    }

    @SubscribeEvent public void jump(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(AbilityData.equipped(p,"rabbit_jump")){int l=AbilityData.level(p,"rabbit_jump"); double bonus=0.025*l; var v=p.getDeltaMovement(); p.setDeltaMovement(v.x,v.y+bonus,v.z); p.hurtMarked=true;}
        if(AbilityData.equipped(p,"iron_golem") && p.getMainHandItem().isEmpty()){int l=AbilityData.level(p,"iron_golem"); double bonus=0.02*l; var v=p.getDeltaMovement(); p.setDeltaMovement(v.x,v.y+bonus,v.z); p.hurtMarked=true;}
    }

    private int fishSeconds(int l){return switch(l){case 1->8;case 2->15;case 3->25;case 4->40;default->60;};}
    private int climbTicks(int l){return switch(l){case 1->60;case 2->100;case 3->150;case 4->220;default->300;};}
}
