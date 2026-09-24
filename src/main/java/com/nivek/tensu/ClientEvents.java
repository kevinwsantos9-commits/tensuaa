package com.nivek.tensu;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid=Tensu.MODID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    public static final KeyMapping MENU=new KeyMapping("key.tensu.menu",GLFW.GLFW_KEY_G,"key.categories.tensu");
    public static final KeyMapping TELEPORT=new KeyMapping("key.tensu.teleport",GLFW.GLFW_KEY_R,"key.categories.tensu");
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(MENU);e.register(TELEPORT);}

    @Mod.EventBusSubscriber(modid=Tensu.MODID,value=Dist.CLIENT)
    public static class Bus{
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
            if(e.phase!=TickEvent.Phase.END)return;
            Minecraft mc=Minecraft.getInstance();
            if(MENU.consumeClick() && mc.screen==null)mc.setScreen(new AbilityScreen());
            if(TELEPORT.consumeClick() && mc.screen==null && mc.player!=null && ClientState.get("enderman_teleport").equipped()) Network.sendToServer(new TeleportPacket());
        }
    }

    public static class AbilityScreen extends Screen {
        private int scroll=0;
        private final int PURPLE=0xFFB98CFF, PANEL=0xE8171324, PANEL2=0xFF241B34;
        public AbilityScreen(){super(Component.literal("TENSU"));}
        @Override protected void init(){ rebuild(); }
        private void rebuild(){
            clearWidgets(); int y=52-scroll;
            for(String id:AbilityData.known()){
                var d=ClientState.get(id); if(d.level()<=0){y+=72;continue;}
                int yy=y; addRenderableWidget(Button.builder(Component.literal(d.equipped()?"EQUIPADA":"EQUIPAR"),b->{Network.sendToServer(new TogglePacket(id));}).bounds(width-150,yy+24,120,20).build()); y+=72;
            }
            addRenderableWidget(Button.builder(Component.literal("Fechar"),b->onClose()).bounds(width/2-45,height-28,90,20).build());
        }
        @Override public boolean mouseScrolled(double x,double y,double scrollX,double scrollY){scroll+=(int)(-scrollY*30);scroll=Math.max(0,Math.min(scroll,Math.max(0,AbilityData.known().size()*72-height+100)));rebuild();return true;}
        @Override public void render(GuiGraphics g,int mx,int my,float pt){
            renderBackground(g,mx,my,pt); g.fill(0,0,width,height,0xC90B0810);
            g.fill(24,18,width-24,height-42,PANEL); g.fill(24,18,width-24,52,0xFF302044);
            g.drawString(font,"TENSU",42,30,PURPLE); g.drawString(font,"HABILIDADES ABSORVIDAS",42,41,0xFFD9C8E8);
            int y=58-scroll;
            for(String id:AbilityData.known()){
                var d=ClientState.get(id); if(d.level()<=0){y+=72;continue;}
                int req=AbilityData.requiredForNext(id,d.level()); int prev=0; for(int l=1;l<d.level();l++)prev+=AbilityData.requiredForNext(id,l); int in=d.kills()-prev; int bar=req==0?req:Math.min(req,Math.max(0,in));
                g.fill(38,y,width-170,y+62,PANEL2); g.drawString(font,AbilityData.name(id),48,y+8,PURPLE); g.drawString(font,"Nível "+d.level()+" / V",48,y+22,0xFFFFFFFF);
                g.drawString(font,AbilityData.shortInfo(id),48,y+37,0xFFB9AFC2);
                if(req>0){int bw=190;g.fill(48,y+52,48+bw,y+57,0xFF463A50);g.fill(48,y+52,48+(bw*bar/req),y+57,PURPLE);g.drawString(font,in+"/"+req+" para evoluir",245,y+48,0xFFD9C8E8);}else g.drawString(font,"NÍVEL MÁXIMO",245,y+48,PURPLE);
                y+=72;
            }
            super.render(g,mx,my,pt);
        }
    }
}
