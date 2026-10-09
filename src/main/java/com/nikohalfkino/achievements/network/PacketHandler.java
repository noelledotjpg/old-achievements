package com.nikohalfkino.achievements.network;

import com.nikohalfkino.achievements.ClassicAchievementsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ClassicAchievementsMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private PacketHandler() {}

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(
                id++,
                InventoryOpenPacket.class,
                InventoryOpenPacket::encode,
                InventoryOpenPacket::decode,
                InventoryOpenPacket::handle
        );
        INSTANCE.registerMessage(
                id,
                BackgroundSyncPacket.class,
                BackgroundSyncPacket::encode,
                BackgroundSyncPacket::decode,
                BackgroundSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
}
