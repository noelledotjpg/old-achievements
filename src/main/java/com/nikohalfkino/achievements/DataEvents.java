package com.nikohalfkino.achievements;

import com.nikohalfkino.achievements.background.BackgroundDataLoader;
import com.nikohalfkino.achievements.network.BackgroundSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

@net.neoforged.fml.common.Mod(OldAchievements.MODID)
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
        PacketDistributor.sendToPlayer(player, new BackgroundSyncPacket(backgrounds));
    }
}
