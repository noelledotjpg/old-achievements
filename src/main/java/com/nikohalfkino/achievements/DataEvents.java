package com.nikohalfkino.achievements;

import com.nikohalfkino.achievements.background.BackgroundDataLoader;
import com.nikohalfkino.achievements.network.BackgroundSyncPacket;
import com.nikohalfkino.achievements.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;

@Mod.EventBusSubscriber(modid = ClassicAchievementsMod.MODID)
public class DataEvents {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new BackgroundDataLoader());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        Map<ResourceLocation, String> backgrounds = BackgroundDataLoader.activeJsons();
        ServerPlayer joiningPlayer = event.getPlayer();
        if (joiningPlayer != null) {
            send(joiningPlayer, backgrounds);
        } else {
            event.getPlayerList().getPlayers().forEach(player -> send(player, backgrounds));
        }
    }

    private static void send(ServerPlayer player, Map<ResourceLocation, String> backgrounds) {
        PacketHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new BackgroundSyncPacket(backgrounds));
    }
}
